package net.minecraft.util;

import org.jetbrains.annotations.Nullable;

public interface Clearable {
   void clear();

   static void clear(@Nullable Object o) {
      if (o instanceof Clearable) {
         ((Clearable)o).clear();
      }

   }
}
