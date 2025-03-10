package net.minecraft.util.collection;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class Weight {
   public static final Codec CODEC;
   private static final Weight DEFAULT;
   private static final Logger LOGGER;
   private final int value;

   private Weight(int weight) {
      this.value = weight;
   }

   public static Weight of(int weight) {
      if (weight == 1) {
         return DEFAULT;
      } else {
         validate(weight);
         return new Weight(weight);
      }
   }

   public int getValue() {
      return this.value;
   }

   private static void validate(int weight) {
      if (weight < 0) {
         throw (IllegalArgumentException)Util.throwOrPause(new IllegalArgumentException("Weight should be >= 0"));
      } else {
         if (weight == 0 && SharedConstants.isDevelopment) {
            LOGGER.warn("Found 0 weight, make sure this is intentional!");
         }

      }
   }

   public String toString() {
      return Integer.toString(this.value);
   }

   public int hashCode() {
      return Integer.hashCode(this.value);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof Weight && this.value == ((Weight)o).value;
      }
   }

   static {
      CODEC = Codec.INT.xmap(Weight::of, Weight::getValue);
      DEFAULT = new Weight(1);
      LOGGER = LogUtils.getLogger();
   }
}
