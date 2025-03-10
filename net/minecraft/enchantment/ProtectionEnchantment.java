package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.MathHelper;

public class ProtectionEnchantment extends Enchantment {
   public final Type protectionType;

   public ProtectionEnchantment(Enchantment.Rarity weight, Type protectionType, EquipmentSlot... slotTypes) {
      super(weight, protectionType == ProtectionEnchantment.Type.FALL ? EnchantmentTarget.ARMOR_FEET : EnchantmentTarget.ARMOR, slotTypes);
      this.protectionType = protectionType;
   }

   public int getMinPower(int level) {
      return this.protectionType.getBasePower() + (level - 1) * this.protectionType.getPowerPerLevel();
   }

   public int getMaxPower(int level) {
      return this.getMinPower(level) + this.protectionType.getPowerPerLevel();
   }

   public int getMaxLevel() {
      return 4;
   }

   public int getProtectionAmount(int level, DamageSource source) {
      if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return 0;
      } else if (this.protectionType == ProtectionEnchantment.Type.ALL) {
         return level;
      } else if (this.protectionType == ProtectionEnchantment.Type.FIRE && source.isIn(DamageTypeTags.IS_FIRE)) {
         return level * 2;
      } else if (this.protectionType == ProtectionEnchantment.Type.FALL && source.isIn(DamageTypeTags.IS_FALL)) {
         return level * 3;
      } else if (this.protectionType == ProtectionEnchantment.Type.EXPLOSION && source.isIn(DamageTypeTags.IS_EXPLOSION)) {
         return level * 2;
      } else {
         return this.protectionType == ProtectionEnchantment.Type.PROJECTILE && source.isIn(DamageTypeTags.IS_PROJECTILE) ? level * 2 : 0;
      }
   }

   public boolean canAccept(Enchantment other) {
      if (other instanceof ProtectionEnchantment lv) {
         if (this.protectionType == lv.protectionType) {
            return false;
         } else {
            return this.protectionType == ProtectionEnchantment.Type.FALL || lv.protectionType == ProtectionEnchantment.Type.FALL;
         }
      } else {
         return super.canAccept(other);
      }
   }

   public static int transformFireDuration(LivingEntity entity, int duration) {
      int j = EnchantmentHelper.getEquipmentLevel(Enchantments.FIRE_PROTECTION, entity);
      if (j > 0) {
         duration -= MathHelper.floor((float)duration * (float)j * 0.15F);
      }

      return duration;
   }

   public static double transformExplosionKnockback(LivingEntity entity, double velocity) {
      int i = EnchantmentHelper.getEquipmentLevel(Enchantments.BLAST_PROTECTION, entity);
      if (i > 0) {
         velocity *= MathHelper.clamp(1.0 - (double)i * 0.15, 0.0, 1.0);
      }

      return velocity;
   }

   public static enum Type {
      ALL(1, 11),
      FIRE(10, 8),
      FALL(5, 6),
      EXPLOSION(5, 8),
      PROJECTILE(3, 6);

      private final int basePower;
      private final int powerPerLevel;

      private Type(int basePower, int powerPerLevel) {
         this.basePower = basePower;
         this.powerPerLevel = powerPerLevel;
      }

      public int getBasePower() {
         return this.basePower;
      }

      public int getPowerPerLevel() {
         return this.powerPerLevel;
      }

      // $FF: synthetic method
      private static Type[] method_36689() {
         return new Type[]{ALL, FIRE, FALL, EXPLOSION, PROJECTILE};
      }
   }
}
