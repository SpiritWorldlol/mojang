package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class MendingEnchantment extends Enchantment {
   public MendingEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.BREAKABLE, slotTypes);
   }

   public int getMinPower(int level) {
      return level * 25;
   }

   public int getMaxPower(int level) {
      return this.getMinPower(level) + 50;
   }

   public boolean isTreasure() {
      return true;
   }
}
