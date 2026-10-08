package com.futurebackport.gametest;

import com.futurebackport.FutureBackport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;

/**
 * Every game test in the mod, for both loaders. NeoForge and Fabric name a @GameTest's structure differently
 * (NeoForge prefixes the holder's namespace, Fabric takes the template as a full id), so the loaders don't register
 * the test classes themselves: they register a @GameTestGenerator that returns {@link #all()}, which reads the vanilla
 * @GameTest annotations and gives every template the futurebackport namespace.
 */
public final class FutureBackportGameTests {
   private static final List<Class<?>> TEST_CLASSES = List.of(
      ContentTests.class,
      CopperGolemTests.class,
      CopperTests.class,
      FarmAnimalTests.class,
      FloraTests.class,
      HappyGhastTests.class,
      NautilusTests.class,
      PaleGardenTests.class,
      PaleOakTests.class,
      ShelfTests.class,
      SpearTests.class,
      WorldgenTests.class
   );

   private FutureBackportGameTests() {
   }

   public static List<TestFunction> all() {
      List<TestFunction> tests = new ArrayList<>();
      for (Class<?> testClass : TEST_CLASSES) {
         Arrays.stream(testClass.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(GameTest.class) && Modifier.isStatic(method.getModifiers()))
            .sorted(Comparator.comparing(Method::getName))
            .forEach(method -> tests.add(toTestFunction(method)));
      }
      return tests;
   }

   private static TestFunction toTestFunction(Method method) {
      GameTest test = method.getAnnotation(GameTest.class);
      String template = test.template().contains(":") ? test.template() : FutureBackport.id(test.template()).toString();
      return new TestFunction(
         test.batch(),
         method.getDeclaringClass().getSimpleName().toLowerCase(Locale.ROOT) + "." + method.getName().toLowerCase(Locale.ROOT),
         template,
         StructureUtils.getRotationForRotationSteps(test.rotationSteps()),
         test.timeoutTicks(),
         test.setupTicks(),
         test.required(),
         test.manualOnly(),
         test.attempts(),
         test.requiredSuccesses(),
         test.skyAccess(),
         helper -> invoke(method, helper)
      );
   }

   private static void invoke(Method method, GameTestHelper helper) {
      try {
         method.invoke(null, helper);
      } catch (InvocationTargetException e) {
         if (e.getCause() instanceof RuntimeException runtime) {
            throw runtime;
         }
         throw new RuntimeException(e.getCause());
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException(e);
      }
   }
}
