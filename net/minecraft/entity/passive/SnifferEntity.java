package net.minecraft.entity.passive;

import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeType;
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
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class SnifferEntity extends AnimalEntity {
   private static final int field_42656 = 1700;
   private static final int field_42657 = 6000;
   private static final int field_42658 = 30;
   private static final int field_42659 = 120;
   private static final int field_42661 = 48000;
   private static final float field_44785 = 0.4F;
   private static final EntityDimensions field_44786;
   private static final TrackedData STATE;
   private static final TrackedData FINISH_DIG_TIME;
   public final AnimationState feelingHappyAnimationState = new AnimationState();
   public final AnimationState scentingAnimationState = new AnimationState();
   public final AnimationState sniffingAnimationState = new AnimationState();
   public final AnimationState diggingAnimationState = new AnimationState();
   public final AnimationState risingAnimationState = new AnimationState();
   public final AnimationState babyGrowthAnimationState = new AnimationState();

   public static DefaultAttributeContainer.Builder createSnifferAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.10000000149011612).add(EntityAttributes.GENERIC_MAX_HEALTH, 14.0);
   }

   public SnifferEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.dataTracker.startTracking(STATE, SnifferEntity.State.IDLING);
      this.dataTracker.startTracking(FINISH_DIG_TIME, 0);
      this.getNavigation().setCanSwim(true);
      this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
      this.setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW, -1.0F);
      this.setPathfindingPenalty(PathNodeType.DAMAGE_CAUTIOUS, -1.0F);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return this.getDimensions(pose).height * 0.6F;
   }

   public void onStartPathfinding() {
      super.onStartPathfinding();
      if (this.isOnFire() || this.isTouchingWater()) {
         this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
      }

   }

   public void onFinishPathfinding() {
      this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return this.dataTracker.method_51696(STATE) && this.getState() == SnifferEntity.State.DIGGING ? field_44786.scaled(this.getScaleFactor()) : super.getDimensions(pose);
   }

   public boolean isPanicking() {
      return this.brain.getOptionalRegisteredMemory(MemoryModuleType.IS_PANICKING).isPresent();
   }

   public boolean isSearching() {
      return this.getState() == SnifferEntity.State.SEARCHING;
   }

   public boolean isTempted() {
      return (Boolean)this.brain.getOptionalRegisteredMemory(MemoryModuleType.IS_TEMPTED).orElse(false);
   }

   public boolean canTryToDig() {
      return !this.isTempted() && !this.isPanicking() && !this.isTouchingWater() && !this.isInLove() && this.isOnGround() && !this.hasVehicle();
   }

   public boolean isDiggingOrSearching() {
      return this.getState() == SnifferEntity.State.DIGGING || this.getState() == SnifferEntity.State.SEARCHING;
   }

   private BlockPos getDigPos() {
      Vec3d lv = this.getDigLocation();
      return BlockPos.ofFloored(lv.getX(), this.getY() + 0.20000000298023224, lv.getZ());
   }

   private Vec3d getDigLocation() {
      return this.getPos().add(this.getRotationVecClient().multiply(2.25));
   }

   private State getState() {
      return (State)this.dataTracker.get(STATE);
   }

   private SnifferEntity setState(State state) {
      this.dataTracker.set(STATE, state);
      return this;
   }

   public void onTrackedDataSet(TrackedData data) {
      if (STATE.equals(data)) {
         State lv = this.getState();
         this.stopAnimations();
         switch (lv) {
            case SCENTING:
               this.scentingAnimationState.startIfNotRunning(this.age);
               break;
            case SNIFFING:
               this.sniffingAnimationState.startIfNotRunning(this.age);
               break;
            case DIGGING:
               this.diggingAnimationState.startIfNotRunning(this.age);
               break;
            case RISING:
               this.risingAnimationState.startIfNotRunning(this.age);
               break;
            case FEELING_HAPPY:
               this.feelingHappyAnimationState.startIfNotRunning(this.age);
         }

         this.calculateDimensions();
      }

      super.onTrackedDataSet(data);
   }

   private void stopAnimations() {
      this.diggingAnimationState.stop();
      this.sniffingAnimationState.stop();
      this.risingAnimationState.stop();
      this.feelingHappyAnimationState.stop();
      this.scentingAnimationState.stop();
   }

   public SnifferEntity startState(State state) {
      switch (state) {
         case SCENTING:
            this.setState(SnifferEntity.State.SCENTING).playScentingSound();
            break;
         case SNIFFING:
            this.playSound(SoundEvents.ENTITY_SNIFFER_SNIFFING, 1.0F, 1.0F);
            this.setState(SnifferEntity.State.SNIFFING);
            break;
         case DIGGING:
            this.setState(SnifferEntity.State.DIGGING).setDigging();
            break;
         case RISING:
            this.playSound(SoundEvents.ENTITY_SNIFFER_DIGGING_STOP, 1.0F, 1.0F);
            this.setState(SnifferEntity.State.RISING);
            break;
         case FEELING_HAPPY:
            this.playSound(SoundEvents.ENTITY_SNIFFER_HAPPY, 1.0F, 1.0F);
            this.setState(SnifferEntity.State.FEELING_HAPPY);
            break;
         case IDLING:
            this.setState(SnifferEntity.State.IDLING);
            break;
         case SEARCHING:
            this.setState(SnifferEntity.State.SEARCHING);
      }

      return this;
   }

   private SnifferEntity playScentingSound() {
      this.playSound(SoundEvents.ENTITY_SNIFFER_SCENTING, 1.0F, this.isBaby() ? 1.3F : 1.0F);
      return this;
   }

   private SnifferEntity setDigging() {
      this.dataTracker.set(FINISH_DIG_TIME, this.age + 120);
      this.getWorld().sendEntityStatus(this, EntityStatuses.START_DIGGING);
      return this;
   }

   public SnifferEntity finishDigging(boolean explored) {
      if (explored) {
         this.addExploredPosition(this.getSteppingPos());
      }

      return this;
   }

   Optional findSniffingTargetPos() {
      return IntStream.range(0, 5).mapToObj((i) -> {
         return FuzzyTargeting.find(this, 10 + 2 * i, 3);
      }).filter(Objects::nonNull).map(BlockPos::ofFloored).filter((pos) -> {
         return this.getWorld().getWorldBorder().contains(pos);
      }).map(BlockPos::down).filter(this::isDiggable).findFirst();
   }

   boolean canDig() {
      return !this.isPanicking() && !this.isTempted() && !this.isBaby() && !this.isTouchingWater() && this.isOnGround() && !this.hasVehicle() && this.isDiggable(this.getDigPos().down());
   }

   private boolean isDiggable(BlockPos pos) {
      return this.getWorld().getBlockState(pos).isIn(BlockTags.SNIFFER_DIGGABLE_BLOCK) && this.getExploredPositions().noneMatch((arg2) -> {
         return GlobalPos.create(this.getWorld().getRegistryKey(), pos).equals(arg2);
      }) && (Boolean)Optional.ofNullable(this.getNavigation().findPathTo((BlockPos)pos, 1)).map(Path::reachesTarget).orElse(false);
   }

   private void dropSeeds() {
      if (!this.getWorld().isClient() && (Integer)this.dataTracker.get(FINISH_DIG_TIME) == this.age) {
         ServerWorld lv = (ServerWorld)this.getWorld();
         LootTable lv2 = lv.getServer().getLootManager().getLootTable(LootTables.SNIFFER_DIGGING_GAMEPLAY);
         LootContext.Builder lv3 = (new LootContext.Builder(lv)).parameter(LootContextParameters.ORIGIN, this.getDigLocation()).parameter(LootContextParameters.THIS_ENTITY, this).random(this.random);
         List list = lv2.generateLoot(lv3.build(LootContextTypes.GIFT));
         BlockPos lv4 = this.getDigPos();
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            ItemStack lv5 = (ItemStack)var6.next();
            ItemEntity lv6 = new ItemEntity(lv, (double)lv4.getX(), (double)lv4.getY(), (double)lv4.getZ(), lv5);
            lv6.setToDefaultPickupDelay();
            lv.spawnEntity(lv6);
         }

         this.playSound(SoundEvents.ENTITY_SNIFFER_DROP_SEED, 1.0F, 1.0F);
      }
   }

   private SnifferEntity spawnDiggingParticles(AnimationState diggingAnimationState) {
      boolean bl = diggingAnimationState.getTimeRunning() > 1700L && diggingAnimationState.getTimeRunning() < 6000L;
      if (bl) {
         BlockPos lv = this.getDigPos();
         BlockState lv2 = this.getWorld().getBlockState(lv.down());
         if (lv2.getRenderType() != BlockRenderType.INVISIBLE) {
            for(int i = 0; i < 30; ++i) {
               Vec3d lv3 = Vec3d.ofCenter(lv).add(0.0, -0.6499999761581421, 0.0);
               this.getWorld().addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, lv2), lv3.x, lv3.y, lv3.z, 0.0, 0.0, 0.0);
            }

            if (this.age % 10 == 0) {
               this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), lv2.getSoundGroup().getHitSound(), this.getSoundCategory(), 0.5F, 0.5F, false);
            }
         }
      }

      if (this.age % 10 == 0) {
         this.getWorld().emitGameEvent(GameEvent.ENTITY_SHAKE, this.getDigPos(), GameEvent.Emitter.of((Entity)this));
      }

      return this;
   }

   private SnifferEntity addExploredPosition(BlockPos pos) {
      List list = (List)this.getExploredPositions().limit(20L).collect(Collectors.toList());
      list.add(0, GlobalPos.create(this.getWorld().getRegistryKey(), pos));
      this.getBrain().remember(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS, (Object)list);
      return this;
   }

   private Stream getExploredPositions() {
      return this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS).stream().flatMap(Collection::stream);
   }

   protected void jump() {
      super.jump();
      double d = this.moveControl.getSpeed();
      if (d > 0.0) {
         double e = this.getVelocity().horizontalLengthSquared();
         if (e < 0.01) {
            this.updateVelocity(0.1F, new Vec3d(0.0, 0.0, 1.0));
         }
      }

   }

   public void breed(ServerWorld world, AnimalEntity other) {
      ItemStack lv = new ItemStack(Items.SNIFFER_EGG);
      ItemEntity lv2 = new ItemEntity(world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), lv);
      lv2.setToDefaultPickupDelay();
      this.breed(world, other, (PassiveEntity)null);
      this.playSound(SoundEvents.BLOCK_SNIFFER_EGG_PLOP, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.5F);
      world.spawnEntity(lv2);
   }

   public void onDeath(DamageSource damageSource) {
      this.startState(SnifferEntity.State.IDLING);
      super.onDeath(damageSource);
   }

   public void tick() {
      switch (this.getState()) {
         case DIGGING:
            this.spawnDiggingParticles(this.diggingAnimationState).dropSeeds();
            break;
         case SEARCHING:
            this.playSearchingSound();
      }

      this.babyGrowthAnimationState.setRunning(this.isBaby(), this.age);
      super.tick();
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      boolean bl = this.isBreedingItem(lv);
      ActionResult lv2 = super.interactMob(player, hand);
      if (lv2.isAccepted() && bl) {
         this.getWorld().playSoundFromEntity((PlayerEntity)null, this, this.getEatSound(lv), SoundCategory.NEUTRAL, 1.0F, MathHelper.nextBetween(this.getWorld().random, 0.8F, 1.2F));
      }

      return lv2;
   }

   public double getMountedHeightOffset() {
      return 1.8;
   }

   public float getNameLabelHeight() {
      return super.getNameLabelHeight() + 0.3F;
   }

   private void playSearchingSound() {
      if (this.getWorld().isClient() && this.age % 20 == 0) {
         this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_SNIFFER_SEARCHING, this.getSoundCategory(), 1.0F, 1.0F, false);
      }

   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_SNIFFER_STEP, 0.15F, 1.0F);
   }

   public SoundEvent getEatSound(ItemStack stack) {
      return SoundEvents.ENTITY_SNIFFER_EAT;
   }

   protected SoundEvent getAmbientSound() {
      return Set.of(SnifferEntity.State.DIGGING, SnifferEntity.State.SEARCHING).contains(this.getState()) ? null : SoundEvents.ENTITY_SNIFFER_IDLE;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_SNIFFER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SNIFFER_DEATH;
   }

   public int getMaxHeadRotation() {
      return 50;
   }

   public void setBaby(boolean baby) {
      this.setBreedingAge(baby ? -48000 : 0);
   }

   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
      return (PassiveEntity)EntityType.SNIFFER.create(world);
   }

   public boolean canBreedWith(AnimalEntity other) {
      if (!(other instanceof SnifferEntity lv)) {
         return false;
      } else {
         Set set = Set.of(SnifferEntity.State.IDLING, SnifferEntity.State.SCENTING, SnifferEntity.State.FEELING_HAPPY);
         return set.contains(this.getState()) && set.contains(lv.getState()) && super.canBreedWith(other);
      }
   }

   public Box getVisibilityBoundingBox() {
      return super.getVisibilityBoundingBox().expand(0.6000000238418579);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return stack.isIn(ItemTags.SNIFFER_FOOD);
   }

   protected Brain deserializeBrain(Dynamic dynamic) {
      return SnifferBrain.create(this.createBrainProfile().deserialize(dynamic));
   }

   public Brain getBrain() {
      return super.getBrain();
   }

   protected Brain.Profile createBrainProfile() {
      return Brain.createProfile(SnifferBrain.MEMORY_MODULES, SnifferBrain.SENSORS);
   }

   protected void mobTick() {
      this.getWorld().getProfiler().push("snifferBrain");
      this.getBrain().tick((ServerWorld)this.getWorld(), this);
      this.getWorld().getProfiler().swap("snifferActivityUpdate");
      SnifferBrain.updateActivities(this);
      this.getWorld().getProfiler().pop();
      super.mobTick();
   }

   protected void sendAiDebugData() {
      super.sendAiDebugData();
      DebugInfoSender.sendBrainDebugData(this);
   }

   static {
      field_44786 = EntityDimensions.changing(EntityType.SNIFFER.getWidth(), EntityType.SNIFFER.getHeight() - 0.4F);
      STATE = DataTracker.registerData(SnifferEntity.class, TrackedDataHandlerRegistry.SNIFFER_STATE);
      FINISH_DIG_TIME = DataTracker.registerData(SnifferEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }

   public static enum State {
      IDLING,
      FEELING_HAPPY,
      SCENTING,
      SNIFFING,
      SEARCHING,
      DIGGING,
      RISING;

      // $FF: synthetic method
      private static State[] method_49151() {
         return new State[]{IDLING, FEELING_HAPPY, SCENTING, SNIFFING, SEARCHING, DIGGING, RISING};
      }
   }
}
