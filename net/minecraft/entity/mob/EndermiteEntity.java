package net.minecraft.entity.mob;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.PowderSnowJumpGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class EndermiteEntity extends HostileEntity {
   private static final int DESPAWN_TIME = 2400;
   private int lifeTime;

   public EndermiteEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.experiencePoints = 3;
   }

   protected void initGoals() {
      this.goalSelector.add(1, new SwimGoal(this));
      this.goalSelector.add(1, new PowderSnowJumpGoal(this, this.getWorld()));
      this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
      this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
      this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(8, new LookAroundGoal(this));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[0])).setGroupRevenge());
      this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, true));
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.13F;
   }

   public static DefaultAttributeContainer.Builder createEndermiteAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.EVENTS;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_ENDERMITE_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ENDERMITE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ENDERMITE_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_ENDERMITE_STEP, 0.15F, 1.0F);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.lifeTime = nbt.getInt("Lifetime");
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("Lifetime", this.lifeTime);
   }

   public void tick() {
      this.bodyYaw = this.getYaw();
      super.tick();
   }

   public void setBodyYaw(float bodyYaw) {
      this.setYaw(bodyYaw);
      super.setBodyYaw(bodyYaw);
   }

   public double getHeightOffset() {
      return 0.1;
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.getWorld().isClient) {
         for(int i = 0; i < 2; ++i) {
            this.getWorld().addParticle(ParticleTypes.PORTAL, this.getParticleX(0.5), this.getRandomBodyY(), this.getParticleZ(0.5), (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);
         }
      } else {
         if (!this.isPersistent()) {
            ++this.lifeTime;
         }

         if (this.lifeTime >= 2400) {
            this.discard();
         }
      }

   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random arg5) {
      if (canSpawnIgnoreLightLevel(type, world, spawnReason, pos, arg5)) {
         PlayerEntity lv = world.getClosestPlayer((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 5.0, true);
         return lv == null;
      } else {
         return false;
      }
   }

   public EntityGroup getGroup() {
      return EntityGroup.ARTHROPOD;
   }
}
