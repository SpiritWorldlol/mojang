package net.minecraft.client.sound;

import java.util.concurrent.locks.LockSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.thread.ThreadExecutor;

@Environment(EnvType.CLIENT)
public class SoundExecutor extends ThreadExecutor {
   private Thread thread = this.createThread();
   private volatile boolean stopped;

   public SoundExecutor() {
      super("Sound executor");
   }

   private Thread createThread() {
      Thread thread = new Thread(this::waitForStop);
      thread.setDaemon(true);
      thread.setName("Sound engine");
      thread.start();
      return thread;
   }

   protected Runnable createTask(Runnable runnable) {
      return runnable;
   }

   protected boolean canExecute(Runnable task) {
      return !this.stopped;
   }

   protected Thread getThread() {
      return this.thread;
   }

   private void waitForStop() {
      while(!this.stopped) {
         this.runTasks(() -> {
            return this.stopped;
         });
      }

   }

   protected void waitForTasks() {
      LockSupport.park("waiting for tasks");
   }

   public void restart() {
      this.stopped = true;
      this.thread.interrupt();

      try {
         this.thread.join();
      } catch (InterruptedException var2) {
         Thread.currentThread().interrupt();
      }

      this.cancelTasks();
      this.stopped = false;
      this.thread = this.createThread();
   }
}
