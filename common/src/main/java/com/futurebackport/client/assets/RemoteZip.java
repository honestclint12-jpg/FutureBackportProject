package com.futurebackport.client.assets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Reads single entries out of a zip file on a web server with HTTP range requests, so getting a few hundred
 * textures out of a 40 MB client jar downloads only those textures and the jar's directory. Servers that ignore
 * the Range header get the whole file streamed once instead.
 */
final class RemoteZip {
   /** Neighbouring entries closer than this are fetched in one request... */
   private static final int MERGE_GAP = 64 * 1024;
   /** ...as long as that request stays below this size, so requests stay small enough to run side by side. */
   private static final int MAX_GROUP = 1024 * 1024;
   /** Room for a local file header's name and extra field, which can differ from the central directory's. */
   private static final int LOCAL_HEADER_SLACK = 30 + 1024;

   private record Entry(String name, int method, long compressedSize, long size, long offset) {
   }

   /** Entries fetched with one range request. */
   record Group(List<Entry> entries, long from, long to) {
   }

   /** Thrown by {@link #plan} when the server ignores range requests; use {@link #readWhole} then. */
   static final class RangeUnsupportedException extends IOException {
      RangeUnsupportedException(URI uri) {
         super("No range requests for " + uri);
      }
   }

   private final Http http;
   private final URI uri;

   RemoteZip(Http http, URI uri) {
      this.http = http;
      this.uri = uri;
   }

   /** Reads the zip's directory and splits the wanted entries that exist into range requests. */
   List<Group> plan(Collection<String> wanted) throws IOException {
      Map<String, Entry> directory = this.readDirectory();
      List<Entry> entries = new ArrayList<>();
      for (String name : wanted) {
         Entry entry = directory.get(name);
         if (entry != null) {
            entries.add(entry);
         }
      }
      entries.sort(Comparator.comparingLong(Entry::offset));
      List<Group> groups = new ArrayList<>();
      int start = 0;
      while (start < entries.size()) {
         int end = start + 1;
         long from = entries.get(start).offset();
         long to = end(entries.get(start));
         while (end < entries.size() && entries.get(end).offset() - to < MERGE_GAP && end(entries.get(end)) - from <= MAX_GROUP) {
            to = Math.max(to, end(entries.get(end)));
            end++;
         }
         groups.add(new Group(List.copyOf(entries.subList(start, end)), from, to));
         start = end;
      }
      return groups;
   }

   /**
    * Fetches one group and calls {@code sink} with each entry's uncompressed bytes. An entry that cannot be read
    * goes to {@code failed} without affecting the others; a failed request fails the whole group.
    */
   void read(Group group, BiConsumer<String, byte[]> sink, BiConsumer<String, IOException> failed) throws IOException {
      ByteBuffer block = ByteBuffer.wrap(this.http.get(this.uri, "bytes=" + group.from() + "-" + (group.to() - 1), 206)).order(ByteOrder.LITTLE_ENDIAN);
      for (Entry entry : group.entries()) {
         byte[] data;
         try {
            data = extract(block, (int)(entry.offset() - group.from()), entry);
         } catch (IOException | RuntimeException e) {
            try {
               // Most likely a local header longer than the slack allowed for: fetch this entry on its own.
               data = this.readAlone(entry);
            } catch (IOException e2) {
               e2.addSuppressed(e);
               failed.accept(entry.name(), e2);
               continue;
            }
         }
         sink.accept(entry.name(), data);
      }
   }

   void readWhole(Collection<String> wanted, BiConsumer<String, byte[]> sink) throws IOException {
      try (ZipInputStream zip = new ZipInputStream(this.http.stream(this.uri))) {
         ZipEntry entry;
         while ((entry = zip.getNextEntry()) != null) {
            if (wanted.contains(entry.getName())) {
               ByteArrayOutputStream out = new ByteArrayOutputStream();
               zip.transferTo(out);
               sink.accept(entry.getName(), out.toByteArray());
            }
         }
      }
   }

   private byte[] readAlone(Entry entry) throws IOException {
      ByteBuffer header = ByteBuffer.wrap(this.http.get(this.uri, "bytes=" + entry.offset() + "-" + (entry.offset() + 29), 206)).order(ByteOrder.LITTLE_ENDIAN);
      if (header.limit() < 30 || header.getInt(0) != 0x04034b50) {
         throw new IOException("Bad local header for " + entry.name());
      }
      long length = 30L + Short.toUnsignedInt(header.getShort(26)) + Short.toUnsignedInt(header.getShort(28)) + entry.compressedSize();
      ByteBuffer block = ByteBuffer.wrap(this.http.get(this.uri, "bytes=" + entry.offset() + "-" + (entry.offset() + length - 1), 206))
         .order(ByteOrder.LITTLE_ENDIAN);
      return extract(block, 0, entry);
   }

