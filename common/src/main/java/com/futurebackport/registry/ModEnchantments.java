package com.futurebackport.registry;

import com.futurebackport.item.LungeEnchantment;
import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {
   public static final RegistrationProvider<Enchantment> ENCHANTMENTS = RegistrationProvider.create(Registries.ENCHANTMENT, "futurebackport");
   public static final RegistryEntry<Enchantment, LungeEnchantment> LUNGE = ENCHANTMENTS.register("lunge", LungeEnchantment::new);
}
