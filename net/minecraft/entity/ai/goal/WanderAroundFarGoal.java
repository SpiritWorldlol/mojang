package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class WanderAroundFarGoal extends WanderAroundGoal {
   public static final float CHANCE = 0.001F;
   protected final float probability;

   public WanderAroundFarGoal(PathAwareEntity arg, double d) {
      this(arg, d, 0.001F);
   }

   public WanderAroundFarGoal(PathAwareEntity mob, double speed, float probability) {
      super(mob, speed);
      this.probability = probability;
   }

   @Nullable
   protected Vec3d getWanderTarget() {
      if (this.mob.isInsideWaterOrBubbleColumn()) {
         Vec3d lv = FuzzyTargeting.find(this.mob, 15, 7);
         return lv == null ? super.getWanderTarget() : lv;
      } else {
         return this.mob.getRandom().nextFloat() >= this.probability ? FuzzyTargeting.find(this.mob, 10, 7) : super.getWanderTarget();
      }
   }
}
