package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.sound.SoundEvents;

public class SittingAttackingPhase extends AbstractSittingPhase {
   private static final int DURATION = 40;
   private int ticks;

   public SittingAttackingPhase(EnderDragonEntity arg) {
      super(arg);
   }

   public void clientTick() {
      this.dragon.getWorld().playSound(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, this.dragon.getSoundCategory(), 2.5F, 0.8F + this.dragon.getRandom().nextFloat() * 0.3F, false);
   }

   public void serverTick() {
      if (this.ticks++ >= 40) {
         this.dragon.getPhaseManager().setPhase(PhaseType.SITTING_FLAMING);
      }

   }

   public void beginPhase() {
      this.ticks = 0;
   }

   public PhaseType getType() {
      return PhaseType.SITTING_ATTACKING;
   }
}
