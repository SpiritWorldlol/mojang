package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class FireballEntity extends AbstractFireballEntity {
   private int explosionPower = 1;

   public FireballEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public FireballEntity(World world, LivingEntity owner, double velocityX, double velocityY, double velocityZ, int explosionPower) {
      super(EntityType.FIREBALL, owner, velocityX, velocityY, velocityZ, world);
      this.explosionPower = explosionPower;
   }

   protected void onCollision(HitResult hitResult) {
      super.onCollision(hitResult);
      if (!this.getWorld().isClient) {
         boolean bl = this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
         this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, bl, World.ExplosionSourceType.MOB);
         this.discard();
      }

   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      if (!this.getWorld().isClient) {
         Entity lv = entityHitResult.getEntity();
         Entity lv2 = this.getOwner();
         lv.damage(this.getDamageSources().fireball(this, lv2), 6.0F);
         if (lv2 instanceof LivingEntity) {
            this.applyDamageEffects((LivingEntity)lv2, lv);
         }

      }
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putByte("ExplosionPower", (byte)this.explosionPower);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("ExplosionPower", NbtElement.NUMBER_TYPE)) {
         this.explosionPower = nbt.getByte("ExplosionPower");
      }

   }
}
