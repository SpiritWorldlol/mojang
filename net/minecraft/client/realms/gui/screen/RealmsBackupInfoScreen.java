package net.minecraft.client.realms.gui.screen;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.dto.Backup;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class RealmsBackupInfoScreen extends RealmsScreen {
   private static final Text UNKNOWN = Text.literal("UNKNOWN");
   private final Screen parent;
   final Backup backup;
   private BackupInfoList backupInfoList;

   public RealmsBackupInfoScreen(Screen parent, Backup backup) {
      super(Text.literal("Changes from last backup"));
      this.parent = parent;
      this.backup = backup;
   }

   public void tick() {
   }

   public void init() {
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20).build());
      this.backupInfoList = new BackupInfoList(this.client);
      this.addSelectableChild(this.backupInfoList);
      this.focusOn(this.backupInfoList);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      this.backupInfoList.render(context, mouseX, mouseY, delta);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 10, 16777215);
      super.render(context, mouseX, mouseY, delta);
   }

   Text checkForSpecificMetadata(String key, String value) {
      String string3 = key.toLowerCase(Locale.ROOT);
      if (string3.contains("game") && string3.contains("mode")) {
         return this.gameModeMetadata(value);
      } else {
         return (Text)(string3.contains("game") && string3.contains("difficulty") ? this.gameDifficultyMetadata(value) : Text.literal(value));
      }
   }

   private Text gameDifficultyMetadata(String value) {
      try {
         return ((Difficulty)RealmsSlotOptionsScreen.DIFFICULTIES.get(Integer.parseInt(value))).getTranslatableName();
      } catch (Exception var3) {
         return UNKNOWN;
      }
   }

   private Text gameModeMetadata(String value) {
      try {
         return ((GameMode)RealmsSlotOptionsScreen.GAME_MODES.get(Integer.parseInt(value))).getSimpleTranslatableName();
      } catch (Exception var3) {
         return UNKNOWN;
      }
   }

   @Environment(EnvType.CLIENT)
   private class BackupInfoList extends AlwaysSelectedEntryListWidget {
      public BackupInfoList(MinecraftClient client) {
         super(client, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.height, 32, RealmsBackupInfoScreen.this.height - 64, 36);
         this.setRenderSelection(false);
         if (RealmsBackupInfoScreen.this.backup.changeList != null) {
            RealmsBackupInfoScreen.this.backup.changeList.forEach((key, value) -> {
               this.addEntry(RealmsBackupInfoScreen.this.new BackupInfoListEntry(key, value));
            });
         }

      }
   }

   @Environment(EnvType.CLIENT)
   class BackupInfoListEntry extends AlwaysSelectedEntryListWidget.Entry {
      private final String key;
      private final String value;

      public BackupInfoListEntry(String key, String value) {
         this.key = key;
         this.value = value;
      }

      public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         TextRenderer lv = RealmsBackupInfoScreen.this.client.textRenderer;
         context.drawTextWithShadow(lv, this.key, x, y, 10526880);
         context.drawTextWithShadow(lv, RealmsBackupInfoScreen.this.checkForSpecificMetadata(this.key, this.value), x, y + 12, 16777215);
      }

      public Text getNarration() {
         return Text.translatable("narrator.select", this.key + " " + this.value);
      }
   }
}
