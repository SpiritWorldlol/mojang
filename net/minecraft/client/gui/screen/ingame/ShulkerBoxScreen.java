package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ShulkerBoxScreen extends HandledScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/shulker_box.png");

   public ShulkerBoxScreen(ShulkerBoxScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      ++this.backgroundHeight;
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
   }
}
