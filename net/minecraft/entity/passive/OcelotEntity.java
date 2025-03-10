package net.minecraft.entity.passive;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class OcelotEntity extends AnimalEntity {
   public static final double CROUCHING_SPEED = 0.6;
   public static final double NORMAL_SPEED = 0.8;
   public static final double SPRINTING_SPEED = 1.33;
   private static final Ingredient TAMING_INGREDIENT;
   private static final TrackedData TRUSTING;
   @Nullable
   private FleeGoal fleeGoal;
   @Nullable
   private OcelotTemptGoal temptGoal;

   public OcelotEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.updateFleeing();
   }

   boolean isTrusting() {
      return (Boolean)this.dataTracker.get(TRUSTING);
   }

   private void setTrusting(boolean trusting) {
      this.dataTracker.set(TRUSTING, trusting);
      this.updateFleeing();
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("Trusting", this.isTrusting());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setTrusting(nbt.getBoolean("Trusting"));
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(TRUSTING, false);
   }

   protected void initGoals() {
      this.temptGoal = new OcelotTemptGoal(this, 0.6, TAMING_INGREDIENT, true);
      this.goalSelector.add(1, new SwimGoal(this));
      this.goalSelector.add(3, this.temptGoal);
      this.goalSelector.add(7, new PounceAtTargetGoal(this, 0.3F));
      this.goalSelector.add(8, new AttackGoal(this));
      this.goalSelector.add(9, new AnimalMateGoal(this, 0.8));
      this.goalSelector.add(10, new WanderAroundFarGoal(this, 0.8, 1.0000001E-5F));
      this.goalSelector.add(11, new LookAtEntityGoal(this, PlayerEntity.class, 10.0F));
      this.targetSelector.add(1, new ActiveTargetGoal(this, ChickenEntity.class, false));
      this.targetSelector.add(1, new ActiveTargetGoal(this, TurtleEntity.class, 10, false, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
   }

   public void mobTick() {
      if (this.getMoveControl().isMoving()) {
         double d = this.getMoveControl().getSpeed();
         if (d == 0.6) {
            this.setPose(EntityPose.CROUCHING);
            this.setSprinting(false);
         } else if (d == 1.33) {
            this.setPose(EntityPose.STANDING);
            this.setSprinting(true);
         } else {
            this.setPose(EntityPose.STANDING);
            this.setSprinting(false);
         }
      } else {
         this.setPose(EntityPose.STANDING);
         this.setSprinting(false);
      }

   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return !this.isTrusting() && this.age > 2400;
   }

   public static DefaultAttributeContainer.Builder createOcelotAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30000001192092896).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_OCELOT_AMBIENT;
   }

   public int getMinAmbientSoundDelay() {
      return 900;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_OCELOT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_OCELOT_DEATH;
   }

   private float getAttackDamage() {
      return (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
   }

   public boolean tryAttack(Entity target) {
      return target.damage(this.getDamageSources().mobAttack(this), this.getAttackDamage());
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if ((this.temptGoal == null || this.temptGoal.isActive()) && !this.isTrusting() && this.isBreedingItem(lv) && player.squaredDistanceTo(this) < 9.0) {
         this.eat(player, hand, lv);
         if (!this.getWorld().isClient) {
            if (this.random.nextInt(3) == 0) {
               this.setTrusting(true);
               this.showEmoteParticle(true);
               this.getWorld().sendEntityStatus(this, EntityStatuses.TAME_OCELOT_SUCCESS);
            } else {
               this.showEmoteParticle(false);
               this.getWorld().sendEntityStatus(this, EntityStatuses.TAME_OCELOT_FAILED);
            }
         }

         return ActionResult.success(this.getWorld().isClient);
      } else {
         return super.interactMob(player, hand);
      }
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.TAME_OCELOT_SUCCESS) {
         this.showEmoteParticle(true);
      } else if (status == EntityStatuses.TAME_OCELOT_FAILED) {
         this.showEmoteParticle(false);
      } else {
         super.handleStatus(status);
      }

   }

   private void showEmoteParticle(boolean positive) {
      ParticleEffect lv = ParticleTypes.HEART;
      if (!positive) {
         lv = ParticleTypes.SMOKE;
      }

      for(int i = 0; i < 7; ++i) {
         double d = this.random.nextGaussian() * 0.02;
         double e = this.random.nextGaussian() * 0.02;
         double f = this.random.nextGaussian() * 0.02;
         this.getWorld().addParticle(lv, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
      }

   }

   protected void updateFleeing() {
      if (this.fleeGoal == null) {
         this.fleeGoal = new FleeGoal(this, PlayerEntity.class, 16.0F, 0.8, 1.33);
      }

      this.goalSelector.remove(this.fleeGoal);
      if (!this.isTrusting()) {
         this.goalSelector.add(4, this.fleeGoal);
      }

   }

   @Nullable
   public OcelotEntity createChild(ServerWorld arg, PassiveEntity arg2) {
      return (OcelotEntity)EntityType.OCELOT.create(arg);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return TAMING_INGREDIENT.test(stack);
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return random.nextInt(3) != 0;
   }

   public boolean canSpawn(WorldView world) {
      if (world.doesNotIntersectEntities(this) && !world.containsFluid(this.getBoundingBox())) {
         BlockPos lv = this.getBlockPos();
         if (lv.getY() < world.getSeaLevel()) {
            return false;
         }

         BlockState lv2 = world.getBlockState(lv.down());
         if (lv2.isOf(Blocks.GRASS_BLOCK) || lv2.isIn(BlockTags.LEAVES)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      if (entityData == null) {
         entityData = new PassiveEntity.PassiveData(1.0F);
      }

      return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityNbt);
   }

   public Vec3d getLeashOffset() {
      return new Vec3d(0.0, (double)(0.5F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
   }

   public boolean bypassesSteppingEffects() {
      return this.isInSneakingPose() || super.bypassesSteppingEffects();
   }

   // $FF: synthetic method
   @Nullable
   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return this.createChild(world, entity);
   }

   static {
      TAMING_INGREDIENT = Ingredient.ofItems(Items.COD, Items.SALMON);
      TRUSTING = DataTracker.registerData(OcelotEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }

   private static class OcelotTemptGoal extends TemptGoal {
      private final OcelotEntity ocelot;

      public OcelotTemptGoal(OcelotEntity ocelot, double speed, Ingredient food, boolean canBeScared) {
         super(ocelot, speed, food, canBeScared);
         this.ocelot = ocelot;
      }

      protected boolean canBeScared() {
         return super.canBeScared() && !this.ocelot.isTrusting();
      }
   }

   private static class FleeGoal extends FleeEntityGoal {
      private final OcelotEntity ocelot;

      public FleeGoal(OcelotEntity ocelot, Class fleeFromType, float distance, double slowSpeed, double fastSpeed) {
         Predicate var10006 = EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR;
         Objects.requireNonNull(var10006);
         super(ocelot, fleeFromType, distance, slowSpeed, fastSpeed, var10006::test);
         this.ocelot = ocelot;
      }

      public boolean canStart() {
         return !this.ocelot.isTrusting() && super.canStart();
      }

      public boolean shouldContinue() {
         return !this.ocelot.isTrusting() && super.shouldContinue();
      }
   }
}
