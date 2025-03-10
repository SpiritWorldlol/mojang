package net.minecraft.entity.mob;

import java.util.UUID;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class ZombifiedPiglinEntity extends ZombieEntity implements Angerable {
   private static final UUID ATTACKING_SPEED_BOOST_ID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
   private static final EntityAttributeModifier ATTACKING_SPEED_BOOST;
   private static final UniformIntProvider ANGRY_SOUND_DELAY_RANGE;
   private int angrySoundDelay;
   private static final UniformIntProvider ANGER_TIME_RANGE;
   private int angerTime;
   @Nullable
   private UUID angryAt;
   private static final int field_30524 = 10;
   private static final UniformIntProvider ANGER_PASSING_COOLDOWN_RANGE;
   private int angerPassingCooldown;
   private static final float EYE_HEIGHT = 1.79F;
   private static final float BABY_EYE_HEIGHT_OFFSET = 0.82F;

   public ZombifiedPiglinEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.setPathfindingPenalty(PathNodeType.LAVA, 8.0F);
   }

   public void setAngryAt(@Nullable UUID angryAt) {
      this.angryAt = angryAt;
   }

   public double getHeightOffset() {
      return this.isBaby() ? -0.05 : -0.45;
   }

   protected void initCustomGoals() {
      this.goalSelector.add(2, new ZombieAttackGoal(this, 1.0, false));
      this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[0])).setGroupRevenge());
      this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
      this.targetSelector.add(3, new UniversalAngerGoal(this, true));
   }

   public static DefaultAttributeContainer.Builder createZombifiedPiglinAttributes() {
      return ZombieEntity.createZombieAttributes().add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 0.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23000000417232513).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return this.isBaby() ? 0.96999997F : 1.79F;
   }

   protected boolean canConvertInWater() {
      return false;
   }

   protected void mobTick() {
      EntityAttributeInstance lv = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (this.hasAngerTime()) {
         if (!this.isBaby() && !lv.hasModifier(ATTACKING_SPEED_BOOST)) {
            lv.addTemporaryModifier(ATTACKING_SPEED_BOOST);
         }

         this.tickAngrySound();
      } else if (lv.hasModifier(ATTACKING_SPEED_BOOST)) {
         lv.removeModifier(ATTACKING_SPEED_BOOST);
      }

      this.tickAngerLogic((ServerWorld)this.getWorld(), true);
      if (this.getTarget() != null) {
         this.tickAngerPassing();
      }

      if (this.hasAngerTime()) {
         this.playerHitTimer = this.age;
      }

      super.mobTick();
   }

   private void tickAngrySound() {
      if (this.angrySoundDelay > 0) {
         --this.angrySoundDelay;
         if (this.angrySoundDelay == 0) {
            this.playAngrySound();
         }
      }

   }

   private void tickAngerPassing() {
      if (this.angerPassingCooldown > 0) {
         --this.angerPassingCooldown;
      } else {
         if (this.getVisibilityCache().canSee(this.getTarget())) {
            this.angerNearbyZombifiedPiglins();
         }

         this.angerPassingCooldown = ANGER_PASSING_COOLDOWN_RANGE.get(this.random);
      }
   }

   private void angerNearbyZombifiedPiglins() {
      double d = this.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
      Box lv = Box.from(this.getPos()).expand(d, 10.0, d);
      this.getWorld().getEntitiesByClass(ZombifiedPiglinEntity.class, lv, EntityPredicates.EXCEPT_SPECTATOR).stream().filter((zombifiedPiglin) -> {
         return zombifiedPiglin != this;
      }).filter((zombifiedPiglin) -> {
         return zombifiedPiglin.getTarget() == null;
      }).filter((zombifiedPiglin) -> {
         return !zombifiedPiglin.isTeammate(this.getTarget());
      }).forEach((zombifiedPiglin) -> {
         zombifiedPiglin.setTarget(this.getTarget());
      });
   }

   private void playAngrySound() {
      this.playSound(SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, this.getSoundVolume() * 2.0F, this.getSoundPitch() * 1.8F);
   }

   public void setTarget(@Nullable LivingEntity target) {
      if (this.getTarget() == null && target != null) {
         this.angrySoundDelay = ANGRY_SOUND_DELAY_RANGE.get(this.random);
         this.angerPassingCooldown = ANGER_PASSING_COOLDOWN_RANGE.get(this.random);
      }

      if (target instanceof PlayerEntity) {
         this.setAttacking((PlayerEntity)target);
      }

      super.setTarget(target);
   }

   public void chooseRandomAngerTime() {
      this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getDifficulty() != Difficulty.PEACEFUL && !world.getBlockState(pos.down()).isOf(Blocks.NETHER_WART_BLOCK);
   }

   public boolean canSpawn(WorldView world) {
      return world.doesNotIntersectEntities(this) && !world.containsFluid(this.getBoundingBox());
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      this.writeAngerToNbt(nbt);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.readAngerFromNbt(this.getWorld(), nbt);
   }

   public void setAngerTime(int angerTime) {
      this.angerTime = angerTime;
   }

   public int getAngerTime() {
      return this.angerTime;
   }

   protected SoundEvent getAmbientSound() {
      return this.hasAngerTime() ? SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_ANGRY : SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_DEATH;
   }

   protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
      this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
   }

   protected ItemStack getSkull() {
      return ItemStack.EMPTY;
   }

   protected void initAttributes() {
      this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).setBaseValue(0.0);
   }

   @Nullable
   public UUID getAngryAt() {
      return this.angryAt;
   }

   public boolean isAngryAt(PlayerEntity player) {
      return this.shouldAngerAt(player);
   }

   public boolean canGather(ItemStack stack) {
      return this.canPickupItem(stack);
   }

   static {
      ATTACKING_SPEED_BOOST = new EntityAttributeModifier(ATTACKING_SPEED_BOOST_ID, "Attacking speed boost", 0.05, EntityAttributeModifier.Operation.ADDITION);
      ANGRY_SOUND_DELAY_RANGE = TimeHelper.betweenSeconds(0, 1);
      ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
      ANGER_PASSING_COOLDOWN_RANGE = TimeHelper.betweenSeconds(4, 6);
   }
}
