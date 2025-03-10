package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import org.jetbrains.annotations.Nullable;

public class PrioritizedGoal extends Goal {
   private final Goal goal;
   private final int priority;
   private boolean running;

   public PrioritizedGoal(int priority, Goal goal) {
      this.priority = priority;
      this.goal = goal;
   }

   public boolean canBeReplacedBy(PrioritizedGoal goal) {
      return this.canStop() && goal.getPriority() < this.getPriority();
   }

   public boolean canStart() {
      return this.goal.canStart();
   }

   public boolean shouldContinue() {
      return this.goal.shouldContinue();
   }

   public boolean canStop() {
      return this.goal.canStop();
   }

   public void start() {
      if (!this.running) {
         this.running = true;
         this.goal.start();
      }
   }

   public void stop() {
      if (this.running) {
         this.running = false;
         this.goal.stop();
      }
   }

   public boolean shouldRunEveryTick() {
      return this.goal.shouldRunEveryTick();
   }

   protected int getTickCount(int ticks) {
      return this.goal.getTickCount(ticks);
   }

   public void tick() {
      this.goal.tick();
   }

   public void setControls(EnumSet controls) {
      this.goal.setControls(controls);
   }

   public EnumSet getControls() {
      return this.goal.getControls();
   }

   public boolean isRunning() {
      return this.running;
   }

   public int getPriority() {
      return this.priority;
   }

   public Goal getGoal() {
      return this.goal;
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && this.getClass() == o.getClass() ? this.goal.equals(((PrioritizedGoal)o).goal) : false;
      }
   }

   public int hashCode() {
      return this.goal.hashCode();
   }
}
