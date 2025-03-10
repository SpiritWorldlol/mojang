package net.minecraft.entity.boss.dragon.phase;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChargingPlayerPhase extends AbstractPhase {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int DURATION = 10;
   @Nullable
   private Vec3d pathTarget;
   private int chargingTicks;

   public ChargingPlayerPhase(EnderDragonEntity arg) {
      super(arg);
   }

   public void serverTick() {
      if (this.pathTarget == null) {
         LOGGER.warn("Aborting charge player as no target was set.");
         this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
      } else if (this.chargingTicks > 0 && this.chargingTicks++ >= 10) {
         this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
      } else {
         double d = this.pathTarget.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
         if (d < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            ++this.chargingTicks;
         }

      }
   }

   public void beginPhase() {
      this.pathTarget = null;
      this.chargingTicks = 0;
   }

   public void setPathTarget(Vec3d pathTarget) {
      this.pathTarget = pathTarget;
   }

   public float getMaxYAcceleration() {
      return 3.0F;
   }

   @Nullable
   public Vec3d getPathTarget() {
      return this.pathTarget;
   }

   public PhaseType getType() {
      return PhaseType.CHARGING_PLAYER;
   }
}
