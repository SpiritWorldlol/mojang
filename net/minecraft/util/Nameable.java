package net.minecraft.util;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface Nameable {
   Text getName();

   default boolean hasCustomName() {
      return this.getCustomName() != null;
   }

   default Text getDisplayName() {
      return this.getName();
   }

   @Nullable
   default Text getCustomName() {
      return null;
   }
}
