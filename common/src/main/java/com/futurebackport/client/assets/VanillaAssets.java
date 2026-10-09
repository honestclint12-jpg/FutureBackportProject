package com.futurebackport.client.assets;

import com.futurebackport.FutureBackport;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

/**
 * The Minecraft textures and sounds the mod uses but does not ship. The list lives in
 * {@code futurebackport_vanilla_assets.json} (written by {@code tools/vanilla-assets.py}); the files themselves are
 * downloaded from Mojang's servers into {@code <game dir>/futurebackport/vanilla-assets} the first time the game
 * starts, and served from there by {@link VanillaAssetsPack} after that.
 */
public final class VanillaAssets {
   private static final String MANIFEST = "/futurebackport_vanilla_assets.json";
   private static final String OBJECTS_URL = "https://resources.download.minecraft.net/";
   private static final String COMPLETE_MARKER = ".complete";
   private static final int PARALLEL_DOWNLOADS = 8;
   @Nullable
   private static VanillaAssets instance;

   private final Path root;
   private final Map<String, JsonObject> files = new LinkedHashMap<>();
   private final Map<String, URI> jars = new HashMap<>();
   private final Set<String> namespaces = new LinkedHashSet<>();
   private final String manifestHash;
   private final CompletableFuture<Void> ready;

