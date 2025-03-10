package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public abstract class SliderWidget extends ClickableWidget {
   private static final Identifier TEXTURE = new Identifier("textures/gui/slider.png");
   protected static final int field_43051 = 200;
   protected static final int field_43052 = 20;
   protected static final int field_43102 = 20;
   protected static final int field_43103 = 4;
   protected static final int field_43054 = 2;
   private static final int field_41788 = 20;
   private static final int field_41789 = 4;
   private static final int field_41790 = 8;
   private static final int field_41792 = 0;
   private static final int field_41793 = 1;
   private static final int field_41794 = 2;
   private static final int field_41795 = 3;
   protected double value;
   private boolean sliderFocused;

   public SliderWidget(int x, int y, int width, int height, Text text, double value) {
      super(x, y, width, height, text);
      this.value = value;
   }

   private int getYImage() {
      int i = this.isFocused() && !this.sliderFocused ? 1 : 0;
      return i * 20;
   }

   private int getTextureV() {
      int i = !this.hovered && !this.sliderFocused ? 2 : 3;
      return i * 20;
   }

   protected MutableText getNarrationMessage() {
      return Text.translatable("gui.narrate.slider", this.getMessage());
   }

   public void appendClickableNarrations(NarrationMessageBuilder builder) {
      builder.put(NarrationPart.TITLE, (Text)this.getNarrationMessage());
      if (this.active) {
         if (this.isFocused()) {
            builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.slider.usage.focused"));
         } else {
            builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.slider.usage.hovered"));
         }
      }

   }

   public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
      MinecraftClient lv = MinecraftClient.getInstance();
      context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      context.drawNineSlicedTexture(TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getYImage());
      context.drawNineSlicedTexture(TEXTURE, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 8, 20, 20, 4, 200, 20, 0, this.getTextureV());
      context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      int k = this.active ? 16777215 : 10526880;
      this.drawScrollableText(context, lv.textRenderer, 2, k | MathHelper.ceil(this.alpha * 255.0F) << 24);
   }

   public void onClick(double mouseX, double mouseY) {
      this.setValueFromMouse(mouseX);
   }

   public void setFocused(boolean focused) {
      super.setFocused(focused);
      if (!focused) {
         this.sliderFocused = false;
      } else {
         GuiNavigationType lv = MinecraftClient.getInstance().getNavigationType();
         if (lv == GuiNavigationType.MOUSE || lv == GuiNavigationType.KEYBOARD_TAB) {
            this.sliderFocused = true;
         }

      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (KeyCodes.isToggle(keyCode)) {
         this.sliderFocused = !this.sliderFocused;
         return true;
      } else {
         if (this.sliderFocused) {
            boolean bl = keyCode == GLFW.GLFW_KEY_LEFT;
            if (bl || keyCode == GLFW.GLFW_KEY_RIGHT) {
               float f = bl ? -1.0F : 1.0F;
               this.setValue(this.value + (double)(f / (float)(this.width - 8)));
               return true;
            }
         }

         return false;
      }
   }

   private void setValueFromMouse(double mouseX) {
      this.setValue((mouseX - (double)(this.getX() + 4)) / (double)(this.width - 8));
   }

   private void setValue(double value) {
      double e = this.value;
      this.value = MathHelper.clamp(value, 0.0, 1.0);
      if (e != this.value) {
         this.applyValue();
      }

      this.updateMessage();
   }

   protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
      this.setValueFromMouse(mouseX);
      super.onDrag(mouseX, mouseY, deltaX, deltaY);
   }

   public void playDownSound(SoundManager soundManager) {
   }

   public void onRelease(double mouseX, double mouseY) {
      super.playDownSound(MinecraftClient.getInstance().getSoundManager());
   }

   protected abstract void updateMessage();

   protected abstract void applyValue();
}
