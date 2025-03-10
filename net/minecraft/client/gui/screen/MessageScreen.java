package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class MessageScreen extends Screen {
   public MessageScreen(Text arg) {
      super(arg);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackgroundTexture(context);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 70, 16777215);
      super.render(context, mouseX, mouseY, delta);
   }
}
