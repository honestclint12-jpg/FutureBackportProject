package com.futurebackport.client.assets;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import org.jetbrains.annotations.Nullable;

/**
 * Plain GET requests through the same proxy as the rest of the game: the one the launcher passed to Minecraft,
 * or the JVM's proxy settings when there is none. {@code java.net.http.HttpClient} cannot use the SOCKS proxy
 * Minecraft builds from {@code --proxyHost}, which is why this uses {@link HttpURLConnection}.
 */
final class Http {
   private static final int CONNECT_TIMEOUT_MS = 15_000;
   /** Per read, not for the whole body, so slow but working connections are fine. */
   private static final int READ_TIMEOUT_MS = 30_000;
   private static final int MAX_BODY = 64 * 1024 * 1024;

   record Response(int status, @Nullable String contentRange, byte[] body) {
   }

   @Nullable
   private final Proxy proxy;

   Http(@Nullable Proxy proxy) {
      this.proxy = proxy == null || proxy.type() == Proxy.Type.DIRECT ? null : proxy;
   }

   /** Downloads {@code uri} (or the byte range {@code range} of it) and fails on any other status than {@code expected}. */
   byte[] get(URI uri, @Nullable String range, int expected) throws IOException {
      HttpURLConnection connection = this.connect(uri, range);
      try {
         int status = connection.getResponseCode();
         if (status != expected) {
            throw new IOException("HTTP " + status + " for " + uri);
         }
         return readBody(connection, uri);
      } finally {
         connection.disconnect();
      }
   }

   /**
    * Asks for a byte range and returns the body only when the server answered with that range (206).
    * A 200 means the server ignored the range; its body (the whole file) is left unread.
    */
   Response getRange(URI uri, String range) throws IOException {
      HttpURLConnection connection = this.connect(uri, range);
      try {
         int status = connection.getResponseCode();
         if (status == 200) {
            return new Response(200, null, new byte[0]);
         }
         if (status != 206) {
            throw new IOException("HTTP " + status + " for " + uri);
         }
         return new Response(206, connection.getHeaderField("Content-Range"), readBody(connection, uri));
      } finally {
         connection.disconnect();
      }
   }

   /** Opens a streamed download of the whole file. The caller closes the stream. */
   InputStream stream(URI uri) throws IOException {
      HttpURLConnection connection = this.connect(uri, null);
      int status = connection.getResponseCode();
      if (status != 200) {
         connection.disconnect();
         throw new IOException("HTTP " + status + " for " + uri);
      }
      return connection.getInputStream();
   }

   private HttpURLConnection connect(URI uri, @Nullable String range) throws IOException {
      HttpURLConnection connection = (HttpURLConnection)(this.proxy == null ? uri.toURL().openConnection() : uri.toURL().openConnection(this.proxy));
      connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
      connection.setReadTimeout(READ_TIMEOUT_MS);
      connection.setInstanceFollowRedirects(true);
      if (range != null) {
         connection.setRequestProperty("Range", range);
      }
      return connection;
   }

   private static byte[] readBody(HttpURLConnection connection, URI uri) throws IOException {
      long length = connection.getContentLengthLong();
      if (length > MAX_BODY) {
         throw new IOException("Unexpectedly large response (" + length + " bytes) for " + uri);
      }
      try (InputStream in = connection.getInputStream()) {
         byte[] body = in.readNBytes(MAX_BODY + 1);
         if (body.length > MAX_BODY) {
            throw new IOException("Unexpectedly large response for " + uri);
         }
         return body;
      }
   }
}
