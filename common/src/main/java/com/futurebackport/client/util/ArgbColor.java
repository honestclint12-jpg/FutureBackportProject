package com.futurebackport.client.util;

import net.minecraft.util.FastColor;

/** Splits a packed ARGB color into the float channels 1.20.1 model rendering takes (1.21 passes the int). */
public final class ArgbColor {

   private ArgbColor() {
   }

   public static float red(int argb) {
      return FastColor.ARGB32.red(argb) / 255.0F;
   }

   public static float green(int argb) {
      return FastColor.ARGB32.green(argb) / 255.0F;
   }

   public static float blue(int argb) {
      return FastColor.ARGB32.blue(argb) / 255.0F;
   }

   public static float alpha(int argb) {
      return FastColor.ARGB32.alpha(argb) / 255.0F;
   }

   /** Packs float channels back into ARGB. */
   public static int pack(float red, float green, float blue, float alpha) {
      return FastColor.ARGB32.color((int) (alpha * 255.0F), (int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F));
   }

   /** Channel-wise multiply, like 1.21's ARGB.multiply. */
   public static int multiply(int a, int b) {
      return FastColor.ARGB32.multiply(a, b);
   }
}
