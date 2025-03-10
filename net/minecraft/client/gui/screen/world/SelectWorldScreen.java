package net.minecraft.client.gui.screen.world;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.PathUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SelectWorldScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final GeneratorOptions DEBUG_GENERATOR_OPTIONS = new GeneratorOptions((long)"test1".hashCode(), true, false);
   protected final Screen parent;
   private ButtonWidget deleteButton;
   private ButtonWidget selectButton;
   private ButtonWidget editButton;
   private ButtonWidget recreateButton;
   protected TextFieldWidget searchBox;
   private WorldListWidget levelList;

   public SelectWorldScreen(Screen parent) {
      super(Text.translatable("selectWorld.title"));
      this.parent = parent;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return super.mouseScrolled(mouseX, mouseY, amount);
   }

   public void tick() {
      this.searchBox.tick();
   }

   protected void init() {
      this.searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 22, 200, 20, this.searchBox, Text.translatable("selectWorld.search"));
      this.searchBox.setChangedListener((search) -> {
         this.levelList.setSearch(search);
      });
      this.levelList = new WorldListWidget(this, this.client, this.width, this.height, 48, this.height - 64, 36, this.searchBox.getText(), this.levelList);
      this.addSelectableChild(this.searchBox);
      this.addSelectableChild(this.levelList);
      this.selectButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.select"), (button) -> {
         this.levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::play);
      }).dimensions(this.width / 2 - 154, this.height - 52, 150, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.create"), (button) -> {
         CreateWorldScreen.create(this.client, this);
      }).dimensions(this.width / 2 + 4, this.height - 52, 150, 20).build());
      this.editButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.edit"), (button) -> {
         this.levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::edit);
      }).dimensions(this.width / 2 - 154, this.height - 28, 72, 20).build());
      this.deleteButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.delete"), (button) -> {
         this.levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::deleteIfConfirmed);
      }).dimensions(this.width / 2 - 76, this.height - 28, 72, 20).build());
      this.recreateButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.recreate"), (button) -> {
         this.levelList.getSelectedAsOptional().ifPresent(WorldListWidget.WorldEntry::recreate);
      }).dimensions(this.width / 2 + 4, this.height - 28, 72, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 + 82, this.height - 28, 72, 20).build());
      this.worldSelected(false, false);
      this.setInitialFocus(this.searchBox);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return super.keyPressed(keyCode, scanCode, modifiers) ? true : this.searchBox.keyPressed(keyCode, scanCode, modifiers);
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   public boolean charTyped(char chr, int modifiers) {
      return this.searchBox.charTyped(chr, modifiers);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.levelList.render(context, mouseX, mouseY, delta);
      this.searchBox.render(context, mouseX, mouseY, delta);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 8, 16777215);
      super.render(context, mouseX, mouseY, delta);
   }

   public void worldSelected(boolean buttonsActive, boolean deleteButtonActive) {
      this.selectButton.active = buttonsActive;
      this.editButton.active = buttonsActive;
      this.recreateButton.active = buttonsActive;
      this.deleteButton.active = deleteButtonActive;
   }

   public void removed() {
      if (this.levelList != null) {
         this.levelList.children().forEach(WorldListWidget.Entry::close);
      }

   }

   // $FF: synthetic method
   private void method_35739(ButtonWidget button) {
      try {
         String string = "DEBUG world";
         if (!this.levelList.children().isEmpty()) {
            WorldListWidget.Entry lv = (WorldListWidget.Entry)this.levelList.children().get(0);
            if (lv instanceof WorldListWidget.WorldEntry) {
               WorldListWidget.WorldEntry lv2 = (WorldListWidget.WorldEntry)lv;
               if (lv2.getLevelDisplayName().equals("DEBUG world")) {
                  lv2.delete();
               }
            }
         }

         LevelInfo lv3 = new LevelInfo("DEBUG world", GameMode.SPECTATOR, false, Difficulty.NORMAL, true, new GameRules(), DataConfiguration.SAFE_MODE);
         String string2 = PathUtil.getNextUniqueName(this.client.getLevelStorage().getSavesDirectory(), "DEBUG world", "");
         this.client.createIntegratedServerLoader().createAndStart(string2, lv3, DEBUG_GENERATOR_OPTIONS, WorldPresets::createDemoOptions);
      } catch (IOException var5) {
         LOGGER.error("Failed to recreate the debug world", var5);
      }

   }
}