   private VanillaAssets(Path root) {
      this.root = root;
      byte[] manifest;
      try (InputStream in = VanillaAssets.class.getResourceAsStream(MANIFEST)) {
         if (in == null) {
            throw new IllegalStateException("Missing " + MANIFEST);
         }
         manifest = in.readAllBytes();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
      this.manifestHash = sha1(manifest);
      JsonObject json = GsonHelper.parse(new String(manifest, StandardCharsets.UTF_8));
      for (Map.Entry<String, JsonElement> jar : json.getAsJsonObject("jars").entrySet()) {
         this.jars.put(jar.getKey(), URI.create(jar.getValue().getAsJsonObject().get("url").getAsString()));
      }
      for (Map.Entry<String, JsonElement> file : json.getAsJsonObject("files").entrySet()) {
         this.files.put(file.getKey(), file.getValue().getAsJsonObject());
         this.namespaces.add(file.getKey().split("/")[1]);
      }
      Thread thread = new Thread(this::fetchMissing, "Future Backport asset download");
      this.ready = new CompletableFuture<>();
      thread.setDaemon(true);
      thread.start();
   }

   /** Starts the download on first use; later calls return the same instance. */
   public static synchronized VanillaAssets get() {
      if (instance == null) {
         instance = new VanillaAssets(Minecraft.getInstance().gameDirectory.toPath().resolve(FutureBackport.MODID).resolve("vanilla-assets"));
      }
      return instance;
   }

   Path root() {
      return this.root;
   }

   Set<String> namespaces() {
      return this.namespaces;
   }

   /** Whether {@code path} (like {@code assets/futurebackport/textures/block/x.png}) is one of these files. */
   boolean contains(String path) {
      return this.files.containsKey(path);
   }

   /** Whether any of these files lives under the folder {@code prefix}. */
   boolean containsUnder(String prefix) {
      String folder = prefix.endsWith("/") ? prefix : prefix + "/";
      return this.files.keySet().stream().anyMatch(path -> path.startsWith(folder));
   }

   /** Blocks until the download has finished or failed. Files that could not be downloaded are simply missing. */
   void awaitDownload() {
      this.ready.handle((ok, error) -> null).join();
   }

   private void fetchMissing() {
      try {
         this.download();
         this.ready.complete(null);
      } catch (Throwable e) {
         FutureBackport.LOGGER.error(
            "Could not download the Minecraft textures and sounds Future Backport needs. Its blocks and mobs will look and sound wrong until the game is restarted with an internet connection.",
            e
         );
         this.ready.completeExceptionally(e);
      }
   }

   private void download() throws Exception {
      Path marker = this.root.resolve(COMPLETE_MARKER);
      if (Files.exists(marker) && Files.readString(marker).trim().equals(this.manifestHash)
         && this.files.keySet().stream().allMatch(path -> Files.isRegularFile(this.root.resolve(path)))) {
         return;
      }
      this.deleteStaleFiles();
      Map<String, JsonObject> missing = new LinkedHashMap<>();
      for (Map.Entry<String, JsonObject> file : this.files.entrySet()) {
         Path target = this.root.resolve(file.getKey());
         JsonObject entry = file.getValue();
         boolean derived = entry.has("overlay") || entry.has("patch");
         if (derived || !Files.isRegularFile(target) || !sha1(Files.readAllBytes(target)).equals(expectedSha1(entry))) {
            missing.put(file.getKey(), entry);
         }
      }
      if (!missing.isEmpty()) {
         FutureBackport.LOGGER.info("Downloading {} Minecraft texture and sound files from Mojang for Future Backport (first start only)", missing.size());
         long start = System.nanoTime();
         this.downloadAll(missing);
         FutureBackport.LOGGER.info("Downloaded Future Backport's Minecraft assets in {} ms", (System.nanoTime() - start) / 1_000_000L);
      }
      Files.writeString(marker, this.manifestHash);
   }

   private void downloadAll(Map<String, JsonObject> missing) throws Exception {
      HttpClient http = httpClient();
      // Jar entries, including the images that derived files are built from, grouped by jar.
      Map<String, Set<String>> wantedByJar = new HashMap<>();
      for (JsonObject entry : missing.values()) {
         for (JsonObject source : sources(entry)) {
            wantedByJar.computeIfAbsent(source.get("jar").getAsString(), jar -> new LinkedHashSet<>()).add(source.get("path").getAsString());
         }
      }
      ExecutorService executor = Executors.newFixedThreadPool(PARALLEL_DOWNLOADS, runnable -> {
         Thread thread = new Thread(runnable, "Future Backport asset download worker");
         thread.setDaemon(true);
         return thread;
      });
      try {
         List<Future<?>> tasks = new ArrayList<>();
         Map<String, Map<String, byte[]>> jarFiles = new ConcurrentHashMap<>();
         for (Map.Entry<String, Set<String>> jar : wantedByJar.entrySet()) {
            URI uri = this.jars.get(jar.getKey());
            if (uri == null) {
               throw new IOException("The asset list names an unknown Minecraft jar " + jar.getKey());
            }
            Map<String, byte[]> out = new ConcurrentHashMap<>();
            jarFiles.put(jar.getKey(), out);
            tasks.add(executor.submit(() -> {
               new RemoteZip(http, uri).read(jar.getValue(), out::put);
               return null;
            }));
         }
         for (Map.Entry<String, JsonObject> file : missing.entrySet()) {
            JsonObject entry = file.getValue();
            if (entry.has("object")) {
               String hash = entry.get("object").getAsString();
               tasks.add(executor.submit(() -> {
                  this.write(file.getKey(), verify(fetchObject(http, hash), hash, file.getKey()));
                  return null;
               }));
            }
         }
         List<Exception> failures = new ArrayList<>();
         for (Future<?> task : tasks) {
            try {
               task.get();
            } catch (java.util.concurrent.ExecutionException e) {
               failures.add(e.getCause() instanceof Exception cause ? cause : e);
            }
         }
         for (Map.Entry<String, JsonObject> file : missing.entrySet()) {
            JsonObject entry = file.getValue();
            if (!entry.has("jar")) {
               continue;
            }
            try {
               byte[] base = sourceBytes(jarFiles, entry, file.getKey());
               if (entry.has("overlay") || entry.has("patch")) {
                  this.writeDerived(file.getKey(), base, entry, jarFiles);
               } else {
                  this.write(file.getKey(), base);
               }
            } catch (IOException e) {
               failures.add(e);
            }
         }
         if (!failures.isEmpty()) {
            IOException error = new IOException(failures.size() + " Minecraft asset downloads failed");
            failures.forEach(error::addSuppressed);
            throw error;
         }
      } finally {
         executor.shutdownNow();
      }
   }

   private void writeDerived(String path, byte[] base, JsonObject entry, Map<String, Map<String, byte[]>> jarFiles) throws IOException {
      try (NativeImage image = NativeImage.read(base)) {
         if (entry.has("overlay")) {
            JsonObject overlaySource = entry.getAsJsonObject("overlay");
            try (NativeImage overlay = NativeImage.read(sourceBytes(jarFiles, overlaySource, path))) {
               for (int y = 0; y < Math.min(image.getHeight(), overlay.getHeight()); y++) {
                  for (int x = 0; x < Math.min(image.getWidth(), overlay.getWidth()); x++) {
                     image.setPixelRGBA(x, y, blend(image.getPixelRGBA(x, y), overlay.getPixelRGBA(x, y)));
                  }
               }
            }
         }
         if (entry.has("patch")) {
            JsonArray patch = entry.getAsJsonArray("patch");
            for (JsonElement pixel : patch) {
               String[] parts = pixel.getAsString().split(",");
               int rgba = (int)Long.parseLong(parts[2], 16);
               int abgr = (rgba & 0xFF) << 24 | (rgba >>> 8 & 0xFF) << 16 | (rgba >>> 16 & 0xFF) << 8 | rgba >>> 24;
               image.setPixelRGBA(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), abgr);
            }
         }
         Path target = this.root.resolve(path);
         Files.createDirectories(target.getParent());
         Path temp = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
         image.writeToFile(temp);
         move(temp, target);
      }
   }

