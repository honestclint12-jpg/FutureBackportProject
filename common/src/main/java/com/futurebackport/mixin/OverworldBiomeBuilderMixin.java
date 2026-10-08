package com.futurebackport.mixin;

import com.futurebackport.FutureBackport;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({OverworldBiomeBuilder.class})
public abstract class OverworldBiomeBuilderMixin {
   private static final ResourceKey<Biome> FUTUREBACKPORT$PALE_GARDEN = ResourceKey.create(Registries.BIOME, FutureBackport.id("pale_garden"));
   @Shadow
   @Final
   private ResourceKey<Biome>[][] PLATEAU_BIOMES;

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void futurebackport$addPaleGarden(CallbackInfo ci) {
      boolean builtInBootstrap = StackWalker.getInstance()
         .walk(frames -> frames.anyMatch(f -> f.getClassName().equals("net.minecraft.data.registries.VanillaRegistries")));
      if (!builtInBootstrap) {
         this.PLATEAU_BIOMES[2][4] = FUTUREBACKPORT$PALE_GARDEN;
      }
   }
}
