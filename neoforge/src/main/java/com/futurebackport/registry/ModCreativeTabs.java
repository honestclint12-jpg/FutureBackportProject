package com.futurebackport.registry;

import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ModCreativeTabs {
   public static final RegistrationProvider<CreativeModeTab> CREATIVE_MODE_TABS = RegistrationProvider.create(Registries.CREATIVE_MODE_TAB, "futurebackport");
   public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register(
      "main",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.futurebackport"))
         .icon(() -> new ItemStack(Items.CLOCK))
         .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(item -> output.accept((ItemLike)item.get())))
         .build()
   );
}
