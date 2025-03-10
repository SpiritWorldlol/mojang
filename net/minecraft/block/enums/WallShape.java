package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum WallShape implements StringIdentifiable {
   NONE("none"),
   LOW("low"),
   TALL("tall");

   private final String name;

   private WallShape(String name) {
      this.name = name;
   }

   public String toString() {
      return this.asString();
   }

   public String asString() {
      return this.name;
   }

   // $FF: synthetic method
   private static WallShape[] method_36739() {
      return new WallShape[]{NONE, LOW, TALL};
   }
}
