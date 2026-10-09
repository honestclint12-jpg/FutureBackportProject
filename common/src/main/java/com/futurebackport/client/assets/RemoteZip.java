package com.futurebackport.client.assets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
   /** Neighbouring entries closer than this are fetched in one request. */
   private static final int MERGE_GAP = 64 * 1024;
   /** Room for a local file header's name and extra field, which can differ from the central directory's. */
   private static final int LOCAL_HEADER_SLACK = 30 + 1024;

   private record Entry(String name, int method, long compressedSize, long size, long offset) {
   }

   private final HttpClient http;
   private final URI uri;

   RemoteZip(HttpClient http, URI uri) {
      this.http = http;
      this.uri = uri;
   }

   /** Calls {@code sink} with the uncompressed bytes of every wanted entry that exists. */
   void read(Collection<String> wanted, BiConsumer<String, byte[]> sink) throws IOException, InterruptedException {
      Map<String, Entry> directory;
      try {
         directory = this.readDirectory();
      } catch (RangeUnsupportedException e) {
         this.readWhole(wanted, sink);
         return;
      }
      List<Entry> entries = new ArrayList<>();
      for (String name : wanted) {
         Entry entry = directory.get(name);
         if (entry != null) {
            entries.add(entry);
         }
      }
      entries.sort(Comparator.comparingLong(Entry::offset));
      int start = 0;
      while (start < entries.size()) {
         int end = start + 1;
         long from = entries.get(start).offset();
         long to = from + entries.get(start).compressedSize() + LOCAL_HEADER_SLACK;
         while (end < entries.size() && entries.get(end).offset() - to < MERGE_GAP) {
            to = Math.max(to, entries.get(end).offset() + entries.get(end).compressedSize() + LOCAL_HEADER_SLACK);
            end++;
         }
         ByteBuffer block = ByteBuffer.wrap(this.range(from, to - 1)).order(ByteOrder.LITTLE_ENDIAN);
         for (Entry entry : entries.subList(start, end)) {
            sink.accept(entry.name(), extract(block, (int)(entry.offset() - from), entry));
         }
         start = end;
      }
   }

   private Map<String, Entry> readDirectory() throws IOException, InterruptedException {
      HttpResponse<byte[]> tailResponse = this.send("bytes=-" + (22 + 65535));
      if (tailResponse.statusCode() != 206) {
         throw new RangeUnsupportedException();
      }
      long length = Long.parseLong(tailResponse.headers().firstValue("Content-Range").orElseThrow().replaceAll(".*/", ""));
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
      ByteBuffer dir = ByteBuffer.wrap(this.range(dirOffset, dirOffset + dirSize - 1)).order(ByteOrder.LITTLE_ENDIAN);
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

   private static byte[] extract(ByteBuffer block, int pos, Entry entry) throws IOException {
      if (block.getInt(pos) != 0x04034b50) {
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

   private void readWhole(Collection<String> wanted, BiConsumer<String, byte[]> sink) throws IOException, InterruptedException {
      HttpResponse<InputStream> response = this.http.send(HttpRequest.newBuilder(this.uri).timeout(Duration.ofMinutes(5)).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
      if (response.statusCode() != 200) {
         response.body().close();
         throw new IOException("HTTP " + response.statusCode() + " for " + this.uri);
      }
      try (ZipInputStream zip = new ZipInputStream(response.body())) {
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

   private byte[] range(long from, long to) throws IOException, InterruptedException {
      HttpResponse<byte[]> response = this.send("bytes=" + from + "-" + to);
      if (response.statusCode() != 206) {
         throw new IOException("HTTP " + response.statusCode() + " for a range of " + this.uri);
      }
      return response.body();
   }

   private HttpResponse<byte[]> send(String range) throws IOException, InterruptedException {
      return this.http.send(HttpRequest.newBuilder(this.uri).timeout(Duration.ofMinutes(1)).header("Range", range).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
   }

   private static byte[] slice(ByteBuffer buf, int pos, int length) throws IOException {
      if (pos < 0 || pos + length > buf.limit()) {
         throw new IOException("Zip entry runs past the downloaded range");
      }
      byte[] out = new byte[length];
      buf.get(pos, out);
      return out;
   }

   private static final class RangeUnsupportedException extends IOException {
   }
}
