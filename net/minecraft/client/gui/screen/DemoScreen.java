package net.minecraft.client.gui.screen;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class DemoScreen extends Screen {
   private static final Identifier DEMO_BG = new Identifier("textures/gui/demo_background.png");
   private MultilineText movementText;
   private MultilineText fullWrappedText;

   public DemoScreen() {
      super(Text.translatable("demo.help.title"));
      this.movementText = MultilineText.EMPTY;
      this.fullWrappedText = MultilineText.EMPTY;
   }

   protected void init() {
      int i = true;
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("demo.help.buy"), (button) -> {
         button.active = false;
         Util.getOperatingSystem().open("https://aka.ms/BuyMinecraftJava");
      }).dimensions(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("demo.help.later"), (button) -> {
         this.client.setScreen((Screen)null);
         this.client.mouse.lockCursor();
      }).dimensions(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20).build());
      GameOptions lv = this.client.options;
      this.movementText = MultilineText.create(this.textRenderer, Text.translatable("demo.help.movementShort", lv.forwardKey.getBoundKeyLocalizedText(), lv.leftKey.getBoundKeyLocalizedText(), lv.backKey.getBoundKeyLocalizedText(), lv.rightKey.getBoundKeyLocalizedText()), Text.translatable("demo.help.movementMouse"), Text.translatable("demo.help.jump", lv.jumpKey.getBoundKeyLocalizedText()), Text.translatable("demo.help.inventory", lv.inventoryKey.getBoundKeyLocalizedText()));
      this.fullWrappedText = MultilineText.create(this.textRenderer, Text.translatable("demo.help.fullWrapped"), 218);
   }

   public void renderBackground(DrawContext context) {
      super.renderBackground(context);
      int i = (this.width - 248) / 2;
      int j = (this.height - 166) / 2;
      context.drawTexture(DEMO_BG, i, j, 0, 0, 248, 166);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      int k = (this.width - 248) / 2 + 10;
      int l = (this.height - 166) / 2 + 8;
      context.drawText(this.textRenderer, this.title, k, l, 2039583, false);
      l = this.movementText.draw(context, k, l + 12, 12, 5197647);
      MultilineText var10000 = this.fullWrappedText;
      int var10003 = l + 20;
      Objects.requireNonNull(this.textRenderer);
      var10000.draw(context, k, var10003, 9, 2039583);
      super.render(context, mouseX, mouseY, delta);
   }
}
