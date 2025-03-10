package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.passive.TameableEntity;

public class AttackWithOwnerGoal extends TrackTargetGoal {
   private final TameableEntity tameable;
   private LivingEntity attacking;
   private int lastAttackTime;

   public AttackWithOwnerGoal(TameableEntity tameable) {
      super(tameable, false);
      this.tameable = tameable;
      this.setControls(EnumSet.of(Goal.Control.TARGET));
   }

   public boolean canStart() {
      if (this.tameable.isTamed() && !this.tameable.isSitting()) {
         LivingEntity lv = this.tameable.getOwner();
         if (lv == null) {
            return false;
         } else {
            this.attacking = lv.getAttacking();
            int i = lv.getLastAttackTime();
            return i != this.lastAttackTime && this.canTrack(this.attacking, TargetPredicate.DEFAULT) && this.tameable.canAttackWithOwner(this.attacking, lv);
         }
      } else {
         return false;
      }
   }

   public void start() {
      this.mob.setTarget(this.attacking);
      LivingEntity lv = this.tameable.getOwner();
      if (lv != null) {
         this.lastAttackTime = lv.getLastAttackTime();
      }

      super.start();
   }
}
