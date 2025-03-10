package net.minecraft.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;

public class HealthBoostStatusEffect extends StatusEffect {
   public HealthBoostStatusEffect(StatusEffectCategory arg, int i) {
      super(arg, i);
   }

   public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.onRemoved(entity, attributes, amplifier);
      if (entity.getHealth() > entity.getMaxHealth()) {
         entity.setHealth(entity.getMaxHealth());
      }

   }
}
