package com.futurebackport.fabric;

import com.futurebackport.gametest.FutureBackportGameTests;
import java.util.Collection;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;

/** Registers the shared game tests (common/.../gametest) through the fabric-gametest entrypoint. */
public final class FabricGameTests {

    @GameTestGenerator
    public Collection<TestFunction> tests() {
        return FutureBackportGameTests.all();
    }
}
