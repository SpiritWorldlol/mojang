package net.minecraft.entity.passive;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ChickenEntity extends AnimalEntity {
   private static final Ingredient BREEDING_INGREDIENT;
   public float flapProgress;
   public float maxWingDeviation;
   public float prevMaxWingDeviation;
   public float prevFlapProgress;
   public float flapSpeed = 1.0F;
   private float field_28639 = 1.0F;
   public int eggLayTime;
   public boolean hasJockey;

   public ChickenEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.eggLayTime = this.random.nextInt(6000) + 6000;
      this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
   }

   protected void initGoals() {
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new EscapeDangerGoal(this, 1.4));
      this.goalSelector.add(2, new AnimalMateGoal(this, 1.0));
      this.goalSelector.add(3, new TemptGoal(this, 1.0, BREEDING_INGREDIENT, false));
      this.goalSelector.add(4, new FollowParentGoal(this, 1.1));
      this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
      this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(7, new LookAroundGoal(this));
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return this.isBaby() ? dimensions.height * 0.85F : dimensions.height * 0.92F;
   }

   public static DefaultAttributeContainer.Builder createChickenAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 4.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
   }

   public void tickMovement() {
      super.tickMovement();
      this.prevFlapProgress = this.flapProgress;
      this.prevMaxWingDeviation = this.maxWingDeviation;
      this.maxWingDeviation += (this.isOnGround() ? -1.0F : 4.0F) * 0.3F;
      this.maxWingDeviation = MathHelper.clamp(this.maxWingDeviation, 0.0F, 1.0F);
      if (!this.isOnGround() && this.flapSpeed < 1.0F) {
         this.flapSpeed = 1.0F;
      }

      this.flapSpeed *= 0.9F;
      Vec3d lv = this.getVelocity();
      if (!this.isOnGround() && lv.y < 0.0) {
         this.setVelocity(lv.multiply(1.0, 0.6, 1.0));
      }

      this.flapProgress += this.flapSpeed * 2.0F;
      if (!this.getWorld().isClient && this.isAlive() && !this.isBaby() && !this.hasJockey() && --this.eggLayTime <= 0) {
         this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         this.dropItem(Items.EGG);
         this.emitGameEvent(GameEvent.ENTITY_PLACE);
         this.eggLayTime = this.random.nextInt(6000) + 6000;
      }

   }

   protected boolean isFlappingWings() {
      return this.speed > this.field_28639;
   }

   protected void addFlapEffects() {
      this.field_28639 = this.speed + this.maxWingDeviation / 2.0F;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_CHICKEN_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_CHICKEN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_CHICKEN_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
   }

   @Nullable
   public ChickenEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      return (ChickenEntity)EntityType.CHICKEN.create(arg);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return BREEDING_INGREDIENT.test(stack);
   }

   public int getXpToDrop() {
      return this.hasJockey() ? 10 : super.getXpToDrop();
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.hasJockey = nbt.getBoolean("IsChickenJockey");
      if (nbt.contains("EggLayTime")) {
         this.eggLayTime = nbt.getInt("EggLayTime");
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("IsChickenJockey", this.hasJockey);
      nbt.putInt("EggLayTime", this.eggLayTime);
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return this.hasJockey();
   }

   public void updatePassengerPosition(Entity passenger) {
      super.updatePassengerPosition(passenger);
      float f = MathHelper.sin(this.bodyYaw * 0.017453292F);
      float g = MathHelper.cos(this.bodyYaw * 0.017453292F);
      float h = 0.1F;
      float i = 0.0F;
      passenger.setPosition(this.getX() + (double)(0.1F * f), this.getBodyY(0.5) + passenger.getHeightOffset() + 0.0, this.getZ() - (double)(0.1F * g));
      if (passenger instanceof LivingEntity) {
         ((LivingEntity)passenger).bodyYaw = this.bodyYaw;
      }

   }

   public boolean hasJockey() {
      return this.hasJockey;
   }

   public void setHasJockey(boolean hasJockey) {
      this.hasJockey = hasJockey;
   }

   // $FF: synthetic method
   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return this.createChild(world, entity);
   }

   static {
      BREEDING_INGREDIENT = Ingredient.ofItems(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD);
   }
}
