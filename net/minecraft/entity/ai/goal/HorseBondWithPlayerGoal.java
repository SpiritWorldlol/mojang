package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class HorseBondWithPlayerGoal extends Goal {
   private final AbstractHorseEntity horse;
   private final double speed;
   private double targetX;
   private double targetY;
   private double targetZ;

   public HorseBondWithPlayerGoal(AbstractHorseEntity horse, double speed) {
      this.horse = horse;
      this.speed = speed;
      this.setControls(EnumSet.of(Goal.Control.MOVE));
   }

   public boolean canStart() {
      if (!this.horse.isTame() && this.horse.hasPassengers()) {
         Vec3d lv = NoPenaltyTargeting.find(this.horse, 5, 4);
         if (lv == null) {
            return false;
         } else {
            this.targetX = lv.x;
            this.targetY = lv.y;
            this.targetZ = lv.z;
            return true;
         }
      } else {
         return false;
      }
   }

   public void start() {
      this.horse.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
   }

   public boolean shouldContinue() {
      return !this.horse.isTame() && !this.horse.getNavigation().isIdle() && this.horse.hasPassengers();
   }

   public void tick() {
      if (!this.horse.isTame() && this.horse.getRandom().nextInt(this.getTickCount(50)) == 0) {
         Entity lv = (Entity)this.horse.getPassengerList().get(0);
         if (lv == null) {
            return;
         }

         if (lv instanceof PlayerEntity) {
            int i = this.horse.getTemper();
            int j = this.horse.getMaxTemper();
            if (j > 0 && this.horse.getRandom().nextInt(j) < i) {
               this.horse.bondWithPlayer((PlayerEntity)lv);
               return;
            }

            this.horse.addTemper(5);
         }

         this.horse.removeAllPassengers();
         this.horse.playAngrySound();
         this.horse.getWorld().sendEntityStatus(this.horse, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
      }

   }
}
