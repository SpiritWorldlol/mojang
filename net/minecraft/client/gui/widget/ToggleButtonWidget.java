package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ToggleButtonWidget extends ClickableWidget {
   protected Identifier texture;
   protected boolean toggled;
   protected int u;
   protected int v;
   protected int pressedUOffset;
   protected int hoverVOffset;

   public ToggleButtonWidget(int x, int y, int width, int height, boolean toggled) {
      super(x, y, width, height, ScreenTexts.EMPTY);
      this.toggled = toggled;
   }

   public void setTextureUV(int u, int v, int pressedUOffset, int hoverVOffset, Identifier texture) {
      this.u = u;
      this.v = v;
      this.pressedUOffset = pressedUOffset;
      this.hoverVOffset = hoverVOffset;
      this.texture = texture;
   }

   public void setToggled(boolean toggled) {
      this.toggled = toggled;
   }

   public boolean isToggled() {
      return this.toggled;
   }

   public void appendClickableNarrations(NarrationMessageBuilder builder) {
      this.appendDefaultNarrations(builder);
   }

   public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
      RenderSystem.disableDepthTest();
      int k = this.u;
      int l = this.v;
      if (this.toggled) {
         k += this.pressedUOffset;
      }

      if (this.isSelected()) {
         l += this.hoverVOffset;
      }

      context.drawTexture(this.texture, this.getX(), this.getY(), k, l, this.width, this.height);
      RenderSystem.enableDepthTest();
   }
}
