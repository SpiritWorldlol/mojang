package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

@Environment(EnvType.CLIENT)
public class BundleTooltipComponent implements TooltipComponent {
   public static final Identifier TEXTURE = new Identifier("textures/gui/container/bundle.png");
   private static final int field_32381 = 4;
   private static final int field_32382 = 1;
   private static final int TEXTURE_SIZE = 128;
   private static final int WIDTH_PER_COLUMN = 18;
   private static final int HEIGHT_PER_ROW = 20;
   private final DefaultedList inventory;
   private final int occupancy;

   public BundleTooltipComponent(BundleTooltipData data) {
      this.inventory = data.getInventory();
      this.occupancy = data.getBundleOccupancy();
   }

   public int getHeight() {
      return this.getRows() * 20 + 2 + 4;
   }

   public int getWidth(TextRenderer textRenderer) {
      return this.getColumns() * 18 + 2;
   }

   public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
      int k = this.getColumns();
      int l = this.getRows();
      boolean bl = this.occupancy >= 64;
      int m = 0;

      for(int n = 0; n < l; ++n) {
         for(int o = 0; o < k; ++o) {
            int p = x + o * 18 + 1;
            int q = y + n * 20 + 1;
            this.drawSlot(p, q, m++, bl, context, textRenderer);
         }
      }

      this.drawOutline(x, y, k, l, context);
   }

   private void drawSlot(int x, int y, int index, boolean shouldBlock, DrawContext context, TextRenderer textRenderer) {
      if (index >= this.inventory.size()) {
         this.draw(context, x, y, shouldBlock ? BundleTooltipComponent.Sprite.BLOCKED_SLOT : BundleTooltipComponent.Sprite.SLOT);
      } else {
         ItemStack lv = (ItemStack)this.inventory.get(index);
         this.draw(context, x, y, BundleTooltipComponent.Sprite.SLOT);
         context.drawItem(lv, x + 1, y + 1, index);
         context.drawItemInSlot(textRenderer, lv, x + 1, y + 1);
         if (index == 0) {
            HandledScreen.drawSlotHighlight(context, x + 1, y + 1, 0);
         }

      }
   }

   private void drawOutline(int x, int y, int columns, int rows, DrawContext context) {
      this.draw(context, x, y, BundleTooltipComponent.Sprite.BORDER_CORNER_TOP);
      this.draw(context, x + columns * 18 + 1, y, BundleTooltipComponent.Sprite.BORDER_CORNER_TOP);

      int m;
      for(m = 0; m < columns; ++m) {
         this.draw(context, x + 1 + m * 18, y, BundleTooltipComponent.Sprite.BORDER_HORIZONTAL_TOP);
         this.draw(context, x + 1 + m * 18, y + rows * 20, BundleTooltipComponent.Sprite.BORDER_HORIZONTAL_BOTTOM);
      }

      for(m = 0; m < rows; ++m) {
         this.draw(context, x, y + m * 20 + 1, BundleTooltipComponent.Sprite.BORDER_VERTICAL);
         this.draw(context, x + columns * 18 + 1, y + m * 20 + 1, BundleTooltipComponent.Sprite.BORDER_VERTICAL);
      }

      this.draw(context, x, y + rows * 20, BundleTooltipComponent.Sprite.BORDER_CORNER_BOTTOM);
      this.draw(context, x + columns * 18 + 1, y + rows * 20, BundleTooltipComponent.Sprite.BORDER_CORNER_BOTTOM);
   }

   private void draw(DrawContext context, int x, int y, Sprite sprite) {
      context.drawTexture(TEXTURE, x, y, 0, (float)sprite.u, (float)sprite.v, sprite.width, sprite.height, 128, 128);
   }

   private int getColumns() {
      return Math.max(2, (int)Math.ceil(Math.sqrt((double)this.inventory.size() + 1.0)));
   }

   private int getRows() {
      return (int)Math.ceil(((double)this.inventory.size() + 1.0) / (double)this.getColumns());
   }

   @Environment(EnvType.CLIENT)
   private static enum Sprite {
      SLOT(0, 0, 18, 20),
      BLOCKED_SLOT(0, 40, 18, 20),
      BORDER_VERTICAL(0, 18, 1, 20),
      BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
      BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
      BORDER_CORNER_TOP(0, 20, 1, 1),
      BORDER_CORNER_BOTTOM(0, 60, 1, 1);

      public final int u;
      public final int v;
      public final int width;
      public final int height;

      private Sprite(int u, int v, int width, int height) {
         this.u = u;
         this.v = v;
         this.width = width;
         this.height = height;
      }

      // $FF: synthetic method
      private static Sprite[] method_36887() {
         return new Sprite[]{SLOT, BLOCKED_SLOT, BORDER_VERTICAL, BORDER_HORIZONTAL_TOP, BORDER_HORIZONTAL_BOTTOM, BORDER_CORNER_TOP, BORDER_CORNER_BOTTOM};
      }
   }
}
