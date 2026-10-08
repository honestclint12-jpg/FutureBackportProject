package com.futurebackport.client;

import com.futurebackport.FutureBackport;
import java.io.IOException;
import net.minecraft.client.resources.LegacyStuffWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome.ClimateSettings;

public final class DryFoliageColor {
   public static final int DEFAULT = -10732494;
   private static final ResourceLocation LOCATION = FutureBackport.id("textures/colormap/dry_foliage.png");
   private static int[] pixels = new int[65536];
   public static final ColorResolver RESOLVER = (biome, x, z) -> {
      ClimateSettings climate = biome.getModifiedClimateSettings();
      return get(Mth.clamp(climate.temperature(), 0.0F, 1.0F), Mth.clamp(climate.downfall(), 0.0F, 1.0F));
   };

   private DryFoliageColor() {
   }

   public static int get(double temperature, double downfall) {
      downfall *= temperature;
      int i = (int)((1.0 - temperature) * 255.0);
      int j = (int)((1.0 - downfall) * 255.0);
      int index = j << 8 | i;
      return index >= pixels.length ? -10732494 : pixels[index];
   }

   public static final class ReloadListener extends SimplePreparableReloadListener<int[]> {
      protected int[] prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
         try {
            return LegacyStuffWrapper.getPixels(resourceManager, DryFoliageColor.LOCATION);
         } catch (IOException var4) {
            throw new IllegalStateException("Failed to load dry foliage color texture", var4);
         }
      }

      protected void apply(int[] loaded, ResourceManager resourceManager, ProfilerFiller profiler) {
         DryFoliageColor.pixels = loaded;
      }
   }
}
