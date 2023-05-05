package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class PlayerSkinDrawer {
   public static final int field_39525 = 8;
   public static final int field_39526 = 8;
   public static final int field_39527 = 8;
   public static final int field_39528 = 8;
   public static final int field_39529 = 40;
   public static final int field_39530 = 8;
   public static final int field_39531 = 8;
   public static final int field_39532 = 8;
   public static final int field_39533 = 64;
   public static final int field_39534 = 64;

   public static void draw(DrawContext context, Identifier texture, int x, int y, int size) {
      draw(context, texture, x, y, size, true, false);
   }

   public static void draw(DrawContext context, Identifier texture, int x, int y, int size, boolean hatVisible, boolean upsideDown) {
      int l = 8 + (upsideDown ? 8 : 0);
      int m = 8 * (upsideDown ? -1 : 1);
      context.drawTexture(texture, x, y, size, size, 8.0F, (float)l, 8, m, 64, 64);
      if (hatVisible) {
         drawHat(context, texture, x, y, size, upsideDown);
      }

   }

   private static void drawHat(DrawContext context, Identifier texture, int x, int y, int size, boolean upsideDown) {
      int l = 8 + (upsideDown ? 8 : 0);
      int m = 8 * (upsideDown ? -1 : 1);
      RenderSystem.enableBlend();
      context.drawTexture(texture, x, y, size, size, 40.0F, (float)l, 8, m, 64, 64);
      RenderSystem.disableBlend();
   }
}
