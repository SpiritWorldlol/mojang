package net.minecraft.client.realms.gui.screen;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class RealmsResetNormalWorldScreen extends RealmsScreen {
   private static final Text RESET_SEED_TEXT = Text.translatable("mco.reset.world.seed");
   private final Consumer callback;
   private TextFieldWidget seedEdit;
   private RealmsWorldGeneratorType generatorType;
   private boolean mapFeatures;
   private final Text parentTitle;

   public RealmsResetNormalWorldScreen(Consumer callback, Text parentTitle) {
      super(Text.translatable("mco.reset.world.generate"));
      this.generatorType = RealmsWorldGeneratorType.DEFAULT;
      this.mapFeatures = true;
      this.callback = callback;
      this.parentTitle = parentTitle;
   }

   public void tick() {
      this.seedEdit.tick();
      super.tick();
   }

   public void init() {
      this.seedEdit = new TextFieldWidget(this.client.textRenderer, this.width / 2 - 100, row(2), 200, 20, (TextFieldWidget)null, Text.translatable("mco.reset.world.seed"));
      this.seedEdit.setMaxLength(32);
      this.addSelectableChild(this.seedEdit);
      this.setInitialFocus(this.seedEdit);
      this.addDrawableChild(CyclingButtonWidget.builder(RealmsWorldGeneratorType::getText).values((Object[])RealmsWorldGeneratorType.values()).initially(this.generatorType).build(this.width / 2 - 102, row(4), 205, 20, Text.translatable("selectWorld.mapType"), (button, generatorType) -> {
         this.generatorType = generatorType;
      }));
      this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.mapFeatures).build(this.width / 2 - 102, row(6) - 2, 205, 20, Text.translatable("selectWorld.mapFeatures"), (button, mapFeatures) -> {
         this.mapFeatures = mapFeatures;
      }));
      this.addDrawableChild(ButtonWidget.builder(this.parentTitle, (button) -> {
         this.callback.accept(new ResetWorldInfo(this.seedEdit.getText(), this.generatorType, this.mapFeatures));
      }).dimensions(this.width / 2 - 102, row(12), 97, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.close();
      }).dimensions(this.width / 2 + 8, row(12), 97, 20).build());
   }

   public void close() {
      this.callback.accept((Object)null);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 17, 16777215);
      context.drawText(this.textRenderer, RESET_SEED_TEXT, this.width / 2 - 100, row(1), 10526880, false);
      this.seedEdit.render(context, mouseX, mouseY, delta);
      super.render(context, mouseX, mouseY, delta);
   }
}
