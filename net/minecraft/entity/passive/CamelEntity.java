package net.minecraft.entity.passive;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.AttackPosOffsettingMount;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CamelEntity extends AbstractHorseEntity implements JumpingMount, AttackPosOffsettingMount, Saddleable {
   public static final Ingredient BREEDING_INGREDIENT;
   public static final int field_40132 = 55;
   public static final int field_41764 = 30;
   private static final float field_40146 = 0.1F;
   private static final float field_40147 = 1.4285F;
   private static final float field_40148 = 22.2222F;
   private static final int field_43388 = 5;
   private static final int field_40149 = 40;
   private static final int field_40133 = 52;
   private static final int field_40134 = 80;
   private static final float field_40135 = 1.43F;
   public static final TrackedData DASHING;
   public static final TrackedData LAST_POSE_TICK;
   public final AnimationState sittingTransitionAnimationState = new AnimationState();
   public final AnimationState sittingAnimationState = new AnimationState();
   public final AnimationState standingTransitionAnimationState = new AnimationState();
   public final AnimationState idlingAnimationState = new AnimationState();
   public final AnimationState dashingAnimationState = new AnimationState();
   private static final EntityDimensions SITTING_DIMENSIONS;
   private int dashCooldown = 0;
   private int idleAnimationCooldown = 0;

   public CamelEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.setStepHeight(1.5F);
      this.moveControl = new CamelMoveControl();
      MobNavigation lv = (MobNavigation)this.getNavigation();
      lv.setCanSwim(true);
      lv.setCanWalkOverFences(true);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putLong("LastPoseTick", (Long)this.dataTracker.get(LAST_POSE_TICK));
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      long l = nbt.getLong("LastPoseTick");
      if (l < 0L) {
         this.setPose(EntityPose.SITTING);
      }

      this.setLastPoseTick(l);
   }

   public static DefaultAttributeContainer.Builder createCamelAttributes() {
      return createBaseHorseAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 32.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.09000000357627869).add(EntityAttributes.HORSE_JUMP_STRENGTH, 0.41999998688697815);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(DASHING, false);
      this.dataTracker.startTracking(LAST_POSE_TICK, 0L);
   }

   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      CamelBrain.initialize(this, world.getRandom());
      this.initLastPoseTick(world.toServerWorld().getTime());
      return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
   }

   protected Brain.Profile createBrainProfile() {
      return CamelBrain.createProfile();
   }

   protected void initGoals() {
   }

   protected Brain deserializeBrain(Dynamic dynamic) {
      return CamelBrain.create(this.createBrainProfile().deserialize(dynamic));
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return pose == EntityPose.SITTING ? SITTING_DIMENSIONS.scaled(this.getScaleFactor()) : super.getDimensions(pose);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height - 0.1F;
   }

   public double getPassengerAttackYOffset() {
      return 0.5;
   }

   protected void mobTick() {
      this.getWorld().getProfiler().push("camelBrain");
      Brain lv = this.getBrain();
      lv.tick((ServerWorld)this.getWorld(), this);
      this.getWorld().getProfiler().pop();
      this.getWorld().getProfiler().push("camelActivityUpdate");
      CamelBrain.updateActivities(this);
      this.getWorld().getProfiler().pop();
      super.mobTick();
   }

   public void tick() {
      super.tick();
      if (this.isDashing() && this.dashCooldown < 50 && (this.isOnGround() || this.isTouchingWater() || this.hasVehicle())) {
         this.setDashing(false);
      }

      if (this.dashCooldown > 0) {
         --this.dashCooldown;
         if (this.dashCooldown == 0) {
            this.getWorld().playSound((PlayerEntity)null, this.getBlockPos(), SoundEvents.ENTITY_CAMEL_DASH_READY, SoundCategory.NEUTRAL, 1.0F, 1.0F);
         }
      }

      if (this.getWorld().isClient()) {
         this.updateAnimations();
      }

      if (this.isStationary()) {
         this.clampHeadYaw(this, 30.0F);
      }

      if (this.isSitting() && this.isTouchingWater()) {
         this.setStanding();
      }

   }

   private void updateAnimations() {
      if (this.idleAnimationCooldown <= 0) {
         this.idleAnimationCooldown = this.random.nextInt(40) + 80;
         this.idlingAnimationState.start(this.age);
      } else {
         --this.idleAnimationCooldown;
      }

      if (this.shouldUpdateSittingAnimations()) {
         this.standingTransitionAnimationState.stop();
         this.dashingAnimationState.stop();
         if (this.shouldPlaySittingTransitionAnimation()) {
            this.sittingTransitionAnimationState.startIfNotRunning(this.age);
            this.sittingAnimationState.stop();
         } else {
            this.sittingTransitionAnimationState.stop();
            this.sittingAnimationState.startIfNotRunning(this.age);
         }
      } else {
         this.sittingTransitionAnimationState.stop();
         this.sittingAnimationState.stop();
         this.dashingAnimationState.setRunning(this.isDashing(), this.age);
         this.standingTransitionAnimationState.setRunning(this.isChangingPose() && this.getLastPoseTickDelta() >= 0L, this.age);
      }

   }

   protected void updateLimbs(float posDelta) {
      float g;
      if (this.getPose() == EntityPose.STANDING && !this.dashingAnimationState.isRunning()) {
         g = Math.min(posDelta * 6.0F, 1.0F);
      } else {
         g = 0.0F;
      }

      this.limbAnimator.updateLimbs(g, 0.2F);
   }

   public void travel(Vec3d movementInput) {
      if (this.isStationary() && this.isOnGround()) {
         this.setVelocity(this.getVelocity().multiply(0.0, 1.0, 0.0));
         movementInput = movementInput.multiply(0.0, 1.0, 0.0);
      }

      super.travel(movementInput);
   }

   protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
      super.tickControlled(controllingPlayer, movementInput);
      if (controllingPlayer.forwardSpeed > 0.0F && this.isSitting() && !this.isChangingPose()) {
         this.startStanding();
      }

   }

   public boolean isStationary() {
      return this.isSitting() || this.isChangingPose();
   }

   protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
      float f = controllingPlayer.isSprinting() && this.getJumpCooldown() == 0 ? 0.1F : 0.0F;
      return (float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) + f;
   }

   protected Vec2f getControlledRotation(LivingEntity controllingPassenger) {
      return this.isStationary() ? new Vec2f(this.getPitch(), this.getYaw()) : super.getControlledRotation(controllingPassenger);
   }

   protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
      return this.isStationary() ? Vec3d.ZERO : super.getControlledMovementInput(controllingPlayer, movementInput);
   }

   public boolean canJump() {
      return !this.isStationary() && super.canJump();
   }

   public void setJumpStrength(int strength) {
      if (this.isSaddled() && this.dashCooldown <= 0 && this.isOnGround()) {
         super.setJumpStrength(strength);
      }
   }

   public boolean canSprintAsVehicle() {
      return true;
   }

   protected void jump(float strength, Vec3d movementInput) {
      double d = this.getAttributeValue(EntityAttributes.HORSE_JUMP_STRENGTH) * (double)this.getJumpVelocityMultiplier() + (double)this.getJumpBoostVelocityModifier();
      this.addVelocity(this.getRotationVector().multiply(1.0, 0.0, 1.0).normalize().multiply((double)(22.2222F * strength) * this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * (double)this.getVelocityMultiplier()).add(0.0, (double)(1.4285F * strength) * d, 0.0));
      this.dashCooldown = 55;
      this.setDashing(true);
      this.velocityDirty = true;
   }

   public boolean isDashing() {
      return (Boolean)this.dataTracker.get(DASHING);
   }

   public void setDashing(boolean dashing) {
      this.dataTracker.set(DASHING, dashing);
   }

   public boolean isPanicking() {
      return this.getBrain().isMemoryInState(MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_PRESENT);
   }

   public void startJumping(int height) {
      this.playSound(SoundEvents.ENTITY_CAMEL_DASH, 1.0F, 1.0F);
      this.setDashing(true);
   }

   public void stopJumping() {
   }

   public int getJumpCooldown() {
      return this.dashCooldown;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_CAMEL_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_CAMEL_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_CAMEL_HURT;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      if (state.getSoundGroup() == BlockSoundGroup.SAND) {
         this.playSound(SoundEvents.ENTITY_CAMEL_STEP_SAND, 1.0F, 1.0F);
      } else {
         this.playSound(SoundEvents.ENTITY_CAMEL_STEP, 1.0F, 1.0F);
      }

   }

   public boolean isBreedingItem(ItemStack stack) {
      return BREEDING_INGREDIENT.test(stack);
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (player.shouldCancelInteraction() && !this.isBaby()) {
         this.openInventory(player);
         return ActionResult.success(this.getWorld().isClient);
      } else {
         ActionResult lv2 = lv.useOnEntity(player, this, hand);
         if (lv2.isAccepted()) {
            return lv2;
         } else if (this.isBreedingItem(lv)) {
            return this.interactHorse(player, lv);
         } else {
            if (this.getPassengerList().size() < 2 && !this.isBaby()) {
               this.putPlayerOnBack(player);
            }

            return ActionResult.success(this.getWorld().isClient);
         }
      }
   }

   protected void updateForLeashLength(float leashLength) {
      if (leashLength > 6.0F && this.isSitting() && !this.isChangingPose()) {
         this.startStanding();
      }

   }

   protected boolean receiveFood(PlayerEntity player, ItemStack item) {
      if (!this.isBreedingItem(item)) {
         return false;
      } else {
         boolean bl = this.getHealth() < this.getMaxHealth();
         if (bl) {
            this.heal(2.0F);
         }

         boolean bl2 = this.isTame() && this.getBreedingAge() == 0 && this.canEat();
         if (bl2) {
            this.lovePlayer(player);
         }

         boolean bl3 = this.isBaby();
         if (bl3) {
            this.getWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 0.0, 0.0, 0.0);
            if (!this.getWorld().isClient) {
               this.growUp(10);
            }
         }

         if (!bl && !bl2 && !bl3) {
            return false;
         } else {
            if (!this.isSilent()) {
               SoundEvent lv = this.getEatSound();
               if (lv != null) {
                  this.getWorld().playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), lv, this.getSoundCategory(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
               }
            }

            return true;
         }
      }
   }

   protected boolean shouldAmbientStand() {
      return false;
   }

   public boolean canBreedWith(AnimalEntity other) {
      boolean var10000;
      if (other != this && other instanceof CamelEntity lv) {
         if (this.canBreed() && lv.canBreed()) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   @Nullable
   public CamelEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      return (CamelEntity)EntityType.CAMEL.create(arg);
   }

   @Nullable
   protected SoundEvent getEatSound() {
      return SoundEvents.ENTITY_CAMEL_EAT;
   }

   protected void applyDamage(DamageSource source, float amount) {
      this.setStanding();
      super.applyDamage(source, amount);
   }

   public void updatePassengerPosition(Entity passenger) {
      int i = this.getPassengerList().indexOf(passenger);
      if (i >= 0) {
         boolean bl = i == 0;
         float f = 0.5F;
         float g = (float)(this.isRemoved() ? 0.009999999776482582 : this.method_45346(bl, 0.0F) + passenger.getHeightOffset());
         if (this.getPassengerList().size() > 1) {
            if (!bl) {
               f = -0.7F;
            }

            if (passenger instanceof AnimalEntity) {
               f += 0.2F;
            }
         }

         Vec3d lv = (new Vec3d(0.0, 0.0, (double)f)).rotateY(-this.bodyYaw * 0.017453292F);
         passenger.setPosition(this.getX() + lv.x, this.getY() + (double)g, this.getZ() + lv.z);
         this.clampPassengerYaw(passenger);
      }
   }

   private double method_45346(boolean bl, float f) {
      double d = this.getMountedHeightOffset();
      float g = this.getScaleFactor() * 1.43F;
      float h = g - this.getScaleFactor() * 0.2F;
      float i = g - h;
      boolean bl2 = this.isChangingPose();
      boolean bl3 = this.isSitting();
      if (bl2) {
         int j = bl3 ? 40 : 52;
         int k;
         float l;
         if (bl3) {
            k = 28;
            l = bl ? 0.5F : 0.1F;
         } else {
            k = bl ? 24 : 32;
            l = bl ? 0.6F : 0.35F;
         }

         float m = MathHelper.clamp((float)this.getLastPoseTickDelta() + f, 0.0F, (float)j);
         boolean bl4 = m < (float)k;
         float n = bl4 ? m / (float)k : (m - (float)k) / (float)(j - k);
         float o = g - l * h;
         d += bl3 ? (double)MathHelper.lerp(n, bl4 ? g : o, bl4 ? o : i) : (double)MathHelper.lerp(n, bl4 ? i - g : i - o, bl4 ? i - o : 0.0F);
      }

      if (bl3 && !bl2) {
         d += (double)i;
      }

      return d;
   }

   public Vec3d getLeashOffset(float tickDelta) {
      return new Vec3d(0.0, this.method_45346(true, tickDelta) - (double)(0.2F * this.getScaleFactor()), (double)(this.getWidth() * 0.56F));
   }

   public double getMountedHeightOffset() {
      return (double)(this.getDimensions(this.isSitting() ? EntityPose.SITTING : EntityPose.STANDING).height - (this.isBaby() ? 0.35F : 0.6F));
   }

   public void onPassengerLookAround(Entity passenger) {
      if (this.getControllingPassenger() != passenger) {
         this.clampPassengerYaw(passenger);
      }

   }

   private void clampPassengerYaw(Entity passenger) {
      passenger.setBodyYaw(this.getYaw());
      float f = passenger.getYaw();
      float g = MathHelper.wrapDegrees(f - this.getYaw());
      float h = MathHelper.clamp(g, -160.0F, 160.0F);
      passenger.prevYaw += h - g;
      float i = f + h - g;
      passenger.setYaw(i);
      passenger.setHeadYaw(i);
   }

   private void clampHeadYaw(Entity entity, float range) {
      float g = entity.getHeadYaw();
      float h = MathHelper.wrapDegrees(this.bodyYaw - g);
      float i = MathHelper.clamp(MathHelper.wrapDegrees(this.bodyYaw - g), -range, range);
      float j = g + h - i;
      entity.setHeadYaw(j);
   }

   public int getMaxHeadRotation() {
      return 30;
   }

   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengerList().size() <= 2;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      if (!this.getPassengerList().isEmpty() && this.isSaddled()) {
         Entity lv = (Entity)this.getPassengerList().get(0);
         if (lv instanceof LivingEntity) {
            LivingEntity lv2 = (LivingEntity)lv;
            return lv2;
         }
      }

      return null;
   }

   protected void sendAiDebugData() {
      super.sendAiDebugData();
      DebugInfoSender.sendBrainDebugData(this);
   }

   public boolean isSitting() {
      return (Long)this.dataTracker.get(LAST_POSE_TICK) < 0L;
   }

   public boolean shouldUpdateSittingAnimations() {
      return this.getLastPoseTickDelta() < 0L != this.isSitting();
   }

   public boolean isChangingPose() {
      long l = this.getLastPoseTickDelta();
      return l < (long)(this.isSitting() ? 40 : 52);
   }

   private boolean shouldPlaySittingTransitionAnimation() {
      return this.isSitting() && this.getLastPoseTickDelta() < 40L && this.getLastPoseTickDelta() >= 0L;
   }

   public void startSitting() {
      if (!this.isSitting()) {
         this.playSound(SoundEvents.ENTITY_CAMEL_SIT, 1.0F, 1.0F);
         this.setPose(EntityPose.SITTING);
         this.setLastPoseTick(-this.getWorld().getTime());
      }
   }

   public void startStanding() {
      if (this.isSitting()) {
         this.playSound(SoundEvents.ENTITY_CAMEL_STAND, 1.0F, 1.0F);
         this.setPose(EntityPose.STANDING);
         this.setLastPoseTick(this.getWorld().getTime());
      }
   }

   public void setStanding() {
      this.setPose(EntityPose.STANDING);
      this.initLastPoseTick(this.getWorld().getTime());
   }

   @VisibleForTesting
   public void setLastPoseTick(long lastPoseTick) {
      this.dataTracker.set(LAST_POSE_TICK, lastPoseTick);
   }

   private void initLastPoseTick(long time) {
      this.setLastPoseTick(Math.max(0L, time - 52L - 1L));
   }

   public long getLastPoseTickDelta() {
      return this.getWorld().getTime() - Math.abs((Long)this.dataTracker.get(LAST_POSE_TICK));
   }

   public SoundEvent getSaddleSound() {
      return SoundEvents.ENTITY_CAMEL_SADDLE;
   }

   public void onTrackedDataSet(TrackedData data) {
      if (!this.firstUpdate && DASHING.equals(data)) {
         this.dashCooldown = this.dashCooldown == 0 ? 55 : this.dashCooldown;
      }

      super.onTrackedDataSet(data);
   }

   protected BodyControl createBodyControl() {
      return new CamelBodyControl(this);
   }

   public boolean isTame() {
      return true;
   }

   public void openInventory(PlayerEntity player) {
      if (!this.getWorld().isClient) {
         player.openHorseInventory(this, this.items);
      }

   }

   // $FF: synthetic method
   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return this.createChild(world, entity);
   }

   static {
      BREEDING_INGREDIENT = Ingredient.ofItems(Items.CACTUS);
      DASHING = DataTracker.registerData(CamelEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      LAST_POSE_TICK = DataTracker.registerData(CamelEntity.class, TrackedDataHandlerRegistry.LONG);
      SITTING_DIMENSIONS = EntityDimensions.changing(EntityType.CAMEL.getWidth(), EntityType.CAMEL.getHeight() - 1.43F);
   }

   class CamelMoveControl extends MoveControl {
      public CamelMoveControl() {
         super(CamelEntity.this);
      }

      public void tick() {
         if (this.state == MoveControl.State.MOVE_TO && !CamelEntity.this.isLeashed() && CamelEntity.this.isSitting() && !CamelEntity.this.isChangingPose()) {
            CamelEntity.this.startStanding();
         }

         super.tick();
      }
   }

   private class CamelBodyControl extends BodyControl {
      public CamelBodyControl(CamelEntity camel) {
         super(camel);
      }

      public void tick() {
         if (!CamelEntity.this.isStationary()) {
            super.tick();
         }

      }
   }
}
