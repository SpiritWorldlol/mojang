package net.minecraft.entity;

import net.minecraft.util.math.MathHelper;

public class DamageUtil {
   public static final float field_29962 = 20.0F;
   public static final float field_29963 = 25.0F;
   public static final float field_29964 = 2.0F;
   public static final float field_29965 = 0.2F;
   private static final int field_29966 = 4;

   public static float getDamageLeft(float damage, float armor, float armorToughness) {
      float i = 2.0F + armorToughness / 4.0F;
      float j = MathHelper.clamp(armor - damage / i, armor * 0.2F, 20.0F);
      return damage * (1.0F - j / 25.0F);
   }

   public static float getInflictedDamage(float damageDealt, float protection) {
      float h = MathHelper.clamp(protection, 0.0F, 20.0F);
      return damageDealt * (1.0F - h / 25.0F);
   }
}
