package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class GenericContainerScreen extends HandledScreen implements ScreenHandlerProvider {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/generic_54.png");
   private final int rows;

   public GenericContainerScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      int i = true;
      int j = true;
      this.rows = handler.getRows();
      this.backgroundHeight = 114 + this.rows * 18;
      this.playerInventoryTitleY = this.backgroundHeight - 94;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      int k = (this.width - this.backgroundWidth) / 2;
      int l = (this.height - this.backgroundHeight) / 2;
      context.drawTexture(TEXTURE, k, l, 0, 0, this.backgroundWidth, this.rows * 18 + 17);
      context.drawTexture(TEXTURE, k, l + this.rows * 18 + 17, 0, 126, this.backgroundWidth, 96);
   }
}
