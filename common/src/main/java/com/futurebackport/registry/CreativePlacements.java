package com.futurebackport.registry;

import net.minecraft.util.GsonHelper;
import com.futurebackport.FutureBackport;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class CreativePlacements {
   private static List<CreativePlacements.Placement> placements;

   private CreativePlacements() {
   }

   private static List<CreativePlacements.Placement> load() {
      List<CreativePlacements.Placement> list = new ArrayList<>();

      try {
         label63: {
            Object var9;
            try (InputStream stream = CreativePlacements.class.getResourceAsStream("/assets/futurebackport/creative_placements.json")) {
               if (stream != null) {
                  for (JsonElement element : GsonHelper.parseArray(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                     JsonObject entry = element.getAsJsonObject();
                     list.add(
                        new CreativePlacements.Placement(
                           new ResourceLocation(entry.get("tab").getAsString()),
                           new ResourceLocation(entry.get("after").getAsString()),
                           new ResourceLocation(entry.get("item").getAsString())
                        )
                     );
                  }
                  break label63;
               }

               var9 = list;
            }

            return (List<CreativePlacements.Placement>)var9;
         }
      } catch (Exception var8) {
         FutureBackport.LOGGER.error("Could not read creative tab placements", var8);
      }

      return list;
   }

   /** Calls {@code insertAfter(after, item)} for every item this mod slots into an existing creative tab. */
   public static void apply(ResourceKey<CreativeModeTab> tabKey, BiConsumer<ItemStack, ItemStack> insertAfter) {
      if (placements == null) {
         placements = load();
      }

      ResourceLocation tab = tabKey.location();

      for (CreativePlacements.Placement placement : placements) {
         if (placement.tab().equals(tab)) {
            Optional<Item> after = BuiltInRegistries.ITEM.getOptional(placement.after());
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(placement.item());
            if (!after.isEmpty() && !item.isEmpty()) {
               try {
                  insertAfter.accept(new ItemStack((ItemLike)after.get()), new ItemStack((ItemLike)item.get()));
               } catch (IllegalArgumentException ignored) {
                  // The anchor item isn't in this tab (e.g. disabled by another mod); skip it.
               }
            }
         }
      }
   }

   private record Placement(ResourceLocation tab, ResourceLocation after, ResourceLocation item) {
   }
}
