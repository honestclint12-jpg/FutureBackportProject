package com.futurebackport.block;

import net.minecraft.util.StringRepresentable;

public enum CreakingHeartState implements StringRepresentable {
   UPROOTED("uprooted"),
   DORMANT("dormant"),
   AWAKE("awake");

   private final String name;

   private CreakingHeartState(String name) {
      this.name = name;
   }

   public String getSerializedName() {
      return this.name;
   }

   @Override
   public String toString() {
      return this.name;
   }
}
