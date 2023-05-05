package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BrewingStandScreen extends HandledScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/brewing_stand.png");
   private static final int[] BUBBLE_PROGRESS = new int[]{29, 24, 20, 16, 11, 6, 0};

   public BrewingStandScreen(BrewingStandScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
   }

   protected void init() {
      super.init();
      this.titleX = (this.backgroundWidth - this.textRenderer.getWidth((StringVisitable)this.title)) / 2;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      int k = (this.width - this.backgroundWidth) / 2;
      int l = (this.height - this.backgroundHeight) / 2;
      context.drawTexture(TEXTURE, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      int m = ((BrewingStandScreenHandler)this.handler).getFuel();
      int n = MathHelper.clamp((18 * m + 20 - 1) / 20, 0, 18);
      if (n > 0) {
         context.drawTexture(TEXTURE, k + 60, l + 44, 176, 29, n, 4);
      }

      int o = ((BrewingStandScreenHandler)this.handler).getBrewTime();
      if (o > 0) {
         int p = (int)(28.0F * (1.0F - (float)o / 400.0F));
         if (p > 0) {
            context.drawTexture(TEXTURE, k + 97, l + 16, 176, 0, 9, p);
         }

         p = BUBBLE_PROGRESS[o / 2 % 7];
         if (p > 0) {
            context.drawTexture(TEXTURE, k + 63, l + 14 + 29 - p, 185, 29 - p, 12, p);
         }
      }

   }
}
