package net.minecraft.client.tutorial;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BundleTutorial {
   private final TutorialManager manager;
   private final GameOptions options;
   @Nullable
   private TutorialToast toast;

   public BundleTutorial(TutorialManager manager, GameOptions options) {
      this.manager = manager;
      this.options = options;
   }

   private void start() {
      if (this.toast != null) {
         this.manager.remove(this.toast);
      }

      Text lv = Text.translatable("tutorial.bundleInsert.title");
      Text lv2 = Text.translatable("tutorial.bundleInsert.description");
      this.toast = new TutorialToast(TutorialToast.Type.RIGHT_CLICK, lv, lv2, true);
      this.manager.add(this.toast, 160);
   }

   private void end() {
      if (this.toast != null) {
         this.manager.remove(this.toast);
         this.toast = null;
      }

      if (!this.options.hideBundleTutorial) {
         this.options.hideBundleTutorial = true;
         this.options.write();
      }

   }

   public void onPickupSlotClick(ItemStack cursorStack, ItemStack slotStack, ClickType clickType) {
      if (!this.options.hideBundleTutorial) {
         if (!cursorStack.isEmpty() && slotStack.isOf(Items.BUNDLE)) {
            if (clickType == ClickType.LEFT) {
               this.start();
            } else if (clickType == ClickType.RIGHT) {
               this.end();
            }
         } else if (cursorStack.isOf(Items.BUNDLE) && !slotStack.isEmpty() && clickType == ClickType.RIGHT) {
            this.end();
         }

      }
   }
}
