package net.minecraft.client.gui.screen.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class GameOptionsScreen extends Screen {
   protected final Screen parent;
   protected final GameOptions gameOptions;

   public GameOptionsScreen(Screen parent, GameOptions gameOptions, Text title) {
      super(title);
      this.parent = parent;
      this.gameOptions = gameOptions;
   }

   public void removed() {
      this.client.options.write();
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   protected void render(DrawContext context, OptionListWidget optionButtons, int mouseX, int mouseY, float tickDelta) {
      this.renderBackground(context);
      optionButtons.render(context, mouseX, mouseY, tickDelta);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 20, 16777215);
      super.render(context, mouseX, mouseY, tickDelta);
   }
}
