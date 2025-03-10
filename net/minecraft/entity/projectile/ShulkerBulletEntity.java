package net.minecraft.entity.projectile;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ShulkerBulletEntity extends ProjectileEntity {
   private static final double field_30666 = 0.15;
   @Nullable
   private Entity target;
   @Nullable
   private Direction direction;
   private int stepCount;
   private double targetX;
   private double targetY;
   private double targetZ;
   @Nullable
   private UUID targetUuid;

   public ShulkerBulletEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.noClip = true;
   }

   public ShulkerBulletEntity(World world, LivingEntity owner, Entity target, Direction.Axis axis) {
      this(EntityType.SHULKER_BULLET, world);
      this.setOwner(owner);
      BlockPos lv = owner.getBlockPos();
      double d = (double)lv.getX() + 0.5;
      double e = (double)lv.getY() + 0.5;
      double f = (double)lv.getZ() + 0.5;
      this.refreshPositionAndAngles(d, e, f, this.getYaw(), this.getPitch());
      this.target = target;
      this.direction = Direction.UP;
      this.changeTargetDirection(axis);
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (this.target != null) {
         nbt.putUuid("Target", this.target.getUuid());
      }

      if (this.direction != null) {
         nbt.putInt("Dir", this.direction.getId());
      }

      nbt.putInt("Steps", this.stepCount);
      nbt.putDouble("TXD", this.targetX);
      nbt.putDouble("TYD", this.targetY);
      nbt.putDouble("TZD", this.targetZ);
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.stepCount = nbt.getInt("Steps");
      this.targetX = nbt.getDouble("TXD");
      this.targetY = nbt.getDouble("TYD");
      this.targetZ = nbt.getDouble("TZD");
      if (nbt.contains("Dir", NbtElement.NUMBER_TYPE)) {
         this.direction = Direction.byId(nbt.getInt("Dir"));
      }

      if (nbt.containsUuid("Target")) {
         this.targetUuid = nbt.getUuid("Target");
      }

   }

   protected void initDataTracker() {
   }

   @Nullable
   private Direction getDirection() {
      return this.direction;
   }

   private void setDirection(@Nullable Direction direction) {
      this.direction = direction;
   }

   private void changeTargetDirection(@Nullable Direction.Axis axis) {
      double d = 0.5;
      BlockPos lv;
      if (this.target == null) {
         lv = this.getBlockPos().down();
      } else {
         d = (double)this.target.getHeight() * 0.5;
         lv = BlockPos.ofFloored(this.target.getX(), this.target.getY() + d, this.target.getZ());
      }

      double e = (double)lv.getX() + 0.5;
      double f = (double)lv.getY() + d;
      double g = (double)lv.getZ() + 0.5;
      Direction lv2 = null;
      if (!lv.isWithinDistance(this.getPos(), 2.0)) {
         BlockPos lv3 = this.getBlockPos();
         List list = Lists.newArrayList();
         if (axis != Direction.Axis.X) {
            if (lv3.getX() < lv.getX() && this.getWorld().isAir(lv3.east())) {
               list.add(Direction.EAST);
            } else if (lv3.getX() > lv.getX() && this.getWorld().isAir(lv3.west())) {
               list.add(Direction.WEST);
            }
         }

         if (axis != Direction.Axis.Y) {
            if (lv3.getY() < lv.getY() && this.getWorld().isAir(lv3.up())) {
               list.add(Direction.UP);
            } else if (lv3.getY() > lv.getY() && this.getWorld().isAir(lv3.down())) {
               list.add(Direction.DOWN);
            }
         }

         if (axis != Direction.Axis.Z) {
            if (lv3.getZ() < lv.getZ() && this.getWorld().isAir(lv3.south())) {
               list.add(Direction.SOUTH);
            } else if (lv3.getZ() > lv.getZ() && this.getWorld().isAir(lv3.north())) {
               list.add(Direction.NORTH);
            }
         }

         lv2 = Direction.random(this.random);
         if (list.isEmpty()) {
            for(int i = 5; !this.getWorld().isAir(lv3.offset(lv2)) && i > 0; --i) {
               lv2 = Direction.random(this.random);
            }
         } else {
            lv2 = (Direction)list.get(this.random.nextInt(list.size()));
         }

         e = this.getX() + (double)lv2.getOffsetX();
         f = this.getY() + (double)lv2.getOffsetY();
         g = this.getZ() + (double)lv2.getOffsetZ();
      }

      this.setDirection(lv2);
      double h = e - this.getX();
      double j = f - this.getY();
      double k = g - this.getZ();
      double l = Math.sqrt(h * h + j * j + k * k);
      if (l == 0.0) {
         this.targetX = 0.0;
         this.targetY = 0.0;
         this.targetZ = 0.0;
      } else {
         this.targetX = h / l * 0.15;
         this.targetY = j / l * 0.15;
         this.targetZ = k / l * 0.15;
      }

      this.velocityDirty = true;
      this.stepCount = 10 + this.random.nextInt(5) * 10;
   }

   public void checkDespawn() {
      if (this.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
         this.discard();
      }

   }

   public void tick() {
      super.tick();
      Vec3d lv;
      if (!this.getWorld().isClient) {
         if (this.target == null && this.targetUuid != null) {
            this.target = ((ServerWorld)this.getWorld()).getEntity(this.targetUuid);
            if (this.target == null) {
               this.targetUuid = null;
            }
         }

         if (this.target == null || !this.target.isAlive() || this.target instanceof PlayerEntity && this.target.isSpectator()) {
            if (!this.hasNoGravity()) {
               this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
            }
         } else {
            this.targetX = MathHelper.clamp(this.targetX * 1.025, -1.0, 1.0);
            this.targetY = MathHelper.clamp(this.targetY * 1.025, -1.0, 1.0);
            this.targetZ = MathHelper.clamp(this.targetZ * 1.025, -1.0, 1.0);
            lv = this.getVelocity();
            this.setVelocity(lv.add((this.targetX - lv.x) * 0.2, (this.targetY - lv.y) * 0.2, (this.targetZ - lv.z) * 0.2));
         }

         HitResult lv2 = ProjectileUtil.getCollision(this, this::canHit);
         if (lv2.getType() != HitResult.Type.MISS) {
            this.onCollision(lv2);
         }
      }

      this.checkBlockCollision();
      lv = this.getVelocity();
      this.setPosition(this.getX() + lv.x, this.getY() + lv.y, this.getZ() + lv.z);
      ProjectileUtil.setRotationFromVelocity(this, 0.5F);
      if (this.getWorld().isClient) {
         this.getWorld().addParticle(ParticleTypes.END_ROD, this.getX() - lv.x, this.getY() - lv.y + 0.15, this.getZ() - lv.z, 0.0, 0.0, 0.0);
      } else if (this.target != null && !this.target.isRemoved()) {
         if (this.stepCount > 0) {
            --this.stepCount;
            if (this.stepCount == 0) {
               this.changeTargetDirection(this.direction == null ? null : this.direction.getAxis());
            }
         }

         if (this.direction != null) {
            BlockPos lv3 = this.getBlockPos();
            Direction.Axis lv4 = this.direction.getAxis();
            if (this.getWorld().isTopSolid(lv3.offset(this.direction), this)) {
               this.changeTargetDirection(lv4);
            } else {
               BlockPos lv5 = this.target.getBlockPos();
               if (lv4 == Direction.Axis.X && lv3.getX() == lv5.getX() || lv4 == Direction.Axis.Z && lv3.getZ() == lv5.getZ() || lv4 == Direction.Axis.Y && lv3.getY() == lv5.getY()) {
                  this.changeTargetDirection(lv4);
               }
            }
         }
      }

   }

   protected boolean canHit(Entity entity) {
      return super.canHit(entity) && !entity.noClip;
   }

   public boolean isOnFire() {
      return false;
   }

   public boolean shouldRender(double distance) {
      return distance < 16384.0;
   }

   public float getBrightnessAtEyes() {
      return 1.0F;
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      Entity lv = entityHitResult.getEntity();
      Entity lv2 = this.getOwner();
      LivingEntity lv3 = lv2 instanceof LivingEntity ? (LivingEntity)lv2 : null;
      boolean bl = lv.damage(this.getDamageSources().mobProjectile(this, lv3), 4.0F);
      if (bl) {
         this.applyDamageEffects(lv3, lv);
         if (lv instanceof LivingEntity) {
            LivingEntity lv4 = (LivingEntity)lv;
            lv4.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 200), (Entity)MoreObjects.firstNonNull(lv2, this));
         }
      }

   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      super.onBlockHit(blockHitResult);
      ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
      this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HIT, 1.0F, 1.0F);
   }

   private void destroy() {
      this.discard();
      this.getWorld().emitGameEvent(GameEvent.ENTITY_DAMAGE, this.getPos(), GameEvent.Emitter.of((Entity)this));
   }

   protected void onCollision(HitResult hitResult) {
      super.onCollision(hitResult);
      this.destroy();
   }

   public boolean canHit() {
      return true;
   }

   public boolean damage(DamageSource source, float amount) {
      if (!this.getWorld().isClient) {
         this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HURT, 1.0F, 1.0F);
         ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2, 0.2, 0.2, 0.0);
         this.destroy();
      }

      return true;
   }

   public void onSpawnPacket(EntitySpawnS2CPacket packet) {
      super.onSpawnPacket(packet);
      double d = packet.getVelocityX();
      double e = packet.getVelocityY();
      double f = packet.getVelocityZ();
      this.setVelocity(d, e, f);
   }
}
