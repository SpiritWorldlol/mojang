package net.minecraft.entity.passive;

import java.util.function.IntFunction;
import net.minecraft.util.function.ValueLists;

public enum HorseMarking {
   NONE(0),
   WHITE(1),
   WHITE_FIELD(2),
   WHITE_DOTS(3),
   BLACK_DOTS(4);

   private static final IntFunction BY_ID = ValueLists.createIdToValueFunction(HorseMarking::getId, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.WRAP);
   private final int id;

   private HorseMarking(int id) {
      this.id = id;
   }

   public int getId() {
      return this.id;
   }

   public static HorseMarking byIndex(int index) {
      return (HorseMarking)BY_ID.apply(index);
   }

   // $FF: synthetic method
   private static HorseMarking[] method_36645() {
      return new HorseMarking[]{NONE, WHITE, WHITE_FIELD, WHITE_DOTS, BLACK_DOTS};
   }
}
