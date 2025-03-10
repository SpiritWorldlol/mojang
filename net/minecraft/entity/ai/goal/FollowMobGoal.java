package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;

public class FollowMobGoal extends Goal {
   private final MobEntity mob;
   private final Predicate targetPredicate;
   @Nullable
   private MobEntity target;
   private final double speed;
   private final EntityNavigation navigation;
   private int updateCountdownTicks;
   private final float minDistance;
   private float oldWaterPathFindingPenalty;
   private final float maxDistance;

   public FollowMobGoal(MobEntity mob, double speed, float minDistance, float maxDistance) {
      this.mob = mob;
      this.targetPredicate = (target) -> {
         return target != null && mob.getClass() != target.getClass();
      };
      this.speed = speed;
      this.navigation = mob.getNavigation();
      this.minDistance = minDistance;
      this.maxDistance = maxDistance;
      this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      if (!(mob.getNavigation() instanceof MobNavigation) && !(mob.getNavigation() instanceof BirdNavigation)) {
         throw new IllegalArgumentException("Unsupported mob type for FollowMobGoal");
      }
   }

   public boolean canStart() {
      List list = this.mob.getWorld().getEntitiesByClass(MobEntity.class, this.mob.getBoundingBox().expand((double)this.maxDistance), this.targetPredicate);
      if (!list.isEmpty()) {
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            MobEntity lv = (MobEntity)var2.next();
            if (!lv.isInvisible()) {
               this.target = lv;
               return true;
            }
         }
      }

      return false;
   }

   public boolean shouldContinue() {
      return this.target != null && !this.navigation.isIdle() && this.mob.squaredDistanceTo(this.target) > (double)(this.minDistance * this.minDistance);
   }

   public void start() {
      this.updateCountdownTicks = 0;
      this.oldWaterPathFindingPenalty = this.mob.getPathfindingPenalty(PathNodeType.WATER);
      this.mob.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
   }

   public void stop() {
      this.target = null;
      this.navigation.stop();
      this.mob.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathFindingPenalty);
   }

   public void tick() {
      if (this.target != null && !this.mob.isLeashed()) {
         this.mob.getLookControl().lookAt(this.target, 10.0F, (float)this.mob.getMaxLookPitchChange());
         if (--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = this.getTickCount(10);
            double d = this.mob.getX() - this.target.getX();
            double e = this.mob.getY() - this.target.getY();
            double f = this.mob.getZ() - this.target.getZ();
            double g = d * d + e * e + f * f;
            if (!(g <= (double)(this.minDistance * this.minDistance))) {
               this.navigation.startMovingTo(this.target, this.speed);
            } else {
               this.navigation.stop();
               LookControl lv = this.target.getLookControl();
               if (g <= (double)this.minDistance || lv.getLookX() == this.mob.getX() && lv.getLookY() == this.mob.getY() && lv.getLookZ() == this.mob.getZ()) {
                  double h = this.target.getX() - this.mob.getX();
                  double i = this.target.getZ() - this.mob.getZ();
                  this.navigation.startMovingTo(this.mob.getX() - h, this.mob.getY(), this.mob.getZ() - i, this.speed);
               }

            }
         }
      }
   }
}
