package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class RespirationEnchantment extends Enchantment {
   public RespirationEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.ARMOR_HEAD, slotTypes);
   }

   public int getMinPower(int level) {
      return 10 * level;
   }

   public int getMaxPower(int level) {
      return this.getMinPower(level) + 30;
   }

   public int getMaxLevel() {
      return 3;
   }
}
