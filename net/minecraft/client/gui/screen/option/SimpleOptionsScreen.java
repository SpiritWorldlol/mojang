package net.minecraft.client.gui.screen.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class SimpleOptionsScreen extends GameOptionsScreen {
   protected final SimpleOption[] options;
   @Nullable
   private ClickableWidget narratorButton;
   protected OptionListWidget buttonList;

   public SimpleOptionsScreen(Screen parent, GameOptions gameOptions, Text title, SimpleOption[] options) {
      super(parent, gameOptions, title);
      this.options = options;
   }

   protected void init() {
      this.buttonList = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
      this.buttonList.addAll(this.options);
      this.addSelectableChild(this.buttonList);
      this.initFooter();
      this.narratorButton = this.buttonList.getWidgetFor(this.gameOptions.getNarrator());
      if (this.narratorButton != null) {
         this.narratorButton.active = this.client.getNarratorManager().isActive();
      }

   }

   protected void initFooter() {
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build());
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.render(context, this.buttonList, mouseX, mouseY, delta);
   }

   public void updateNarratorButtonText() {
      if (this.narratorButton instanceof CyclingButtonWidget) {
         ((CyclingButtonWidget)this.narratorButton).setValue((NarratorMode)this.gameOptions.getNarrator().getValue());
      }

   }
}
