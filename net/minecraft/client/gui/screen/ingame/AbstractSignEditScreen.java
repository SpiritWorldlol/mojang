package net.minecraft.client.gui.screen.ingame;

import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public abstract class AbstractSignEditScreen extends Screen {
   private final SignBlockEntity blockEntity;
   private SignText text;
   private final String[] messages;
   private final boolean front;
   protected final WoodType signType;
   private int ticksSinceOpened;
   private int currentRow;
   @Nullable
   private SelectionManager selectionManager;

   public AbstractSignEditScreen(SignBlockEntity blockEntity, boolean front, boolean filtered) {
      this(blockEntity, front, filtered, Text.translatable("sign.edit"));
   }

   public AbstractSignEditScreen(SignBlockEntity blockEntity, boolean front, boolean filtered, Text title) {
      super(title);
      this.blockEntity = blockEntity;
      this.text = blockEntity.getText(front);
      this.front = front;
      this.signType = AbstractSignBlock.getWoodType(blockEntity.getCachedState().getBlock());
      this.messages = (String[])IntStream.range(0, 4).mapToObj((line) -> {
         return this.text.getMessage(line, filtered);
      }).map(Text::getString).toArray((i) -> {
         return new String[i];
      });
   }

   protected void init() {
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.finishEditing();
      }).dimensions(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build());
      this.selectionManager = new SelectionManager(() -> {
         return this.messages[this.currentRow];
      }, this::setCurrentRowMessage, SelectionManager.makeClipboardGetter(this.client), SelectionManager.makeClipboardSetter(this.client), (string) -> {
         return this.client.textRenderer.getWidth(string) <= this.blockEntity.getMaxTextWidth();
      });
   }

   public void tick() {
      ++this.ticksSinceOpened;
      if (!this.canEdit()) {
         this.finishEditing();
      }

   }

   private boolean canEdit() {
      return this.client == null || this.client.player == null || !this.blockEntity.getType().supports(this.blockEntity.getCachedState()) || !this.blockEntity.isPlayerTooFarToEdit(this.client.player.getUuid());
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_UP) {
         this.currentRow = this.currentRow - 1 & 3;
         this.selectionManager.putCursorAtEnd();
         return true;
      } else if (keyCode != GLFW.GLFW_KEY_DOWN && keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
         return this.selectionManager.handleSpecialKey(keyCode) ? true : super.keyPressed(keyCode, scanCode, modifiers);
      } else {
         this.currentRow = this.currentRow + 1 & 3;
         this.selectionManager.putCursorAtEnd();
         return true;
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      this.selectionManager.insert(chr);
      return true;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      DiffuseLighting.disableGuiDepthLighting();
      this.renderBackground(context);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 40, 16777215);
      this.renderSign(context);
      DiffuseLighting.enableGuiDepthLighting();
      super.render(context, mouseX, mouseY, delta);
   }

   public void close() {
      this.finishEditing();
   }

   public void removed() {
      ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
      if (lv != null) {
         lv.sendPacket(new UpdateSignC2SPacket(this.blockEntity.getPos(), this.front, this.messages[0], this.messages[1], this.messages[2], this.messages[3]));
      }

   }

   public boolean shouldPause() {
      return false;
   }

   protected abstract void renderSignBackground(DrawContext context, BlockState state);

   protected abstract Vector3f getTextScale();

   protected void translateForRender(DrawContext context, BlockState state) {
      context.getMatrices().translate((float)this.width / 2.0F, 90.0F, 50.0F);
   }

   private void renderSign(DrawContext context) {
      BlockState lv = this.blockEntity.getCachedState();
      context.getMatrices().push();
      this.translateForRender(context, lv);
      context.getMatrices().push();
      this.renderSignBackground(context, lv);
      context.getMatrices().pop();
      this.renderSignText(context);
      context.getMatrices().pop();
   }

   private void renderSignText(DrawContext context) {
      context.getMatrices().translate(0.0F, 0.0F, 4.0F);
      Vector3f vector3f = this.getTextScale();
      context.getMatrices().scale(vector3f.x(), vector3f.y(), vector3f.z());
      int i = this.text.getColor().getSignColor();
      boolean bl = this.ticksSinceOpened / 6 % 2 == 0;
      int j = this.selectionManager.getSelectionStart();
      int k = this.selectionManager.getSelectionEnd();
      int l = 4 * this.blockEntity.getTextLineHeight() / 2;
      int m = this.currentRow * this.blockEntity.getTextLineHeight() - l;

      int n;
      String string;
      int o;
      int p;
      int q;
      for(n = 0; n < this.messages.length; ++n) {
         string = this.messages[n];
         if (string != null) {
            if (this.textRenderer.isRightToLeft()) {
               string = this.textRenderer.mirror(string);
            }

            o = -this.textRenderer.getWidth(string) / 2;
            context.drawText(this.textRenderer, string, o, n * this.blockEntity.getTextLineHeight() - l, i, false);
            if (n == this.currentRow && j >= 0 && bl) {
               p = this.textRenderer.getWidth(string.substring(0, Math.max(Math.min(j, string.length()), 0)));
               q = p - this.textRenderer.getWidth(string) / 2;
               if (j >= string.length()) {
                  context.drawText(this.textRenderer, "_", q, m, i, false);
               }
            }
         }
      }

      for(n = 0; n < this.messages.length; ++n) {
         string = this.messages[n];
         if (string != null && n == this.currentRow && j >= 0) {
            o = this.textRenderer.getWidth(string.substring(0, Math.max(Math.min(j, string.length()), 0)));
            p = o - this.textRenderer.getWidth(string) / 2;
            if (bl && j < string.length()) {
               context.fill(p, m - 1, p + 1, m + this.blockEntity.getTextLineHeight(), -16777216 | i);
            }

            if (k != j) {
               q = Math.min(j, k);
               int r = Math.max(j, k);
               int s = this.textRenderer.getWidth(string.substring(0, q)) - this.textRenderer.getWidth(string) / 2;
               int t = this.textRenderer.getWidth(string.substring(0, r)) - this.textRenderer.getWidth(string) / 2;
               int u = Math.min(s, t);
               int v = Math.max(s, t);
               context.method_51739(RenderLayer.method_51786(), u, m, v, m + this.blockEntity.getTextLineHeight(), -16776961);
            }
         }
      }

   }

   private void setCurrentRowMessage(String message) {
      this.messages[this.currentRow] = message;
      this.text = this.text.withMessage(this.currentRow, Text.literal(message));
      this.blockEntity.setText(this.text, this.front);
   }

   private void finishEditing() {
      this.client.setScreen((Screen)null);
   }
}
