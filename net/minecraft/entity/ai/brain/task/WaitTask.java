package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public class WaitTask implements Task {
   private final int minRunTime;
   private final int maxRunTime;
   private MultiTickTask.Status status;
   private long waitUntil;

   public WaitTask(int minRunTime, int maxRunTime) {
      this.status = MultiTickTask.Status.STOPPED;
      this.minRunTime = minRunTime;
      this.maxRunTime = maxRunTime;
   }

   public MultiTickTask.Status getStatus() {
      return this.status;
   }

   public final boolean tryStarting(ServerWorld world, LivingEntity entity, long time) {
      this.status = MultiTickTask.Status.RUNNING;
      int i = this.minRunTime + world.getRandom().nextInt(this.maxRunTime + 1 - this.minRunTime);
      this.waitUntil = time + (long)i;
      return true;
   }

   public final void tick(ServerWorld world, LivingEntity entity, long time) {
      if (time > this.waitUntil) {
         this.stop(world, entity, time);
      }

   }

   public final void stop(ServerWorld world, LivingEntity entity, long time) {
      this.status = MultiTickTask.Status.STOPPED;
   }

   public String getName() {
      return this.getClass().getSimpleName();
   }
}