   /** Source-over blending of two ABGR pixels. */
   private static int blend(int below, int above) {
      int aboveAlpha = above >>> 24;
      if (aboveAlpha == 255) {
         return above;
      }
      if (aboveAlpha == 0) {
         return below;
      }
      int belowAlpha = (below >>> 24) * (255 - aboveAlpha) / 255;
      int alpha = aboveAlpha + belowAlpha;
      int result = alpha << 24;
      for (int shift = 0; shift < 24; shift += 8) {
         int channel = ((above >>> shift & 0xFF) * aboveAlpha + (below >>> shift & 0xFF) * belowAlpha) / alpha;
         result |= channel << shift;
      }
      return result;
   }

   private static byte[] sourceBytes(Map<String, Map<String, byte[]>> jarFiles, JsonObject source, String forPath) throws IOException {
      byte[] bytes = jarFiles.getOrDefault(source.get("jar").getAsString(), Map.of()).get(source.get("path").getAsString());
      if (bytes == null) {
         throw new IOException("Could not get " + source.get("path").getAsString() + " for " + forPath);
      }
      return verify(bytes, source.get("sha1").getAsString(), forPath);
   }

   private static List<JsonObject> sources(JsonObject entry) {
      List<JsonObject> sources = new ArrayList<>();
      if (entry.has("jar")) {
         sources.add(entry);
      }
      if (entry.has("overlay")) {
         sources.add(entry.getAsJsonObject("overlay"));
      }
      return sources;
   }

   @Nullable
   private static String expectedSha1(JsonObject entry) {
      return entry.has("object") ? entry.get("object").getAsString() : entry.get("sha1").getAsString();
   }

   private static byte[] fetchObject(HttpClient http, String hash) throws IOException, InterruptedException {
      URI uri = URI.create(OBJECTS_URL + hash.substring(0, 2) + "/" + hash);
      HttpResponse<byte[]> response = http.send(HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(1)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
      if (response.statusCode() != 200) {
         throw new IOException("HTTP " + response.statusCode() + " for " + uri);
      }
      return response.body();
   }

   private static byte[] verify(byte[] bytes, String sha1, String forPath) throws IOException {
      if (!sha1(bytes).equals(sha1)) {
         throw new IOException("Downloaded file for " + forPath + " does not match its checksum");
      }
      return bytes;
   }

   private void write(String path, byte[] bytes) throws IOException {
      Path target = this.root.resolve(path);
      Files.createDirectories(target.getParent());
      Path temp = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
      Files.write(temp, bytes);
      move(temp, target);
   }

   private static void move(Path from, Path to) throws IOException {
      try {
         Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException e) {
         Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
      }
   }

   /** Removes files left behind by older versions of the mod, so they cannot shadow anything. */
   private void deleteStaleFiles() throws IOException {
      Path assets = this.root.resolve("assets");
      if (!Files.isDirectory(assets)) {
         return;
      }
      try (Stream<Path> walk = Files.walk(assets)) {
         for (Path file : walk.filter(Files::isRegularFile).toList()) {
            if (!this.files.containsKey(this.root.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "/"))) {
               Files.delete(file);
            }
         }
      }
   }

   private static HttpClient httpClient() {
      HttpClient.Builder builder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NORMAL);
      Proxy proxy = Minecraft.getInstance().getProxy();
      if (proxy != null && proxy.type() == Proxy.Type.HTTP && proxy.address() instanceof InetSocketAddress address) {
         builder.proxy(ProxySelector.of(address));
      }
      return builder.build();
   }

   private static String sha1(byte[] bytes) {
      try {
         return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(bytes));
      } catch (NoSuchAlgorithmException e) {
         throw new IllegalStateException(e);
      }
   }
}
