package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class HorseScreen extends HandledScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/horse.png");
   private final AbstractHorseEntity entity;
   private float mouseX;
   private float mouseY;

   public HorseScreen(HorseScreenHandler handler, PlayerInventory inventory, AbstractHorseEntity entity) {
      super(handler, inventory, entity.getDisplayName());
      this.entity = entity;
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      int k = (this.width - this.backgroundWidth) / 2;
      int l = (this.height - this.backgroundHeight) / 2;
      context.drawTexture(TEXTURE, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      if (this.entity instanceof AbstractDonkeyEntity) {
         AbstractDonkeyEntity lv = (AbstractDonkeyEntity)this.entity;
         if (lv.hasChest()) {
            context.drawTexture(TEXTURE, k + 79, l + 17, 0, this.backgroundHeight, lv.getInventoryColumns() * 18, 54);
         }
      }

      if (this.entity.canBeSaddled()) {
         context.drawTexture(TEXTURE, k + 7, l + 35 - 18, 18, this.backgroundHeight + 54, 18, 18);
      }

      if (this.entity.hasArmorSlot()) {
         if (this.entity instanceof LlamaEntity) {
            context.drawTexture(TEXTURE, k + 7, l + 35, 36, this.backgroundHeight + 54, 18, 18);
         } else {
            context.drawTexture(TEXTURE, k + 7, l + 35, 0, this.backgroundHeight + 54, 18, 18);
         }
      }

      InventoryScreen.drawEntity(context, k + 51, l + 60, 17, (float)(k + 51) - this.mouseX, (float)(l + 75 - 50) - this.mouseY, this.entity);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      this.mouseX = (float)mouseX;
      this.mouseY = (float)mouseY;
      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }
}
