package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class RealmsClientOutdatedScreen extends RealmsScreen {
   private static final Text INCOMPATIBLE_TITLE = Text.translatable("mco.client.incompatible.title");
   private static final Text[] INCOMPATIBLE_LINES_UNSTABLE = new Text[]{Text.translatable("mco.client.incompatible.msg.line1"), Text.translatable("mco.client.incompatible.msg.line2"), Text.translatable("mco.client.incompatible.msg.line3")};
   private static final Text[] INCOMPATIBLE_LINES = new Text[]{Text.translatable("mco.client.incompatible.msg.line1"), Text.translatable("mco.client.incompatible.msg.line2")};
   private final Screen parent;

   public RealmsClientOutdatedScreen(Screen parent) {
      super(INCOMPATIBLE_TITLE);
      this.parent = parent;
   }

   public void init() {
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 100, row(12), 200, 20).build());
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, row(3), 16711680);
      Text[] lvs = this.getLines();

      for(int k = 0; k < lvs.length; ++k) {
         context.drawCenteredTextWithShadow(this.textRenderer, lvs[k], this.width / 2, row(5) + k * 12, 16777215);
      }

      super.render(context, mouseX, mouseY, delta);
   }

   private Text[] getLines() {
      return SharedConstants.getGameVersion().isStable() ? INCOMPATIBLE_LINES : INCOMPATIBLE_LINES_UNSTABLE;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER && keyCode != GLFW.GLFW_KEY_ESCAPE) {
         return super.keyPressed(keyCode, scanCode, modifiers);
      } else {
         this.client.setScreen(this.parent);
         return true;
      }
   }
}
