package com.futurebackport.mixin.client;

import com.futurebackport.client.assets.VanillaAssetsPack;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the downloaded Minecraft assets pack to the client's resource packs (not to data pack repositories). */
@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
   @Shadow
   @Final
   @Mutable
   private Set<RepositorySource> sources;

   @Inject(method = "<init>", at = @At("RETURN"))
   private void futurebackport$addVanillaAssets(RepositorySource[] repositorySources, CallbackInfo ci) {
      if (this.sources.stream().anyMatch(source -> source instanceof ClientPackSource)) {
         Set<RepositorySource> sources = new LinkedHashSet<>(this.sources);
         sources.add(VanillaAssetsPack.SOURCE);
         this.sources = sources;
      }
   }
}
