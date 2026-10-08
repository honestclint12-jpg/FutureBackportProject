package com.futurebackport.registry;

import com.futurebackport.menu.NautilusMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "futurebackport");
   public static final DeferredHolder<MenuType<?>, MenuType<NautilusMenu>> NAUTILUS = MENUS.register(
      "nautilus", () -> IMenuTypeExtension.create(NautilusMenu::fromNetwork)
   );
}
