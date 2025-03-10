package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class JumpInBedTask extends MultiTickTask {
   private static final int MAX_TICKS_OUT_OF_BED = 100;
   private static final int MIN_JUMP_TICKS = 3;
   private static final int JUMP_TIME_VARIANCE = 6;
   private static final int TICKS_TO_NEXT_JUMP = 5;
   private final float walkSpeed;
   @Nullable
   private BlockPos bedPos;
   private int ticksOutOfBedUntilStopped;
   private int jumpsRemaining;
   private int ticksToNextJump;

   public JumpInBedTask(float walkSpeed) {
      super(ImmutableMap.of(MemoryModuleType.NEAREST_BED, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));
      this.walkSpeed = walkSpeed;
   }

   protected boolean shouldRun(ServerWorld arg, MobEntity arg2) {
      return arg2.isBaby() && this.shouldStartJumping(arg, arg2);
   }

   protected void run(ServerWorld arg, MobEntity arg2, long l) {
      super.run(arg, arg2, l);
      this.getNearestBed(arg2).ifPresent((pos) -> {
         this.bedPos = pos;
         this.ticksOutOfBedUntilStopped = 100;
         this.jumpsRemaining = 3 + arg.random.nextInt(4);
         this.ticksToNextJump = 0;
         this.setWalkTarget(arg2, pos);
      });
   }

   protected void finishRunning(ServerWorld arg, MobEntity arg2, long l) {
      super.finishRunning(arg, arg2, l);
      this.bedPos = null;
      this.ticksOutOfBedUntilStopped = 0;
      this.jumpsRemaining = 0;
      this.ticksToNextJump = 0;
   }

   protected boolean shouldKeepRunning(ServerWorld arg, MobEntity arg2, long l) {
      return arg2.isBaby() && this.bedPos != null && this.isBedAt(arg, this.bedPos) && !this.isBedGoneTooLong(arg, arg2) && !this.isDoneJumping(arg, arg2);
   }

   protected boolean isTimeLimitExceeded(long time) {
      return false;
   }

   protected void keepRunning(ServerWorld arg, MobEntity arg2, long l) {
      if (!this.isAboveBed(arg, arg2)) {
         --this.ticksOutOfBedUntilStopped;
      } else if (this.ticksToNextJump > 0) {
         --this.ticksToNextJump;
      } else {
         if (this.isOnBed(arg, arg2)) {
            arg2.getJumpControl().setActive();
            --this.jumpsRemaining;
            this.ticksToNextJump = 5;
         }

      }
   }

   private void setWalkTarget(MobEntity mob, BlockPos pos) {
      mob.getBrain().remember(MemoryModuleType.WALK_TARGET, (Object)(new WalkTarget(pos, this.walkSpeed, 0)));
   }

   private boolean shouldStartJumping(ServerWorld world, MobEntity mob) {
      return this.isAboveBed(world, mob) || this.getNearestBed(mob).isPresent();
   }

   private boolean isAboveBed(ServerWorld world, MobEntity mob) {
      BlockPos lv = mob.getBlockPos();
      BlockPos lv2 = lv.down();
      return this.isBedAt(world, lv) || this.isBedAt(world, lv2);
   }

   private boolean isOnBed(ServerWorld world, MobEntity mob) {
      return this.isBedAt(world, mob.getBlockPos());
   }

   private boolean isBedAt(ServerWorld world, BlockPos pos) {
      return world.getBlockState(pos).isIn(BlockTags.BEDS);
   }

   private Optional getNearestBed(MobEntity mob) {
      return mob.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_BED);
   }

   private boolean isBedGoneTooLong(ServerWorld world, MobEntity mob) {
      return !this.isAboveBed(world, mob) && this.ticksOutOfBedUntilStopped <= 0;
   }

   private boolean isDoneJumping(ServerWorld world, MobEntity mob) {
      return this.isAboveBed(world, mob) && this.jumpsRemaining <= 0;
   }

   // $FF: synthetic method
   protected boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
      return this.shouldKeepRunning(world, (MobEntity)entity, time);
   }

   // $FF: synthetic method
   protected void keepRunning(ServerWorld world, LivingEntity entity, long time) {
      this.keepRunning(world, (MobEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (MobEntity)entity, time);
   }
}
