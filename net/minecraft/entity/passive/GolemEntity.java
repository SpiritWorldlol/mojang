package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class GolemEntity extends PathAwareEntity {
   protected GolemEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return null;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return null;
   }

   public int getMinAmbientSoundDelay() {
      return 120;
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return false;
   }
}
