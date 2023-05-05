package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class InventoryScreen extends AbstractInventoryScreen implements RecipeBookProvider {
   private static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier("textures/gui/recipe_button.png");
   private float mouseX;
   private float mouseY;
   private final RecipeBookWidget recipeBook = new RecipeBookWidget();
   private boolean narrow;
   private boolean mouseDown;

   public InventoryScreen(PlayerEntity player) {
      super(player.playerScreenHandler, player.getInventory(), Text.translatable("container.crafting"));
      this.titleX = 97;
   }

   public void handledScreenTick() {
      if (this.client.interactionManager.hasCreativeInventory()) {
         this.client.setScreen(new CreativeInventoryScreen(this.client.player, this.client.player.networkHandler.getEnabledFeatures(), (Boolean)this.client.options.getOperatorItemsTab().getValue()));
      } else {
         this.recipeBook.update();
      }
   }

   protected void init() {
      if (this.client.interactionManager.hasCreativeInventory()) {
         this.client.setScreen(new CreativeInventoryScreen(this.client.player, this.client.player.networkHandler.getEnabledFeatures(), (Boolean)this.client.options.getOperatorItemsTab().getValue()));
      } else {
         super.init();
         this.narrow = this.width < 379;
         this.recipeBook.initialize(this.width, this.height, this.client, this.narrow, (AbstractRecipeScreenHandler)this.handler);
         this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
         this.addDrawableChild(new TexturedButtonWidget(this.x + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (button) -> {
            this.recipeBook.toggleOpen();
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            button.setPosition(this.x + 104, this.height / 2 - 22);
            this.mouseDown = true;
         }));
         this.addSelectableChild(this.recipeBook);
         this.setInitialFocus(this.recipeBook);
      }
   }

   protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
      context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 4210752, false);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      if (this.recipeBook.isOpen() && this.narrow) {
         this.drawBackground(context, delta, mouseX, mouseY);
         this.recipeBook.render(context, mouseX, mouseY, delta);
      } else {
         this.recipeBook.render(context, mouseX, mouseY, delta);
         super.render(context, mouseX, mouseY, delta);
         this.recipeBook.drawGhostSlots(context, this.x, this.y, false, delta);
      }

      this.drawMouseoverTooltip(context, mouseX, mouseY);
      this.recipeBook.drawTooltip(context, this.x, this.y, mouseX, mouseY);
      this.mouseX = (float)mouseX;
      this.mouseY = (float)mouseY;
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      int k = this.x;
      int l = this.y;
      context.drawTexture(BACKGROUND_TEXTURE, k, l, 0, 0, this.backgroundWidth, this.backgroundHeight);
      drawEntity(context, k + 51, l + 75, 30, (float)(k + 51) - this.mouseX, (float)(l + 75 - 50) - this.mouseY, this.client.player);
   }

   public static void drawEntity(DrawContext context, int x, int y, int size, float mouseX, float mouseY, LivingEntity entity) {
      float h = (float)Math.atan((double)(mouseX / 40.0F));
      float l = (float)Math.atan((double)(mouseY / 40.0F));
      Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
      Quaternionf quaternionf2 = (new Quaternionf()).rotateX(l * 20.0F * 0.017453292F);
      quaternionf.mul(quaternionf2);
      float m = entity.bodyYaw;
      float n = entity.getYaw();
      float o = entity.getPitch();
      float p = entity.prevHeadYaw;
      float q = entity.headYaw;
      entity.bodyYaw = 180.0F + h * 20.0F;
      entity.setYaw(180.0F + h * 40.0F);
      entity.setPitch(-l * 20.0F);
      entity.headYaw = entity.getYaw();
      entity.prevHeadYaw = entity.getYaw();
      drawEntity(context, x, y, size, quaternionf, quaternionf2, entity);
      entity.bodyYaw = m;
      entity.setYaw(n);
      entity.setPitch(o);
      entity.prevHeadYaw = p;
      entity.headYaw = q;
   }

   public static void drawEntity(DrawContext context, int x, int y, int size, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity entity) {
      context.getMatrices().push();
      context.getMatrices().translate((double)x, (double)y, 50.0);
      context.getMatrices().multiplyPositionMatrix((new Matrix4f()).scaling((float)size, (float)size, (float)(-size)));
      context.getMatrices().multiply(quaternionf);
      DiffuseLighting.method_34742();
      EntityRenderDispatcher lv = MinecraftClient.getInstance().getEntityRenderDispatcher();
      if (quaternionf2 != null) {
         quaternionf2.conjugate();
         lv.setRotation(quaternionf2);
      }

      lv.setRenderShadows(false);
      RenderSystem.runAsFancy(() -> {
         lv.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, context.getMatrices(), context.getVertexConsumers(), 15728880);
      });
      context.draw();
      lv.setRenderShadows(true);
      context.getMatrices().pop();
      DiffuseLighting.enableGuiDepthLighting();
   }

   protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
      return (!this.narrow || !this.recipeBook.isOpen()) && super.isPointWithinBounds(x, y, width, height, pointX, pointY);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
         this.setFocused(this.recipeBook);
         return true;
      } else {
         return this.narrow && this.recipeBook.isOpen() ? false : super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.mouseDown) {
         this.mouseDown = false;
         return true;
      } else {
         return super.mouseReleased(mouseX, mouseY, button);
      }
   }

   protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
      boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
      return this.recipeBook.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.backgroundWidth, this.backgroundHeight, button) && bl;
   }

   protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
      super.onMouseClick(slot, slotId, button, actionType);
      this.recipeBook.slotClicked(slot);
   }

   public void refreshRecipeBook() {
      this.recipeBook.refresh();
   }

   public RecipeBookWidget getRecipeBookWidget() {
      return this.recipeBook;
   }
}
