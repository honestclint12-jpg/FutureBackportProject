package com.futurebackport.client.assets;

import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

/**
 * Serves {@link VanillaAssets} as an always-on resource pack. Whether a path is in the pack is answered from the
 * manifest straight away; only reading one of its files waits for the first-start download, so the rest of the
 * resource reload is never held up.
 */
public final class VanillaAssetsPack implements PackResources {
   private static final String ID = "futurebackport_vanilla_assets";
   /** Always on and pinned to the bottom of the list, so every resource pack the player adds can retexture the mod. */
   public static final RepositorySource SOURCE = consumer -> {
      Pack.ResourcesSupplier resources = id -> new VanillaAssetsPack(VanillaAssets.get());
      Pack.Info info = Pack.readPackInfo(ID, resources);
      if (info != null) {
         consumer.accept(Pack.create(
            ID, Component.literal("Future Backport: Minecraft assets"), true, resources, info, PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, true, PackSource.BUILT_IN
         ));
      }
   };

   private final VanillaAssets assets;
   @Nullable
   private PathPackResources files;

   private VanillaAssetsPack(VanillaAssets assets) {
      this.assets = assets;
   }

   private synchronized PathPackResources files() {
      if (this.files == null) {
         this.assets.awaitDownload();
         this.files = new PathPackResources(ID, this.assets.root(), true);
      }
      return this.files;
   }

   @Nullable
   @Override
   public IoSupplier<InputStream> getRootResource(String... elements) {
      return elements.length == 1 && elements[0].equals("pack.mcmeta") ? () -> new ByteArrayInputStream(packMetadata()) : null;
   }

   @Nullable
   @Override
   public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
      if (type != PackType.CLIENT_RESOURCES || !this.assets.contains("assets/" + location.getNamespace() + "/" + location.getPath())) {
         return null;
      }
      return this.files().getResource(type, location);
   }

   @Override
   public void listResources(PackType type, String namespace, String path, PackResources.ResourceOutput output) {
      if (type == PackType.CLIENT_RESOURCES && this.assets.containsUnder("assets/" + namespace + "/" + path)) {
         this.files().listResources(type, namespace, path, (location, resource) -> {
            if (this.assets.contains("assets/" + location.getNamespace() + "/" + location.getPath())) {
               output.accept(location, resource);
            }
         });
      }
   }

   @Override
   public Set<String> getNamespaces(PackType type) {
      return type == PackType.CLIENT_RESOURCES ? this.assets.namespaces() : Set.of();
   }

   @Nullable
   @Override
   public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
      return AbstractPackResources.getMetadataFromStream(serializer, new ByteArrayInputStream(packMetadata()));
   }

   @Override
   public String packId() {
      return ID;
   }

   @Override
   public boolean isBuiltin() {
      return true;
   }

   @Override
   public synchronized void close() {
      if (this.files != null) {
         this.files.close();
      }
   }

   private static byte[] packMetadata() {
      JsonObject pack = new JsonObject();
      pack.addProperty("description", "Minecraft textures and sounds used by Future Backport, downloaded from Mojang");
      pack.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES));
      JsonObject root = new JsonObject();
      root.add("pack", pack);
      return root.toString().getBytes(StandardCharsets.UTF_8);
   }
}
