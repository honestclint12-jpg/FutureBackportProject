package com.futurebackport.client.assets;

import com.futurebackport.FutureBackport;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
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
         showFailureToast();
      }
   }

   /** Waits until the loading screen is gone: a toast added under it would time out before anyone sees it. */
   private static void showFailureToast() {
      Minecraft minecraft = Minecraft.getInstance();
      try {
         for (int i = 0; i < 1200 && (minecraft.getOverlay() != null || minecraft.screen == null && minecraft.level == null); i++) {
            Thread.sleep(250L);
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return;
      }
      minecraft.execute(() -> SystemToast.add(
         minecraft.getToasts(),
         SystemToast.SystemToastId.PACK_LOAD_FAILURE,
         Component.translatable("futurebackport.assets.download_failed.title"),
         Component.translatable("futurebackport.assets.download_failed.description")
      ));
   }

   private void download() throws Exception {
      this.deleteStaleFiles();
      // Plain files are checked against their SHA-1 on every start. Derived files have no fixed hash, so they are
      // trusted once a complete download for this exact manifest has been recorded.
      Path marker = this.root.resolve(COMPLETE_MARKER);
      boolean complete = Files.exists(marker) && Files.readString(marker).trim().equals(this.manifestHash);
      Map<String, JsonObject> missing = new LinkedHashMap<>();
      for (Map.Entry<String, JsonObject> file : this.files.entrySet()) {
         Path target = this.root.resolve(file.getKey());
         JsonObject entry = file.getValue();
         boolean ok = Files.isRegularFile(target) && (isDerived(entry) ? complete : sha1(Files.readAllBytes(target)).equals(expectedSha1(entry)));
         if (!ok) {
            missing.put(file.getKey(), entry);
         }
      }
      if (missing.isEmpty()) {
         if (!complete) {
            Files.writeString(marker, this.manifestHash);
         }
         return;
      }
      Files.deleteIfExists(marker);
      FutureBackport.LOGGER.info("Downloading {} Minecraft texture and sound files from Mojang for Future Backport", missing.size());
      long start = System.nanoTime();
      this.downloadAll(missing);
      Files.writeString(marker, this.manifestHash);
      FutureBackport.LOGGER.info("Downloaded Future Backport's Minecraft assets in {} ms", (System.nanoTime() - start) / 1_000_000L);
   }

   private void downloadAll(Map<String, JsonObject> missing) throws Exception {
      Http http = new Http(Minecraft.getInstance().getProxy());
      // Jar entries, including the images that derived files are built from, grouped by jar.
      Map<String, Set<String>> wantedByJar = new HashMap<>();
      for (JsonObject entry : missing.values()) {
         for (JsonObject source : sources(entry)) {
            wantedByJar.computeIfAbsent(source.get("jar").getAsString(), jar -> new LinkedHashSet<>()).add(source.get("path").getAsString());
         }
      }
      Map<String, Map<String, byte[]>> jarFiles = new ConcurrentHashMap<>();
      List<Exception> failures = Collections.synchronizedList(new ArrayList<>());
      // The first failed request stops all the others, so a dead connection fails within one timeout.
      AtomicBoolean abort = new AtomicBoolean();
      ExecutorService executor = Executors.newFixedThreadPool(PARALLEL_DOWNLOADS, runnable -> {
         Thread thread = new Thread(runnable, "Future Backport asset download worker");
         thread.setDaemon(true);
         return thread;
      });
      try {
         // Step 1: read each jar's directory to plan its range requests.
         Map<String, RemoteZip> zips = new HashMap<>();
         Map<String, Future<List<RemoteZip.Group>>> plans = new HashMap<>();
         for (Map.Entry<String, Set<String>> jar : wantedByJar.entrySet()) {
            URI uri = this.jars.get(jar.getKey());
            if (uri == null) {
               throw new IOException("The asset list names an unknown Minecraft jar " + jar.getKey());
            }
            RemoteZip zip = new RemoteZip(http, uri);
            zips.put(jar.getKey(), zip);
            jarFiles.put(jar.getKey(), new ConcurrentHashMap<>());
            plans.put(jar.getKey(), executor.submit(() -> zip.plan(jar.getValue())));
         }
         // Step 2: every range request and every sound file runs as its own task.
         List<Future<?>> tasks = new ArrayList<>();
         for (Map.Entry<String, JsonObject> file : missing.entrySet()) {
            JsonObject entry = file.getValue();
            if (entry.has("object")) {
               String hash = entry.get("object").getAsString();
               URI uri = URI.create(OBJECTS_URL + hash.substring(0, 2) + "/" + hash);
               tasks.add(submit(executor, abort, failures, () -> this.write(file.getKey(), verify(http.get(uri, null, 200), hash, file.getKey()))));
            }
         }
         for (Map.Entry<String, Future<List<RemoteZip.Group>>> plan : plans.entrySet()) {
            String jar = plan.getKey();
            RemoteZip zip = zips.get(jar);
            Map<String, byte[]> out = jarFiles.get(jar);
            try {
               for (RemoteZip.Group group : plan.getValue().get()) {
                  tasks.add(submit(executor, abort, failures, () -> zip.read(group, out::put, (name, e) -> failures.add(e))));
               }
            } catch (ExecutionException e) {
               if (e.getCause() instanceof RemoteZip.RangeUnsupportedException) {
                  tasks.add(submit(executor, abort, failures, () -> zip.readWhole(wantedByJar.get(jar), out::put)));
               } else {
                  failures.add(e.getCause() instanceof Exception cause ? cause : e);
                  abort.set(true);
               }
            }
         }
         for (Future<?> task : tasks) {
            task.get();
         }
         // Step 3: write the jar files that arrived, building the derived ones.
         for (Map.Entry<String, JsonObject> file : missing.entrySet()) {
            JsonObject entry = file.getValue();
            if (!entry.has("jar")) {
               continue;
            }
            try {
               byte[] base = sourceBytes(jarFiles, entry, file.getKey());
               if (isDerived(entry)) {
                  this.writeDerived(file.getKey(), base, entry, jarFiles);
               } else {
                  this.write(file.getKey(), base);
               }
            } catch (IOException e) {
               failures.add(e);
            }
         }
         if (!failures.isEmpty()) {
            IOException error = new IOException(failures.size() + " Minecraft asset downloads failed, the first ones shown below");
            failures.stream().limit(3).forEach(error::addSuppressed);
            throw error;
         }
      } finally {
         executor.shutdownNow();
      }
   }

   private interface IoTask {
      void run() throws IOException;
   }

   private static Future<?> submit(ExecutorService executor, AtomicBoolean abort, List<Exception> failures, IoTask task) {
      return executor.submit(() -> {
         if (abort.get()) {
            return;
         }
         try {
            task.run();
         } catch (IOException | RuntimeException e) {
            failures.add(e);
            abort.set(true);
         }
      });
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

   private static boolean isDerived(JsonObject entry) {
      return entry.has("overlay") || entry.has("patch");
   }

   private static String expectedSha1(JsonObject entry) {
      return entry.has("object") ? entry.get("object").getAsString() : entry.get("sha1").getAsString();
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

   /** Removes files that are not in the manifest (left by older versions of the mod, or added by hand). */
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

   private static String sha1(byte[] bytes) {
      try {
         return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(bytes));
      } catch (NoSuchAlgorithmException e) {
         throw new IllegalStateException(e);
      }
   }
}
