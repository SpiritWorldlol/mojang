package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.realms.RealmsLabel;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsWorldOptions;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class RealmsSlotOptionsScreen extends RealmsScreen {
   private static final int field_32125 = 2;
   public static final List DIFFICULTIES;
   private static final int field_32126 = 0;
   public static final List GAME_MODES;
   private static final Text EDIT_SLOT_NAME;
   static final Text SPAWN_PROTECTION;
   private static final Text SPAWN_TOGGLE_TITLE;
   private TextFieldWidget nameEdit;
   protected final RealmsConfigureWorldScreen parent;
   private int column1_x;
   private int column2_x;
   private final RealmsWorldOptions options;
   private final RealmsServer.WorldType worldType;
   private Difficulty difficulty;
   private GameMode gameMode;
   private final String defaultSlotName;
   private String slotName;
   private boolean pvp;
   private boolean spawnNpcs;
   private boolean spawnAnimals;
   private boolean spawnMonsters;
   int spawnProtection;
   private boolean commandBlocks;
   private boolean forceGameMode;
   SettingsSlider spawnProtectionButton;

   public RealmsSlotOptionsScreen(RealmsConfigureWorldScreen parent, RealmsWorldOptions options, RealmsServer.WorldType worldType, int activeSlot) {
      super(Text.translatable("mco.configure.world.buttons.options"));
      this.parent = parent;
      this.options = options;
      this.worldType = worldType;
      this.difficulty = (Difficulty)get(DIFFICULTIES, options.difficulty, 2);
      this.gameMode = (GameMode)get(GAME_MODES, options.gameMode, 0);
      this.defaultSlotName = options.getDefaultSlotName(activeSlot);
      this.setSlotName(options.getSlotName(activeSlot));
      if (worldType == RealmsServer.WorldType.NORMAL) {
         this.pvp = options.pvp;
         this.spawnProtection = options.spawnProtection;
         this.forceGameMode = options.forceGameMode;
         this.spawnAnimals = options.spawnAnimals;
         this.spawnMonsters = options.spawnMonsters;
         this.spawnNpcs = options.spawnNpcs;
         this.commandBlocks = options.commandBlocks;
      } else {
         this.pvp = true;
         this.spawnProtection = 0;
         this.forceGameMode = false;
         this.spawnAnimals = true;
         this.spawnMonsters = true;
         this.spawnNpcs = true;
         this.commandBlocks = true;
      }

   }

   public void tick() {
      this.nameEdit.tick();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private static Object get(List list, int index, int fallbackIndex) {
      try {
         return list.get(index);
      } catch (IndexOutOfBoundsException var4) {
         return list.get(fallbackIndex);
      }
   }

   private static int indexOf(List list, Object value, int fallbackIndex) {
      int j = list.indexOf(value);
      return j == -1 ? fallbackIndex : j;
   }

   public void init() {
      this.column2_x = 170;
      this.column1_x = this.width / 2 - this.column2_x;
      int i = this.width / 2 + 10;
      if (this.worldType != RealmsServer.WorldType.NORMAL) {
         MutableText lv;
         if (this.worldType == RealmsServer.WorldType.ADVENTUREMAP) {
            lv = Text.translatable("mco.configure.world.edit.subscreen.adventuremap");
         } else if (this.worldType == RealmsServer.WorldType.INSPIRATION) {
            lv = Text.translatable("mco.configure.world.edit.subscreen.inspiration");
         } else {
            lv = Text.translatable("mco.configure.world.edit.subscreen.experience");
         }

         this.addLabel(new RealmsLabel(lv, this.width / 2, 26, 16711680));
      }

      this.nameEdit = new TextFieldWidget(this.client.textRenderer, this.column1_x + 2, row(1), this.column2_x - 4, 20, (TextFieldWidget)null, Text.translatable("mco.configure.world.edit.slot.name"));
      this.nameEdit.setMaxLength(10);
      this.nameEdit.setText(this.slotName);
      this.nameEdit.setChangedListener(this::setSlotName);
      this.focusOn(this.nameEdit);
      CyclingButtonWidget lv2 = (CyclingButtonWidget)this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.pvp).build(i, row(1), this.column2_x, 20, Text.translatable("mco.configure.world.pvp"), (button, pvp) -> {
         this.pvp = pvp;
      }));
      this.addDrawableChild(CyclingButtonWidget.builder(GameMode::getSimpleTranslatableName).values((Collection)GAME_MODES).initially(this.gameMode).build(this.column1_x, row(3), this.column2_x, 20, Text.translatable("selectWorld.gameMode"), (button, gameModeIndex) -> {
         this.gameMode = gameModeIndex;
      }));
      Text lv3 = Text.translatable("mco.configure.world.spawn_toggle.message");
      CyclingButtonWidget lv4 = (CyclingButtonWidget)this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.spawnAnimals).build(i, row(3), this.column2_x, 20, Text.translatable("mco.configure.world.spawnAnimals"), this.getSpawnToggleButtonCallback(lv3, (spawnAnimals) -> {
         this.spawnAnimals = spawnAnimals;
      })));
      CyclingButtonWidget lv5 = CyclingButtonWidget.onOffBuilder(this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters).build(i, row(5), this.column2_x, 20, Text.translatable("mco.configure.world.spawnMonsters"), this.getSpawnToggleButtonCallback(lv3, (spawnMonsters) -> {
         this.spawnMonsters = spawnMonsters;
      }));
      this.addDrawableChild(CyclingButtonWidget.builder(Difficulty::getTranslatableName).values((Collection)DIFFICULTIES).initially(this.difficulty).build(this.column1_x, row(5), this.column2_x, 20, Text.translatable("options.difficulty"), (button, difficulty) -> {
         this.difficulty = difficulty;
         if (this.worldType == RealmsServer.WorldType.NORMAL) {
            boolean bl = this.difficulty != Difficulty.PEACEFUL;
            lv5.active = bl;
            lv5.setValue(bl && this.spawnMonsters);
         }

      }));
      this.addDrawableChild(lv5);
      this.spawnProtectionButton = (SettingsSlider)this.addDrawableChild(new SettingsSlider(this.column1_x, row(7), this.column2_x, this.spawnProtection, 0.0F, 16.0F));
      CyclingButtonWidget lv6 = (CyclingButtonWidget)this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.spawnNpcs).build(i, row(7), this.column2_x, 20, Text.translatable("mco.configure.world.spawnNPCs"), this.getSpawnToggleButtonCallback(Text.translatable("mco.configure.world.spawn_toggle.message.npc"), (spawnNpcs) -> {
         this.spawnNpcs = spawnNpcs;
      })));
      CyclingButtonWidget lv7 = (CyclingButtonWidget)this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.forceGameMode).build(this.column1_x, row(9), this.column2_x, 20, Text.translatable("mco.configure.world.forceGameMode"), (button, forceGameMode) -> {
         this.forceGameMode = forceGameMode;
      }));
      CyclingButtonWidget lv8 = (CyclingButtonWidget)this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.commandBlocks).build(i, row(9), this.column2_x, 20, Text.translatable("mco.configure.world.commandBlocks"), (button, commandBlocks) -> {
         this.commandBlocks = commandBlocks;
      }));
      if (this.worldType != RealmsServer.WorldType.NORMAL) {
         lv2.active = false;
         lv4.active = false;
         lv6.active = false;
         lv5.active = false;
         this.spawnProtectionButton.active = false;
         lv8.active = false;
         lv7.active = false;
      }

      if (this.difficulty == Difficulty.PEACEFUL) {
         lv5.active = false;
      }

      this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.buttons.done"), (button) -> {
         this.saveSettings();
      }).dimensions(this.column1_x, row(13), this.column2_x, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(i, row(13), this.column2_x, 20).build());
      this.addSelectableChild(this.nameEdit);
   }

   private CyclingButtonWidget.UpdateCallback getSpawnToggleButtonCallback(Text text, Consumer valueSetter) {
      return (button, value) -> {
         if (value) {
            valueSetter.accept(true);
         } else {
            this.client.setScreen(new ConfirmScreen((confirmed) -> {
               if (confirmed) {
                  valueSetter.accept(false);
               }

               this.client.setScreen(this);
            }, SPAWN_TOGGLE_TITLE, text, ScreenTexts.PROCEED, ScreenTexts.CANCEL));
         }

      };
   }

   public Text getNarratedTitle() {
      return ScreenTexts.joinSentences(this.getTitle(), this.narrateLabels());
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 17, 16777215);
      context.drawText(this.textRenderer, EDIT_SLOT_NAME, this.column1_x + this.column2_x / 2 - this.textRenderer.getWidth((StringVisitable)EDIT_SLOT_NAME) / 2, row(0) - 5, 16777215, false);
      this.nameEdit.render(context, mouseX, mouseY, delta);
      super.render(context, mouseX, mouseY, delta);
   }

   private void setSlotName(String slotName) {
      if (slotName.equals(this.defaultSlotName)) {
         this.slotName = "";
      } else {
         this.slotName = slotName;
      }

   }

   private void saveSettings() {
      int i = indexOf(DIFFICULTIES, this.difficulty, 2);
      int j = indexOf(GAME_MODES, this.gameMode, 0);
      if (this.worldType != RealmsServer.WorldType.ADVENTUREMAP && this.worldType != RealmsServer.WorldType.EXPERIENCE && this.worldType != RealmsServer.WorldType.INSPIRATION) {
         boolean bl = this.worldType == RealmsServer.WorldType.NORMAL && this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters;
         this.parent.saveSlotSettings(new RealmsWorldOptions(this.pvp, this.spawnAnimals, bl, this.spawnNpcs, this.spawnProtection, this.commandBlocks, i, j, this.forceGameMode, this.slotName));
      } else {
         this.parent.saveSlotSettings(new RealmsWorldOptions(this.options.pvp, this.options.spawnAnimals, this.options.spawnMonsters, this.options.spawnNpcs, this.options.spawnProtection, this.options.commandBlocks, i, j, this.options.forceGameMode, this.slotName));
      }

   }

   static {
      DIFFICULTIES = ImmutableList.of(Difficulty.PEACEFUL, Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD);
      GAME_MODES = ImmutableList.of(GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE);
      EDIT_SLOT_NAME = Text.translatable("mco.configure.world.edit.slot.name");
      SPAWN_PROTECTION = Text.translatable("mco.configure.world.spawnProtection");
      SPAWN_TOGGLE_TITLE = Text.translatable("mco.configure.world.spawn_toggle.title").formatted(Formatting.RED, Formatting.BOLD);
   }

   @Environment(EnvType.CLIENT)
   private class SettingsSlider extends SliderWidget {
      private final double min;
      private final double max;

      public SettingsSlider(int x, int y, int width, int value, float min, float max) {
         super(x, y, width, 20, ScreenTexts.EMPTY, 0.0);
         this.min = (double)min;
         this.max = (double)max;
         this.value = (double)((MathHelper.clamp((float)value, min, max) - min) / (max - min));
         this.updateMessage();
      }

      public void applyValue() {
         if (RealmsSlotOptionsScreen.this.spawnProtectionButton.active) {
            RealmsSlotOptionsScreen.this.spawnProtection = (int)MathHelper.lerp(MathHelper.clamp(this.value, 0.0, 1.0), this.min, this.max);
         }
      }

      protected void updateMessage() {
         this.setMessage(ScreenTexts.composeGenericOptionText(RealmsSlotOptionsScreen.SPAWN_PROTECTION, (Text)(RealmsSlotOptionsScreen.this.spawnProtection == 0 ? ScreenTexts.OFF : Text.literal(String.valueOf(RealmsSlotOptionsScreen.this.spawnProtection)))));
      }

      public void onClick(double mouseX, double mouseY) {
      }

      public void onRelease(double mouseX, double mouseY) {
      }
   }
}
