package net.minecraft.item.map;

import java.util.Objects;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class MapIcon {
   private final Type type;
   private final byte x;
   private final byte z;
   private final byte rotation;
   @Nullable
   private final Text text;

   public MapIcon(Type type, byte x, byte z, byte rotation, @Nullable Text text) {
      this.type = type;
      this.x = x;
      this.z = z;
      this.rotation = rotation;
      this.text = text;
   }

   public byte getTypeId() {
      return this.type.getId();
   }

   public Type getType() {
      return this.type;
   }

   public byte getX() {
      return this.x;
   }

   public byte getZ() {
      return this.z;
   }

   public byte getRotation() {
      return this.rotation;
   }

   public boolean isAlwaysRendered() {
      return this.type.isAlwaysRendered();
   }

   @Nullable
   public Text getText() {
      return this.text;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof MapIcon)) {
         return false;
      } else {
         MapIcon lv = (MapIcon)o;
         return this.type == lv.type && this.rotation == lv.rotation && this.x == lv.x && this.z == lv.z && Objects.equals(this.text, lv.text);
      }
   }

   public int hashCode() {
      int i = this.type.getId();
      i = 31 * i + this.x;
      i = 31 * i + this.z;
      i = 31 * i + this.rotation;
      i = 31 * i + Objects.hashCode(this.text);
      return i;
   }

   public static enum Type {
      PLAYER(false, true),
      FRAME(true, true),
      RED_MARKER(false, true),
      BLUE_MARKER(false, true),
      TARGET_X(true, false),
      TARGET_POINT(true, false),
      PLAYER_OFF_MAP(false, true),
      PLAYER_OFF_LIMITS(false, true),
      MANSION(true, 5393476, false),
      MONUMENT(true, 3830373, false),
      BANNER_WHITE(true, true),
      BANNER_ORANGE(true, true),
      BANNER_MAGENTA(true, true),
      BANNER_LIGHT_BLUE(true, true),
      BANNER_YELLOW(true, true),
      BANNER_LIME(true, true),
      BANNER_PINK(true, true),
      BANNER_GRAY(true, true),
      BANNER_LIGHT_GRAY(true, true),
      BANNER_CYAN(true, true),
      BANNER_PURPLE(true, true),
      BANNER_BLUE(true, true),
      BANNER_BROWN(true, true),
      BANNER_GREEN(true, true),
      BANNER_RED(true, true),
      BANNER_BLACK(true, true),
      RED_X(true, false);

      private final byte id;
      private final boolean alwaysRender;
      private final int tintColor;
      private final boolean useIconCountLimit;

      private Type(boolean alwaysRender, boolean useIconCountLimit) {
         this(alwaysRender, -1, useIconCountLimit);
      }

      private Type(boolean alwaysRender, int tintColor, boolean useIconCountLimit) {
         this.useIconCountLimit = useIconCountLimit;
         this.id = (byte)this.ordinal();
         this.alwaysRender = alwaysRender;
         this.tintColor = tintColor;
      }

      public byte getId() {
         return this.id;
      }

      public boolean isAlwaysRendered() {
         return this.alwaysRender;
      }

      public boolean hasTintColor() {
         return this.tintColor >= 0;
      }

      public int getTintColor() {
         return this.tintColor;
      }

      public static Type byId(byte id) {
         return values()[MathHelper.clamp(id, 0, values().length - 1)];
      }

      public boolean shouldUseIconCountLimit() {
         return this.useIconCountLimit;
      }

      // $FF: synthetic method
      private static Type[] method_36790() {
         return new Type[]{PLAYER, FRAME, RED_MARKER, BLUE_MARKER, TARGET_X, TARGET_POINT, PLAYER_OFF_MAP, PLAYER_OFF_LIMITS, MANSION, MONUMENT, BANNER_WHITE, BANNER_ORANGE, BANNER_MAGENTA, BANNER_LIGHT_BLUE, BANNER_YELLOW, BANNER_LIME, BANNER_PINK, BANNER_GRAY, BANNER_LIGHT_GRAY, BANNER_CYAN, BANNER_PURPLE, BANNER_BLUE, BANNER_BROWN, BANNER_GREEN, BANNER_RED, BANNER_BLACK, RED_X};
      }
   }
}
