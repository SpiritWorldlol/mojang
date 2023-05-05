package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CartographyTableScreen extends HandledScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/cartography_table.png");

   public CartographyTableScreen(CartographyTableScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.titleY -= 2;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      this.renderBackground(context);
      int k = this.x;
      int l = this.y;
      context.drawTexture(TEXTURE, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      ItemStack lv = ((CartographyTableScreenHandler)this.handler).getSlot(1).getStack();
      boolean bl = lv.isOf(Items.MAP);
      boolean bl2 = lv.isOf(Items.PAPER);
      boolean bl3 = lv.isOf(Items.GLASS_PANE);
      ItemStack lv2 = ((CartographyTableScreenHandler)this.handler).getSlot(0).getStack();
      boolean bl4 = false;
      Integer integer;
      MapState lv3;
      if (lv2.isOf(Items.FILLED_MAP)) {
         integer = FilledMapItem.getMapId(lv2);
         lv3 = FilledMapItem.getMapState((Integer)integer, this.client.world);
         if (lv3 != null) {
            if (lv3.locked) {
               bl4 = true;
               if (bl2 || bl3) {
                  context.drawTexture(TEXTURE, k + 35, l + 31, this.backgroundWidth + 50, 132, 28, 21);
               }
            }

            if (bl2 && lv3.scale >= 4) {
               bl4 = true;
               context.drawTexture(TEXTURE, k + 35, l + 31, this.backgroundWidth + 50, 132, 28, 21);
            }
         }
      } else {
         integer = null;
         lv3 = null;
      }

      this.drawMap(context, integer, lv3, bl, bl2, bl3, bl4);
   }

   private void drawMap(DrawContext context, @Nullable Integer mapId, @Nullable MapState mapState, boolean cloneMode, boolean expandMode, boolean lockMode, boolean cannotExpand) {
      int i = this.x;
      int j = this.y;
      if (expandMode && !cannotExpand) {
         context.drawTexture(TEXTURE, i + 67, j + 13, this.backgroundWidth, 66, 66, 66);
         this.drawMap(context, mapId, mapState, i + 85, j + 31, 0.226F);
      } else if (cloneMode) {
         context.drawTexture(TEXTURE, i + 67 + 16, j + 13, this.backgroundWidth, 132, 50, 66);
         this.drawMap(context, mapId, mapState, i + 86, j + 16, 0.34F);
         context.getMatrices().push();
         context.getMatrices().translate(0.0F, 0.0F, 1.0F);
         context.drawTexture(TEXTURE, i + 67, j + 13 + 16, this.backgroundWidth, 132, 50, 66);
         this.drawMap(context, mapId, mapState, i + 70, j + 32, 0.34F);
         context.getMatrices().pop();
      } else if (lockMode) {
         context.drawTexture(TEXTURE, i + 67, j + 13, this.backgroundWidth, 0, 66, 66);
         this.drawMap(context, mapId, mapState, i + 71, j + 17, 0.45F);
         context.getMatrices().push();
         context.getMatrices().translate(0.0F, 0.0F, 1.0F);
         context.drawTexture(TEXTURE, i + 66, j + 12, 0, this.backgroundHeight, 66, 66);
         context.getMatrices().pop();
      } else {
         context.drawTexture(TEXTURE, i + 67, j + 13, this.backgroundWidth, 0, 66, 66);
         this.drawMap(context, mapId, mapState, i + 71, j + 17, 0.45F);
      }

   }

   private void drawMap(DrawContext context, @Nullable Integer mapId, @Nullable MapState mapState, int x, int y, float scale) {
      if (mapId != null && mapState != null) {
         context.getMatrices().push();
         context.getMatrices().translate((float)x, (float)y, 1.0F);
         context.getMatrices().scale(scale, scale, 1.0F);
         this.client.gameRenderer.getMapRenderer().draw(context.getMatrices(), context.getVertexConsumers(), mapId, mapState, true, LightmapTextureManager.MAX_LIGHT_COORDINATE);
         context.draw();
         context.getMatrices().pop();
      }

   }
}
