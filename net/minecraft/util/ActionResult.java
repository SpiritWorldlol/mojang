package net.minecraft.util;

public enum ActionResult {
   SUCCESS,
   CONSUME,
   CONSUME_PARTIAL,
   PASS,
   FAIL;

   public boolean isAccepted() {
      return this == SUCCESS || this == CONSUME || this == CONSUME_PARTIAL;
   }

   public boolean shouldSwingHand() {
      return this == SUCCESS;
   }

   public boolean shouldIncrementStat() {
      return this == SUCCESS || this == CONSUME;
   }

   public static ActionResult success(boolean swingHand) {
      return swingHand ? SUCCESS : CONSUME;
   }

   // $FF: synthetic method
   private static ActionResult[] method_36599() {
      return new ActionResult[]{SUCCESS, CONSUME, CONSUME_PARTIAL, PASS, FAIL};
   }
}
