package net.minecraft.screen.slot;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class FurnaceOutputSlot extends Slot {
   private final PlayerEntity player;
   private int amount;

   public FurnaceOutputSlot(PlayerEntity player, Inventory inventory, int index, int x, int y) {
      super(inventory, index, x, y);
      this.player = player;
   }

   public boolean canInsert(ItemStack stack) {
      return false;
   }

   public ItemStack takeStack(int amount) {
      if (this.hasStack()) {
         this.amount += Math.min(amount, this.getStack().getCount());
      }

      return super.takeStack(amount);
   }

   public void onTakeItem(PlayerEntity player, ItemStack stack) {
      this.onCrafted(stack);
      super.onTakeItem(player, stack);
   }

   protected void onCrafted(ItemStack stack, int amount) {
      this.amount += amount;
      this.onCrafted(stack);
   }

   protected void onCrafted(ItemStack stack) {
      stack.onCraft(this.player.getWorld(), this.player, this.amount);
      PlayerEntity var4 = this.player;
      if (var4 instanceof ServerPlayerEntity lv) {
         Inventory var5 = this.inventory;
         if (var5 instanceof AbstractFurnaceBlockEntity lv2) {
            lv2.dropExperienceForRecipesUsed(lv);
         }
      }

      this.amount = 0;
   }
}