   private Map<String, Entry> readDirectory() throws IOException {
      Http.Response tailResponse = this.http.getRange(this.uri, "bytes=-" + (22 + 65535));
      if (tailResponse.status() != 206) {
         throw new RangeUnsupportedException(this.uri);
      }
      String contentRange = tailResponse.contentRange();
      if (contentRange == null || !contentRange.contains("/")) {
         throw new IOException("No Content-Range for " + this.uri);
      }
      long length = Long.parseLong(contentRange.substring(contentRange.lastIndexOf('/') + 1).trim());
      byte[] tail = tailResponse.body();
      ByteBuffer buf = ByteBuffer.wrap(tail).order(ByteOrder.LITTLE_ENDIAN);
      int eocd = tail.length - 22;
      while (eocd >= 0 && buf.getInt(eocd) != 0x06054b50) {
         eocd--;
      }
      if (eocd < 0) {
         throw new IOException("No zip directory in " + this.uri);
      }
      long dirSize = Integer.toUnsignedLong(buf.getInt(eocd + 12));
      long dirOffset = Integer.toUnsignedLong(buf.getInt(eocd + 16));
      if (dirOffset == 0xFFFFFFFFL || dirOffset + dirSize > length) {
         throw new IOException("Unsupported zip layout in " + this.uri);
      }
      ByteBuffer dir = ByteBuffer.wrap(this.http.get(this.uri, "bytes=" + dirOffset + "-" + (dirOffset + dirSize - 1), 206)).order(ByteOrder.LITTLE_ENDIAN);
      Map<String, Entry> entries = new HashMap<>();
      int pos = 0;
      while (pos + 46 <= dir.limit() && dir.getInt(pos) == 0x02014b50) {
         int method = Short.toUnsignedInt(dir.getShort(pos + 10));
         long compressed = Integer.toUnsignedLong(dir.getInt(pos + 20));
         long size = Integer.toUnsignedLong(dir.getInt(pos + 24));
         int nameLength = Short.toUnsignedInt(dir.getShort(pos + 28));
         int extraLength = Short.toUnsignedInt(dir.getShort(pos + 30));
         int commentLength = Short.toUnsignedInt(dir.getShort(pos + 32));
         long offset = Integer.toUnsignedLong(dir.getInt(pos + 42));
         String name = new String(slice(dir, pos + 46, nameLength), StandardCharsets.UTF_8);
         entries.put(name, new Entry(name, method, compressed, size, offset));
         pos += 46 + nameLength + extraLength + commentLength;
      }
      return entries;
   }

   private static long end(Entry entry) {
      return entry.offset() + entry.compressedSize() + LOCAL_HEADER_SLACK;
   }

   private static byte[] extract(ByteBuffer block, int pos, Entry entry) throws IOException {
      if (pos + 30 > block.limit() || block.getInt(pos) != 0x04034b50) {
         throw new IOException("Bad local header for " + entry.name());
      }
      int dataStart = pos + 30 + Short.toUnsignedInt(block.getShort(pos + 26)) + Short.toUnsignedInt(block.getShort(pos + 28));
      byte[] data = slice(block, dataStart, (int)entry.compressedSize());
      if (entry.method() == 0) {
         return data;
      }
      if (entry.method() != 8) {
         throw new IOException("Unsupported compression for " + entry.name());
      }
      Inflater inflater = new Inflater(true);
      try {
         inflater.setInput(data);
         byte[] out = new byte[(int)entry.size()];
         int n = 0;
         while (n < out.length && !inflater.finished()) {
            int read = inflater.inflate(out, n, out.length - n);
            if (read == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
               break;
            }
            n += read;
         }
         if (n != out.length) {
            throw new IOException("Truncated entry " + entry.name());
         }
         return out;
      } catch (DataFormatException e) {
         throw new IOException("Corrupt entry " + entry.name(), e);
      } finally {
         inflater.end();
      }
   }

   private static byte[] slice(ByteBuffer buf, int pos, int length) throws IOException {
      if (pos < 0 || length < 0 || pos + length > buf.limit()) {
         throw new IOException("Zip entry runs past the downloaded range");
      }
      byte[] out = new byte[length];
      buf.get(pos, out);
      return out;
   }
}
