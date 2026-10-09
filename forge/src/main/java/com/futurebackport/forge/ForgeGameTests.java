package com.futurebackport.forge;

import com.futurebackport.FutureBackport;
import com.futurebackport.gametest.FutureBackportGameTests;
import java.util.Collection;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.GameTestHolder;

/** Registers the shared game tests (common/.../gametest) with Forge. */
@GameTestHolder(FutureBackport.MODID)
public final class ForgeGameTests {

    @GameTestGenerator
    public static Collection<TestFunction> tests() {
        return FutureBackportGameTests.all();
    }
}
