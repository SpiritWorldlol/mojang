package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class FireAspectEnchantment extends Enchantment {
   protected FireAspectEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.WEAPON, slotTypes);
   }

   public int getMinPower(int level) {
      return 10 + 20 * (level - 1);
   }

   public int getMaxPower(int level) {
      return super.getMinPower(level) + 50;
   }

   public int getMaxLevel() {
      return 2;
   }
}
