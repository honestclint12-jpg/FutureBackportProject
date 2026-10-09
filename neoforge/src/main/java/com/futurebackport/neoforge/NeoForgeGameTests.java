package com.futurebackport.neoforge;

import com.futurebackport.gametest.FutureBackportGameTests;
import java.util.Collection;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Registers the shared game tests (common/.../gametest) with NeoForge. */
@GameTestHolder("futurebackport")
public final class NeoForgeGameTests {

    @GameTestGenerator
    public static Collection<TestFunction> tests() {
        return FutureBackportGameTests.all();
    }
}
