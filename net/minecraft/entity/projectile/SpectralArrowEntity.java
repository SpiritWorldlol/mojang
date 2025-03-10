package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

public class SpectralArrowEntity extends PersistentProjectileEntity {
   private int duration = 200;

   public SpectralArrowEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public SpectralArrowEntity(World world, LivingEntity owner) {
      super(EntityType.SPECTRAL_ARROW, owner, world);
   }

   public SpectralArrowEntity(World world, double x, double y, double z) {
      super(EntityType.SPECTRAL_ARROW, x, y, z, world);
   }

   public void tick() {
      super.tick();
      if (this.getWorld().isClient && !this.inGround) {
         this.getWorld().addParticle(ParticleTypes.INSTANT_EFFECT, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
      }

   }

   protected ItemStack asItemStack() {
      return new ItemStack(Items.SPECTRAL_ARROW);
   }

   protected void onHit(LivingEntity target) {
      super.onHit(target);
      StatusEffectInstance lv = new StatusEffectInstance(StatusEffects.GLOWING, this.duration, 0);
      target.addStatusEffect(lv, this.getEffectCause());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("Duration")) {
         this.duration = nbt.getInt("Duration");
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("Duration", this.duration);
   }
}
