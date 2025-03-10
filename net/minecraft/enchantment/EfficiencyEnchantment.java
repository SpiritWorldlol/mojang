package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class EfficiencyEnchantment extends Enchantment {
   protected EfficiencyEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.DIGGER, slotTypes);
   }

   public int getMinPower(int level) {
      return 1 + 10 * (level - 1);
   }

   public int getMaxPower(int level) {
      return super.getMinPower(level) + 50;
   }

   public int getMaxLevel() {
      return 5;
   }

   public boolean isAcceptableItem(ItemStack stack) {
      return stack.isOf(Items.SHEARS) ? true : super.isAcceptableItem(stack);
   }
}
