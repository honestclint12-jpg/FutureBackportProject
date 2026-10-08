package com.futurebackport.neoforge.platform;

import com.futurebackport.platform.services.PlatformHelper;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public final class NeoForgePlatformHelper implements PlatformHelper {

    @Override
    public String getPlatformName() {
        return "neoforge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public SpawnEggItem spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties properties) {
        return new DeferredSpawnEggItem(type, backgroundColor, highlightColor, properties);
    }
}
