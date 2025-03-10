package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class NarratedMultilineTextWidget extends MultilineTextWidget {
   private static final int FOCUSED_BORDER_COLOR = -1;
   private static final int UNFOCUSED_BORDER_COLOR = -6250336;
   private static final int BACKGROUND_COLOR = 1426063360;
   private static final int EXPANSION = 3;
   private static final int BORDER_WIDTH = 1;

   public NarratedMultilineTextWidget(TextRenderer textRenderer, Text text, int width) {
      super(text, textRenderer);
      this.setMaxWidth(width);
      this.setCentered(true);
      this.active = true;
   }

   protected void appendClickableNarrations(NarrationMessageBuilder builder) {
      builder.put(NarrationPart.TITLE, this.getMessage());
   }

   public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
      int k = this.getX() - 3;
      int l = this.getY() - 3;
      int m = this.getX() + this.getWidth() + 3;
      int n = this.getY() + this.getHeight() + 3;
      int o = this.isFocused() ? -1 : -6250336;
      context.fill(k - 1, l - 1, k, n + 1, o);
      context.fill(m, l - 1, m + 1, n + 1, o);
      context.fill(k, l, m, l - 1, o);
      context.fill(k, n, m, n + 1, o);
      context.fill(k, l, m, n, 1426063360);
      super.renderButton(context, mouseX, mouseY, delta);
   }

   public void playDownSound(SoundManager soundManager) {
   }
}
