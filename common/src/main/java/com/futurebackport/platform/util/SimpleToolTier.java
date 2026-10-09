package com.futurebackport.platform.util;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Plain {@link Tier} implementation (Forge's {@code ForgeTier} is not available on other loaders). */
public record SimpleToolTier(int level, int uses, float speed, float attackDamageBonus,
                             int enchantmentValue, Supplier<Ingredient> repairIngredientSupplier) implements Tier {

    public SimpleToolTier {
        repairIngredientSupplier = Suppliers.memoize(repairIngredientSupplier::get);
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamageBonus;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredientSupplier.get();
    }
}
