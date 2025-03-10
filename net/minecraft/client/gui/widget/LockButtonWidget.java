package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class LockButtonWidget extends ButtonWidget {
   private boolean locked;

   public LockButtonWidget(int x, int y, ButtonWidget.PressAction action) {
      super(x, y, 20, 20, Text.translatable("narrator.button.difficulty_lock"), action, DEFAULT_NARRATION_SUPPLIER);
   }

   protected MutableText getNarrationMessage() {
      return ScreenTexts.joinSentences(super.getNarrationMessage(), this.isLocked() ? Text.translatable("narrator.button.difficulty_lock.locked") : Text.translatable("narrator.button.difficulty_lock.unlocked"));
   }

   public boolean isLocked() {
      return this.locked;
   }

   public void setLocked(boolean locked) {
      this.locked = locked;
   }

   public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
      IconLocation lv;
      if (!this.active) {
         lv = this.locked ? LockButtonWidget.IconLocation.LOCKED_DISABLED : LockButtonWidget.IconLocation.UNLOCKED_DISABLED;
      } else if (this.isSelected()) {
         lv = this.locked ? LockButtonWidget.IconLocation.LOCKED_HOVER : LockButtonWidget.IconLocation.UNLOCKED_HOVER;
      } else {
         lv = this.locked ? LockButtonWidget.IconLocation.LOCKED : LockButtonWidget.IconLocation.UNLOCKED;
      }

      context.drawTexture(ButtonWidget.WIDGETS_TEXTURE, this.getX(), this.getY(), lv.getU(), lv.getV(), this.width, this.height);
   }

   @Environment(EnvType.CLIENT)
   static enum IconLocation {
      LOCKED(0, 146),
      LOCKED_HOVER(0, 166),
      LOCKED_DISABLED(0, 186),
      UNLOCKED(20, 146),
      UNLOCKED_HOVER(20, 166),
      UNLOCKED_DISABLED(20, 186);

      private final int u;
      private final int v;

      private IconLocation(int u, int v) {
         this.u = u;
         this.v = v;
      }

      public int getU() {
         return this.u;
      }

      public int getV() {
         return this.v;
      }

      // $FF: synthetic method
      private static IconLocation[] method_36870() {
         return new IconLocation[]{LOCKED, LOCKED_HOVER, LOCKED_DISABLED, UNLOCKED, UNLOCKED_HOVER, UNLOCKED_DISABLED};
      }
   }
}
