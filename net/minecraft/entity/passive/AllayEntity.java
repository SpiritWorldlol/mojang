package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.GameEventTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.Vibrations;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AllayEntity extends PathAwareEntity implements InventoryOwner, Vibrations {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Vec3i ITEM_PICKUP_RANGE_EXPANDER = new Vec3i(1, 1, 1);
   private static final int field_39461 = 5;
   private static final float field_39462 = 55.0F;
   private static final float field_39463 = 15.0F;
   private static final Ingredient DUPLICATION_INGREDIENT;
   private static final int DUPLICATION_COOLDOWN = 6000;
   private static final int field_39679 = 3;
   private static final double field_40129 = 0.4;
   private static final TrackedData DANCING;
   private static final TrackedData CAN_DUPLICATE;
   protected static final ImmutableList SENSORS;
   protected static final ImmutableList MEMORY_MODULES;
   public static final ImmutableList THROW_SOUND_PITCHES;
   private final EntityGameEventHandler gameEventHandler;
   private Vibrations.ListenerData vibrationListenerData;
   private final Vibrations.Callback vibrationCallback;
   private final EntityGameEventHandler jukeboxEventHandler;
   private final SimpleInventory inventory = new SimpleInventory(1);
   private @Nullable BlockPos jukeboxPos;
   private long duplicationCooldown;
   private float field_38935;
   private float field_38936;
   private float field_39472;
   private float field_39473;
   private float field_39474;

   public AllayEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.moveControl = new FlightMoveControl(this, 20, true);
      this.setCanPickUpLoot(this.canPickUpLoot());
      this.vibrationCallback = new VibrationCallback();
      this.vibrationListenerData = new Vibrations.ListenerData();
      this.gameEventHandler = new EntityGameEventHandler(new Vibrations.VibrationListener(this));
      this.jukeboxEventHandler = new EntityGameEventHandler(new JukeboxEventListener(this.vibrationCallback.getPositionSource(), GameEvent.JUKEBOX_PLAY.getRange()));
   }

   protected Brain.Profile createBrainProfile() {
      return Brain.createProfile(MEMORY_MODULES, SENSORS);
   }

   protected Brain deserializeBrain(Dynamic dynamic) {
      return AllayBrain.create(this.createBrainProfile().deserialize(dynamic));
   }

   public Brain getBrain() {
      return super.getBrain();
   }

   public static DefaultAttributeContainer.Builder createAllayAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0).add(EntityAttributes.GENERIC_FLYING_SPEED, 0.10000000149011612).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.10000000149011612).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
   }

   protected EntityNavigation createNavigation(World world) {
      BirdNavigation lv = new BirdNavigation(this, world);
      lv.setCanPathThroughDoors(false);
      lv.setCanSwim(true);
      lv.setCanEnterOpenDoors(true);
      return lv;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(DANCING, false);
      this.dataTracker.startTracking(CAN_DUPLICATE, true);
   }

   public void travel(Vec3d movementInput) {
      if (this.isLogicalSideForUpdatingMovement()) {
         if (this.isTouchingWater()) {
            this.updateVelocity(0.02F, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.800000011920929));
         } else if (this.isInLava()) {
            this.updateVelocity(0.02F, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.5));
         } else {
            this.updateVelocity(this.getMovementSpeed(), movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9100000262260437));
         }
      }

      this.updateLimbs(false);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height * 0.6F;
   }

   public boolean damage(DamageSource source, float amount) {
      Entity var4 = source.getAttacker();
      if (var4 instanceof PlayerEntity lv) {
         Optional optional = this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER);
         if (optional.isPresent() && lv.getUuid().equals(optional.get())) {
            return false;
         }
      }

      return super.damage(source, amount);
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
   }

   protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
   }

   protected SoundEvent getAmbientSound() {
      return this.hasStackEquipped(EquipmentSlot.MAINHAND) ? SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM : SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ALLAY_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ALLAY_DEATH;
   }

   protected float getSoundVolume() {
      return 0.4F;
   }

   protected void mobTick() {
      this.getWorld().getProfiler().push("allayBrain");
      this.getBrain().tick((ServerWorld)this.getWorld(), this);
      this.getWorld().getProfiler().pop();
      this.getWorld().getProfiler().push("allayActivityUpdate");
      AllayBrain.updateActivities(this);
      this.getWorld().getProfiler().pop();
      super.mobTick();
   }

   public void tickMovement() {
      super.tickMovement();
      if (!this.getWorld().isClient && this.isAlive() && this.age % 10 == 0) {
         this.heal(1.0F);
      }

      if (this.isDancing() && this.shouldStopDancing() && this.age % 20 == 0) {
         this.setDancing(false);
         this.jukeboxPos = null;
      }

      this.tickDuplicationCooldown();
   }

   public void tick() {
      super.tick();
      if (this.getWorld().isClient) {
         this.field_38936 = this.field_38935;
         if (this.isHoldingItem()) {
            this.field_38935 = MathHelper.clamp(this.field_38935 + 1.0F, 0.0F, 5.0F);
         } else {
            this.field_38935 = MathHelper.clamp(this.field_38935 - 1.0F, 0.0F, 5.0F);
         }

         if (this.isDancing()) {
            ++this.field_39472;
            this.field_39474 = this.field_39473;
            if (this.method_44360()) {
               ++this.field_39473;
            } else {
               --this.field_39473;
            }

            this.field_39473 = MathHelper.clamp(this.field_39473, 0.0F, 15.0F);
         } else {
            this.field_39472 = 0.0F;
            this.field_39473 = 0.0F;
            this.field_39474 = 0.0F;
         }
      } else {
         Vibrations.Ticker.tick(this.getWorld(), this.vibrationListenerData, this.vibrationCallback);
         if (this.isPanicking()) {
            this.setDancing(false);
         }
      }

   }

   public boolean canPickUpLoot() {
      return !this.isItemPickupCoolingDown() && this.isHoldingItem();
   }

   public boolean isHoldingItem() {
      return !this.getStackInHand(Hand.MAIN_HAND).isEmpty();
   }

   public boolean canEquip(ItemStack stack) {
      return false;
   }

   private boolean isItemPickupCoolingDown() {
      return this.getBrain().isMemoryInState(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleState.VALUE_PRESENT);
   }

   protected ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      ItemStack lv2 = this.getStackInHand(Hand.MAIN_HAND);
      if (this.isDancing() && this.matchesDuplicationIngredient(lv) && this.canDuplicate()) {
         this.duplicate();
         this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_BREEDING_PARTICLES);
         this.getWorld().playSoundFromEntity(player, this, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 2.0F, 1.0F);
         this.decrementStackUnlessInCreative(player, lv);
         return ActionResult.SUCCESS;
      } else if (lv2.isEmpty() && !lv.isEmpty()) {
         ItemStack lv3 = lv.copyWithCount(1);
         this.setStackInHand(Hand.MAIN_HAND, lv3);
         this.decrementStackUnlessInCreative(player, lv);
         this.getWorld().playSoundFromEntity(player, this, SoundEvents.ENTITY_ALLAY_ITEM_GIVEN, SoundCategory.NEUTRAL, 2.0F, 1.0F);
         this.getBrain().remember(MemoryModuleType.LIKED_PLAYER, (Object)player.getUuid());
         return ActionResult.SUCCESS;
      } else if (!lv2.isEmpty() && hand == Hand.MAIN_HAND && lv.isEmpty()) {
         this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
         this.getWorld().playSoundFromEntity(player, this, SoundEvents.ENTITY_ALLAY_ITEM_TAKEN, SoundCategory.NEUTRAL, 2.0F, 1.0F);
         this.swingHand(Hand.MAIN_HAND);
         Iterator var5 = this.getInventory().clearToList().iterator();

         while(var5.hasNext()) {
            ItemStack lv4 = (ItemStack)var5.next();
            LookTargetUtil.give(this, lv4, this.getPos());
         }

         this.getBrain().forget(MemoryModuleType.LIKED_PLAYER);
         player.giveItemStack(lv2);
         return ActionResult.SUCCESS;
      } else {
         return super.interactMob(player, hand);
      }
   }

   public void updateJukeboxPos(BlockPos jukeboxPos, boolean playing) {
      if (playing) {
         if (!this.isDancing()) {
            this.jukeboxPos = jukeboxPos;
            this.setDancing(true);
         }
      } else if (jukeboxPos.equals(this.jukeboxPos) || this.jukeboxPos == null) {
         this.jukeboxPos = null;
         this.setDancing(false);
      }

   }

   public SimpleInventory getInventory() {
      return this.inventory;
   }

   protected Vec3i getItemPickUpRangeExpander() {
      return ITEM_PICKUP_RANGE_EXPANDER;
   }

   public boolean canGather(ItemStack stack) {
      ItemStack lv = this.getStackInHand(Hand.MAIN_HAND);
      return !lv.isEmpty() && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && this.inventory.canInsert(stack) && this.areItemsEqual(lv, stack);
   }

   private boolean areItemsEqual(ItemStack stack, ItemStack stack2) {
      return stack.isItemEqual(stack2) && !this.areDifferentPotions(stack, stack2);
   }

   private boolean areDifferentPotions(ItemStack stack, ItemStack stack2) {
      NbtCompound lv = stack.getNbt();
      boolean bl = lv != null && lv.contains("Potion");
      if (!bl) {
         return false;
      } else {
         NbtCompound lv2 = stack2.getNbt();
         boolean bl2 = lv2 != null && lv2.contains("Potion");
         if (!bl2) {
            return true;
         } else {
            NbtElement lv3 = lv.get("Potion");
            NbtElement lv4 = lv2.get("Potion");
            return lv3 != null && lv4 != null && !lv3.equals(lv4);
         }
      }
   }

   protected void loot(ItemEntity item) {
      InventoryOwner.pickUpItem(this, this, item);
   }

   protected void sendAiDebugData() {
      super.sendAiDebugData();
      DebugInfoSender.sendBrainDebugData(this);
   }

   public boolean isFlappingWings() {
      return !this.isOnGround();
   }

   public void updateEventHandler(BiConsumer callback) {
      World var3 = this.getWorld();
      if (var3 instanceof ServerWorld lv) {
         callback.accept(this.gameEventHandler, lv);
         callback.accept(this.jukeboxEventHandler, lv);
      }

   }

   public boolean isDancing() {
      return (Boolean)this.dataTracker.get(DANCING);
   }

   public boolean isPanicking() {
      return this.brain.getOptionalRegisteredMemory(MemoryModuleType.IS_PANICKING).isPresent();
   }

   public void setDancing(boolean dancing) {
      if (!this.getWorld().isClient && this.canMoveVoluntarily() && (!dancing || !this.isPanicking())) {
         this.dataTracker.set(DANCING, dancing);
      }
   }

   private boolean shouldStopDancing() {
      return this.jukeboxPos == null || !this.jukeboxPos.isWithinDistance(this.getPos(), (double)GameEvent.JUKEBOX_PLAY.getRange()) || !this.getWorld().getBlockState(this.jukeboxPos).isOf(Blocks.JUKEBOX);
   }

   public float method_43397(float f) {
      return MathHelper.lerp(f, this.field_38936, this.field_38935) / 5.0F;
   }

   public boolean method_44360() {
      float f = this.field_39472 % 55.0F;
      return f < 15.0F;
   }

   public float method_44368(float f) {
      return MathHelper.lerp(f, this.field_39474, this.field_39473) / 15.0F;
   }

   public boolean areItemsDifferent(ItemStack stack, ItemStack stack2) {
      return !this.areItemsEqual(stack, stack2);
   }

   protected void dropInventory() {
      super.dropInventory();
      this.inventory.clearToList().forEach(this::dropStack);
      ItemStack lv = this.getEquippedStack(EquipmentSlot.MAINHAND);
      if (!lv.isEmpty() && !EnchantmentHelper.hasVanishingCurse(lv)) {
         this.dropStack(lv);
         this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      }

   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return false;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      this.writeInventory(nbt);
      DataResult var10000 = Vibrations.ListenerData.CODEC.encodeStart(NbtOps.INSTANCE, this.vibrationListenerData);
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((arg2) -> {
         nbt.put("listener", arg2);
      });
      nbt.putLong("DuplicationCooldown", this.duplicationCooldown);
      nbt.putBoolean("CanDuplicate", this.canDuplicate());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.readInventory(nbt);
      if (nbt.contains("listener", NbtElement.COMPOUND_TYPE)) {
         DataResult var10000 = Vibrations.ListenerData.CODEC.parse(new Dynamic(NbtOps.INSTANCE, nbt.getCompound("listener")));
         Logger var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         var10000.resultOrPartial(var10001::error).ifPresent((arg) -> {
            this.vibrationListenerData = arg;
         });
      }

      this.duplicationCooldown = (long)nbt.getInt("DuplicationCooldown");
      this.dataTracker.set(CAN_DUPLICATE, nbt.getBoolean("CanDuplicate"));
   }

   protected boolean shouldFollowLeash() {
      return false;
   }

   private void tickDuplicationCooldown() {
      if (this.duplicationCooldown > 0L) {
         --this.duplicationCooldown;
      }

      if (!this.getWorld().isClient() && this.duplicationCooldown == 0L && !this.canDuplicate()) {
         this.dataTracker.set(CAN_DUPLICATE, true);
      }

   }

   private boolean matchesDuplicationIngredient(ItemStack stack) {
      return DUPLICATION_INGREDIENT.test(stack);
   }

   private void duplicate() {
      AllayEntity lv = (AllayEntity)EntityType.ALLAY.create(this.getWorld());
      if (lv != null) {
         lv.refreshPositionAfterTeleport(this.getPos());
         lv.setPersistent();
         lv.startDuplicationCooldown();
         this.startDuplicationCooldown();
         this.getWorld().spawnEntity(lv);
      }

   }

   private void startDuplicationCooldown() {
      this.duplicationCooldown = 6000L;
      this.dataTracker.set(CAN_DUPLICATE, false);
   }

   private boolean canDuplicate() {
      return (Boolean)this.dataTracker.get(CAN_DUPLICATE);
   }

   private void decrementStackUnlessInCreative(PlayerEntity player, ItemStack stack) {
      if (!player.getAbilities().creativeMode) {
         stack.decrement(1);
      }

   }

   public Vec3d getLeashOffset() {
      return new Vec3d(0.0, (double)this.getStandingEyeHeight() * 0.6, (double)this.getWidth() * 0.1);
   }

   public double getHeightOffset() {
      return 0.4;
   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.ADD_BREEDING_PARTICLES) {
         for(int i = 0; i < 3; ++i) {
            this.addHeartParticle();
         }
      } else {
         super.handleStatus(status);
      }

   }

   private void addHeartParticle() {
      double d = this.random.nextGaussian() * 0.02;
      double e = this.random.nextGaussian() * 0.02;
      double f = this.random.nextGaussian() * 0.02;
      this.getWorld().addParticle(ParticleTypes.HEART, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
   }

   public Vibrations.ListenerData getVibrationListenerData() {
      return this.vibrationListenerData;
   }

   public Vibrations.Callback getVibrationCallback() {
      return this.vibrationCallback;
   }

   static {
      DUPLICATION_INGREDIENT = Ingredient.ofItems(Items.AMETHYST_SHARD);
      DANCING = DataTracker.registerData(AllayEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      CAN_DUPLICATE = DataTracker.registerData(AllayEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NEAREST_ITEMS);
      MEMORY_MODULES = ImmutableList.of(MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.LIKED_PLAYER, MemoryModuleType.LIKED_NOTEBLOCK, MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.IS_PANICKING, new MemoryModuleType[0]);
      THROW_SOUND_PITCHES = ImmutableList.of(0.5625F, 0.625F, 0.75F, 0.9375F, 1.0F, 1.0F, 1.125F, 1.25F, 1.5F, 1.875F, 2.0F, 2.25F, new Float[]{2.5F, 3.0F, 3.75F, 4.0F});
   }

   class VibrationCallback implements Vibrations.Callback {
      private static final int RANGE = 16;
      private final PositionSource positionSource = new EntityPositionSource(AllayEntity.this, AllayEntity.this.getStandingEyeHeight());

      public int getRange() {
         return 16;
      }

      public PositionSource getPositionSource() {
         return this.positionSource;
      }

      public boolean accepts(ServerWorld world, BlockPos pos, GameEvent event, GameEvent.Emitter emitter) {
         if (AllayEntity.this.isAiDisabled()) {
            return false;
         } else {
            Optional optional = AllayEntity.this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK);
            if (optional.isEmpty()) {
               return true;
            } else {
               GlobalPos lv = (GlobalPos)optional.get();
               return lv.getDimension().equals(world.getRegistryKey()) && lv.getPos().equals(pos);
            }
         }
      }

      public void accept(ServerWorld world, BlockPos pos, GameEvent event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance) {
         if (event == GameEvent.NOTE_BLOCK_PLAY) {
            AllayBrain.rememberNoteBlock(AllayEntity.this, new BlockPos(pos));
         }

      }

      public TagKey getTag() {
         return GameEventTags.ALLAY_CAN_LISTEN;
      }
   }

   private class JukeboxEventListener implements GameEventListener {
      private final PositionSource positionSource;
      private final int range;

      public JukeboxEventListener(PositionSource positionSource, int range) {
         this.positionSource = positionSource;
         this.range = range;
      }

      public PositionSource getPositionSource() {
         return this.positionSource;
      }

      public int getRange() {
         return this.range;
      }

      public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
         if (event == GameEvent.JUKEBOX_PLAY) {
            AllayEntity.this.updateJukeboxPos(BlockPos.ofFloored(emitterPos), true);
            return true;
         } else if (event == GameEvent.JUKEBOX_STOP_PLAY) {
            AllayEntity.this.updateJukeboxPos(BlockPos.ofFloored(emitterPos), false);
            return true;
         } else {
            return false;
         }
      }
   }
}
