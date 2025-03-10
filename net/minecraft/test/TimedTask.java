package net.minecraft.test;

import org.jetbrains.annotations.Nullable;

class TimedTask {
   @Nullable
   public final Long duration;
   public final Runnable task;

   private TimedTask(@Nullable Long duration, Runnable task) {
      this.duration = duration;
      this.task = task;
   }

   static TimedTask create(Runnable task) {
      return new TimedTask((Long)null, task);
   }

   static TimedTask create(long duration, Runnable task) {
      return new TimedTask(duration, task);
   }
}
