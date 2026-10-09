package com.futurebackport.registry;

import com.futurebackport.FutureBackport;
import net.minecraft.world.entity.vehicle.Boat;

/**
 * Boat types are an enum in 1.21.1, so each loader has to add the pale oak constant itself
 * (NeoForge: enum extension in META-INF/enumextensions.json).
 */
public final class ModBoats {
   public static final String PALE_OAK_NAME = "futurebackport:pale_oak";
   private static Boat.Type paleOak;

   private ModBoats() {
   }

   public static Boat.Type paleOak() {
      if (paleOak == null) {
         paleOak = find(PALE_OAK_NAME);
      }
      return paleOak;
   }

   private static Boat.Type find(String name) {
      for (Boat.Type type : Boat.Type.values()) {
         if (type.getName().equals(name)) {
            return type;
         }
      }
      FutureBackport.LOGGER.warn("Boat type {} was not added on this loader; pale oak boats will look like oak boats", name);
      return Boat.Type.OAK;
   }
}
