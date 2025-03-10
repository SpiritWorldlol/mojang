package net.minecraft.server.rcon;

import com.mojang.logging.LogUtils;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.logging.UncaughtExceptionHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class RconBase implements Runnable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
   private static final int field_29794 = 5;
   protected volatile boolean running;
   protected final String description;
   @Nullable
   protected Thread thread;

   protected RconBase(String description) {
      this.description = description;
   }

   public synchronized boolean start() {
      if (this.running) {
         return true;
      } else {
         this.running = true;
         String var10004 = this.description;
         this.thread = new Thread(this, var10004 + " #" + THREAD_COUNTER.incrementAndGet());
         this.thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(LOGGER));
         this.thread.start();
         LOGGER.info("Thread {} started", this.description);
         return true;
      }
   }

   public synchronized void stop() {
      this.running = false;
      if (null != this.thread) {
         int i = 0;

         while(this.thread.isAlive()) {
            try {
               this.thread.join(1000L);
               ++i;
               if (i >= 5) {
                  LOGGER.warn("Waited {} seconds attempting force stop!", i);
               } else if (this.thread.isAlive()) {
                  LOGGER.warn("Thread {} ({}) failed to exit after {} second(s)", new Object[]{this, this.thread.getState(), i, new Exception("Stack:")});
                  this.thread.interrupt();
               }
            } catch (InterruptedException var3) {
            }
         }

         LOGGER.info("Thread {} stopped", this.description);
         this.thread = null;
      }
   }

   public boolean isRunning() {
      return this.running;
   }
}
