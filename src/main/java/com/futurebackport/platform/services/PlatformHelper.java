package com.futurebackport.platform.services;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/** Small loader differences that don't deserve their own service. */
public interface PlatformHelper {

    /** "neoforge", "fabric" or "forge". */
    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    /**
     * A spawn egg for an entity type that may not be registered yet when the item is created
     * (NeoForge registers items before entity types).
     */
    SpawnEggItem spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties properties);
}
