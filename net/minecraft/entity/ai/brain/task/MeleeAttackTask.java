package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.Hand;

public class MeleeAttackTask {
   public static SingleTickTask create(int cooldown) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), context.queryMemoryValue(MemoryModuleType.ATTACK_TARGET), context.queryMemoryAbsent(MemoryModuleType.ATTACK_COOLING_DOWN), context.queryMemoryValue(MemoryModuleType.VISIBLE_MOBS)).apply(context, (lookTarget, attackTarget, attackCoolingDown, visibleMobs) -> {
            return (world, entity, time) -> {
               LivingEntity lv = (LivingEntity)context.getValue(attackTarget);
               if (!isHoldingUsableRangedWeapon(entity) && entity.isInAttackRange(lv) && ((LivingTargetCache)context.getValue(visibleMobs)).contains(lv)) {
                  lookTarget.remember((Object)(new EntityLookTarget(lv, true)));
                  entity.swingHand(Hand.MAIN_HAND);
                  entity.tryAttack(lv);
                  attackCoolingDown.remember(true, (long)cooldown);
                  return true;
               } else {
                  return false;
               }
            };
         });
      });
   }

   private static boolean isHoldingUsableRangedWeapon(MobEntity mob) {
      return mob.isHolding((stack) -> {
         Item lv = stack.getItem();
         return lv instanceof RangedWeaponItem && mob.canUseRangedWeapon((RangedWeaponItem)lv);
      });
   }
}
