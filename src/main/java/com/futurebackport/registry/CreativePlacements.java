package com.futurebackport.registry;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

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
                  for (JsonElement element : JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray()) {
                     JsonObject entry = element.getAsJsonObject();
                     list.add(
                        new CreativePlacements.Placement(
                           ResourceLocation.parse(entry.get("tab").getAsString()),
                           ResourceLocation.parse(entry.get("after").getAsString()),
                           ResourceLocation.parse(entry.get("item").getAsString())
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

   public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
      if (placements == null) {
         placements = load();
      }

      ResourceLocation tab = event.getTabKey().location();

      for (CreativePlacements.Placement placement : placements) {
         if (placement.tab().equals(tab)) {
            Optional<Item> after = BuiltInRegistries.ITEM.getOptional(placement.after());
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(placement.item());
            if (!after.isEmpty() && !item.isEmpty()) {
               try {
                  event.insertAfter(new ItemStack((ItemLike)after.get()), new ItemStack((ItemLike)item.get()), TabVisibility.PARENT_AND_SEARCH_TABS);
               } catch (IllegalArgumentException var7) {
               }
            }
         }
      }
   }

   private record Placement(ResourceLocation tab, ResourceLocation after, ResourceLocation item) {
   }
}
