package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class DatapackFailureScreen extends Screen {
   private MultilineText wrappedText;
   private final Runnable runServerInSafeMode;

   public DatapackFailureScreen(Runnable runServerInSafeMode) {
      super(Text.translatable("datapackFailure.title"));
      this.wrappedText = MultilineText.EMPTY;
      this.runServerInSafeMode = runServerInSafeMode;
   }

   protected void init() {
      super.init();
      this.wrappedText = MultilineText.create(this.textRenderer, this.getTitle(), this.width - 50);
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("datapackFailure.safeMode"), (button) -> {
         this.runServerInSafeMode.run();
      }).dimensions(this.width / 2 - 155, this.height / 6 + 96, 150, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.TO_TITLE, (button) -> {
         this.client.setScreen((Screen)null);
      }).dimensions(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20).build());
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      this.wrappedText.drawCenterWithShadow(context, this.width / 2, 70);
      super.render(context, mouseX, mouseY, delta);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }
}
