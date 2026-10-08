package com.futurebackport.registry;

import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;

import com.futurebackport.menu.NautilusMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public class ModMenus {
   public static final RegistrationProvider<MenuType<?>> MENUS = RegistrationProvider.create(Registries.MENU, "futurebackport");
   public static final RegistryEntry<MenuType<?>, MenuType<NautilusMenu>> NAUTILUS = MENUS.register(
      "nautilus", () -> IMenuTypeExtension.create(NautilusMenu::fromNetwork)
   );
}
