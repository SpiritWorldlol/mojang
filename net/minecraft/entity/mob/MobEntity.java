package net.minecraft.entity.mob;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class MobEntity extends LivingEntity implements Targeter {
   private static final TrackedData MOB_FLAGS;
   private static final int AI_DISABLED_FLAG = 1;
   private static final int LEFT_HANDED_FLAG = 2;
   private static final int ATTACKING_FLAG = 4;
   protected static final int MINIMUM_DROPPED_XP_PER_EQUIPMENT = 1;
   private static final Vec3i ITEM_PICK_UP_RANGE_EXPANDER;
   public static final float BASE_SPAWN_EQUIPMENT_CHANCE = 0.15F;
   public static final float DEFAULT_CAN_PICKUP_LOOT_CHANCE = 0.55F;
   public static final float BASE_ENCHANTED_ARMOR_CHANCE = 0.5F;
   public static final float BASE_ENCHANTED_MAIN_HAND_EQUIPMENT_CHANCE = 0.25F;
   public static final String LEASH_KEY = "Leash";
   public static final float DEFAULT_DROP_CHANCE = 0.085F;
   public static final int field_38932 = 2;
   public static final int field_35039 = 2;
   public int ambientSoundChance;
   protected int experiencePoints;
   protected LookControl lookControl;
   protected MoveControl moveControl;
   protected JumpControl jumpControl;
   private final BodyControl bodyControl;
   protected EntityNavigation navigation;
   protected final GoalSelector goalSelector;
   protected final GoalSelector targetSelector;
   @Nullable
   private LivingEntity target;
   private final MobVisibilityCache visibilityCache;
   private final DefaultedList handItems;
   protected final float[] handDropChances;
   private final DefaultedList armorItems;
   protected final float[] armorDropChances;
   private boolean canPickUpLoot;
   private boolean persistent;
   private final Map pathfindingPenalties;
   @Nullable
   private Identifier lootTable;
   private long lootTableSeed;
   @Nullable
   private Entity holdingEntity;
   private int holdingEntityId;
   @Nullable
   private NbtCompound leashNbt;
   private BlockPos positionTarget;
   private float positionTargetRange;

   protected MobEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.handItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
      this.handDropChances = new float[2];
      this.armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
      this.armorDropChances = new float[4];
      this.pathfindingPenalties = Maps.newEnumMap(PathNodeType.class);
      this.positionTarget = BlockPos.ORIGIN;
      this.positionTargetRange = -1.0F;
      this.goalSelector = new GoalSelector(arg2.getProfilerSupplier());
      this.targetSelector = new GoalSelector(arg2.getProfilerSupplier());
      this.lookControl = new LookControl(this);
      this.moveControl = new MoveControl(this);
      this.jumpControl = new JumpControl(this);
      this.bodyControl = this.createBodyControl();
      this.navigation = this.createNavigation(arg2);
      this.visibilityCache = new MobVisibilityCache(this);
      Arrays.fill(this.armorDropChances, 0.085F);
      Arrays.fill(this.handDropChances, 0.085F);
      if (arg2 != null && !arg2.isClient) {
         this.initGoals();
      }

   }

   protected void initGoals() {
   }

   public static DefaultAttributeContainer.Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
   }

   protected EntityNavigation createNavigation(World world) {
      return new MobNavigation(this, world);
   }

   protected boolean movesIndependently() {
      return false;
   }

   public float getPathfindingPenalty(PathNodeType nodeType) {
      MobEntity lv2;
      label17: {
         Entity var4 = this.getControllingVehicle();
         if (var4 instanceof MobEntity lv) {
            if (lv.movesIndependently()) {
               lv2 = lv;
               break label17;
            }
         }

         lv2 = this;
      }

      Float float_ = (Float)lv2.pathfindingPenalties.get(nodeType);
      return float_ == null ? nodeType.getDefaultPenalty() : float_;
   }

   public void setPathfindingPenalty(PathNodeType nodeType, float penalty) {
      this.pathfindingPenalties.put(nodeType, penalty);
   }

   public void onStartPathfinding() {
   }

   public void onFinishPathfinding() {
   }

   protected BodyControl createBodyControl() {
      return new BodyControl(this);
   }

   public LookControl getLookControl() {
      return this.lookControl;
   }

   public MoveControl getMoveControl() {
      Entity var2 = this.getControllingVehicle();
      if (var2 instanceof MobEntity lv) {
         return lv.getMoveControl();
      } else {
         return this.moveControl;
      }
   }

   public JumpControl getJumpControl() {
      return this.jumpControl;
   }

   public EntityNavigation getNavigation() {
      Entity var2 = this.getControllingVehicle();
      if (var2 instanceof MobEntity lv) {
         return lv.getNavigation();
      } else {
         return this.navigation;
      }
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      MobEntity var10000;
      if (!this.isAiDisabled()) {
         Entity var2 = this.getFirstPassenger();
         if (var2 instanceof MobEntity) {
            MobEntity lv = (MobEntity)var2;
            var10000 = lv;
            return var10000;
         }
      }

      var10000 = null;
      return var10000;
   }

   public MobVisibilityCache getVisibilityCache() {
      return this.visibilityCache;
   }

   @Nullable
   public LivingEntity getTarget() {
      return this.target;
   }

   public void setTarget(@Nullable LivingEntity target) {
      this.target = target;
   }

   public boolean canTarget(EntityType type) {
      return type != EntityType.GHAST;
   }

   public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
      return false;
   }

   public void onEatingGrass() {
      this.emitGameEvent(GameEvent.EAT);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(MOB_FLAGS, (byte)0);
   }

   public int getMinAmbientSoundDelay() {
      return 80;
   }

   public void playAmbientSound() {
      SoundEvent lv = this.getAmbientSound();
      if (lv != null) {
         this.playSound(lv, this.getSoundVolume(), this.getSoundPitch());
      }

   }

   public void baseTick() {
      super.baseTick();
      this.getWorld().getProfiler().push("mobBaseTick");
      if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundChance++) {
         this.resetSoundDelay();
         this.playAmbientSound();
      }

      this.getWorld().getProfiler().pop();
   }

   protected void playHurtSound(DamageSource source) {
      this.resetSoundDelay();
      super.playHurtSound(source);
   }

   private void resetSoundDelay() {
      this.ambientSoundChance = -this.getMinAmbientSoundDelay();
   }

   public int getXpToDrop() {
      if (this.experiencePoints > 0) {
         int i = this.experiencePoints;

         int j;
         for(j = 0; j < this.armorItems.size(); ++j) {
            if (!((ItemStack)this.armorItems.get(j)).isEmpty() && this.armorDropChances[j] <= 1.0F) {
               i += 1 + this.random.nextInt(3);
            }
         }

         for(j = 0; j < this.handItems.size(); ++j) {
            if (!((ItemStack)this.handItems.get(j)).isEmpty() && this.handDropChances[j] <= 1.0F) {
               i += 1 + this.random.nextInt(3);
            }
         }

         return i;
      } else {
         return this.experiencePoints;
      }
   }

   public void playSpawnEffects() {
      if (this.getWorld().isClient) {
         for(int i = 0; i < 20; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            double g = 10.0;
            this.getWorld().addParticle(ParticleTypes.POOF, this.offsetX(1.0) - d * 10.0, this.getRandomBodyY() - e * 10.0, this.getParticleZ(1.0) - f * 10.0, d, e, f);
         }
      } else {
         this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_SPAWN_EFFECTS);
      }

   }

   public void handleStatus(byte status) {
      if (status == EntityStatuses.PLAY_SPAWN_EFFECTS) {
         this.playSpawnEffects();
      } else {
         super.handleStatus(status);
      }

   }

   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
         this.updateLeash();
         if (this.age % 5 == 0) {
            this.updateGoalControls();
         }
      }

   }

   protected void updateGoalControls() {
      boolean bl = !(this.getControllingPassenger() instanceof MobEntity);
      boolean bl2 = !(this.getVehicle() instanceof BoatEntity);
      this.goalSelector.setControlEnabled(Goal.Control.MOVE, bl);
      this.goalSelector.setControlEnabled(Goal.Control.JUMP, bl && bl2);
      this.goalSelector.setControlEnabled(Goal.Control.LOOK, bl);
   }

   protected float turnHead(float bodyRotation, float headRotation) {
      this.bodyControl.tick();
      return headRotation;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("CanPickUpLoot", this.canPickUpLoot());
      nbt.putBoolean("PersistenceRequired", this.persistent);
      NbtList lv = new NbtList();

      NbtCompound lv3;
      for(Iterator var3 = this.armorItems.iterator(); var3.hasNext(); lv.add(lv3)) {
         ItemStack lv2 = (ItemStack)var3.next();
         lv3 = new NbtCompound();
         if (!lv2.isEmpty()) {
            lv2.writeNbt(lv3);
         }
      }

      nbt.put("ArmorItems", lv);
      NbtList lv4 = new NbtList();

      NbtCompound lv6;
      for(Iterator var11 = this.handItems.iterator(); var11.hasNext(); lv4.add(lv6)) {
         ItemStack lv5 = (ItemStack)var11.next();
         lv6 = new NbtCompound();
         if (!lv5.isEmpty()) {
            lv5.writeNbt(lv6);
         }
      }

      nbt.put("HandItems", lv4);
      NbtList lv7 = new NbtList();
      float[] var14 = this.armorDropChances;
      int var16 = var14.length;

      int var7;
      for(var7 = 0; var7 < var16; ++var7) {
         float f = var14[var7];
         lv7.add(NbtFloat.of(f));
      }

      nbt.put("ArmorDropChances", lv7);
      NbtList lv8 = new NbtList();
      float[] var17 = this.handDropChances;
      var7 = var17.length;

      for(int var19 = 0; var19 < var7; ++var19) {
         float g = var17[var19];
         lv8.add(NbtFloat.of(g));
      }

      nbt.put("HandDropChances", lv8);
      if (this.holdingEntity != null) {
         lv6 = new NbtCompound();
         if (this.holdingEntity instanceof LivingEntity) {
            UUID uUID = this.holdingEntity.getUuid();
            lv6.putUuid("UUID", uUID);
         } else if (this.holdingEntity instanceof AbstractDecorationEntity) {
            BlockPos lv9 = ((AbstractDecorationEntity)this.holdingEntity).getDecorationBlockPos();
            lv6.putInt("X", lv9.getX());
            lv6.putInt("Y", lv9.getY());
            lv6.putInt("Z", lv9.getZ());
         }

         nbt.put("Leash", lv6);
      } else if (this.leashNbt != null) {
         nbt.put("Leash", this.leashNbt.copy());
      }

      nbt.putBoolean("LeftHanded", this.isLeftHanded());
      if (this.lootTable != null) {
         nbt.putString("DeathLootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            nbt.putLong("DeathLootTableSeed", this.lootTableSeed);
         }
      }

      if (this.isAiDisabled()) {
         nbt.putBoolean("NoAI", this.isAiDisabled());
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("CanPickUpLoot", NbtElement.BYTE_TYPE)) {
         this.setCanPickUpLoot(nbt.getBoolean("CanPickUpLoot"));
      }

      this.persistent = nbt.getBoolean("PersistenceRequired");
      NbtList lv;
      int i;
      if (nbt.contains("ArmorItems", NbtElement.LIST_TYPE)) {
         lv = nbt.getList("ArmorItems", NbtElement.COMPOUND_TYPE);

         for(i = 0; i < this.armorItems.size(); ++i) {
            this.armorItems.set(i, ItemStack.fromNbt(lv.getCompound(i)));
         }
      }

      if (nbt.contains("HandItems", NbtElement.LIST_TYPE)) {
         lv = nbt.getList("HandItems", NbtElement.COMPOUND_TYPE);

         for(i = 0; i < this.handItems.size(); ++i) {
            this.handItems.set(i, ItemStack.fromNbt(lv.getCompound(i)));
         }
      }

      if (nbt.contains("ArmorDropChances", NbtElement.LIST_TYPE)) {
         lv = nbt.getList("ArmorDropChances", NbtElement.FLOAT_TYPE);

         for(i = 0; i < lv.size(); ++i) {
            this.armorDropChances[i] = lv.getFloat(i);
         }
      }

      if (nbt.contains("HandDropChances", NbtElement.LIST_TYPE)) {
         lv = nbt.getList("HandDropChances", NbtElement.FLOAT_TYPE);

         for(i = 0; i < lv.size(); ++i) {
            this.handDropChances[i] = lv.getFloat(i);
         }
      }

      if (nbt.contains("Leash", NbtElement.COMPOUND_TYPE)) {
         this.leashNbt = nbt.getCompound("Leash");
      }

      this.setLeftHanded(nbt.getBoolean("LeftHanded"));
      if (nbt.contains("DeathLootTable", NbtElement.STRING_TYPE)) {
         this.lootTable = new Identifier(nbt.getString("DeathLootTable"));
         this.lootTableSeed = nbt.getLong("DeathLootTableSeed");
      }

      this.setAiDisabled(nbt.getBoolean("NoAI"));
   }

   protected void dropLoot(DamageSource source, boolean causedByPlayer) {
      super.dropLoot(source, causedByPlayer);
      this.lootTable = null;
   }

   protected LootContext.Builder getLootContextBuilder(boolean causedByPlayer, DamageSource source) {
      return super.getLootContextBuilder(causedByPlayer, source).random(this.lootTableSeed, this.random);
   }

   public final Identifier getLootTable() {
      return this.lootTable == null ? this.getLootTableId() : this.lootTable;
   }

   protected Identifier getLootTableId() {
      return super.getLootTable();
   }

   public void setForwardSpeed(float forwardSpeed) {
      this.forwardSpeed = forwardSpeed;
   }

   public void setUpwardSpeed(float upwardSpeed) {
      this.upwardSpeed = upwardSpeed;
   }

   public void setSidewaysSpeed(float sidewaysSpeed) {
      this.sidewaysSpeed = sidewaysSpeed;
   }

   public void setMovementSpeed(float movementSpeed) {
      super.setMovementSpeed(movementSpeed);
      this.setForwardSpeed(movementSpeed);
   }

   public void tickMovement() {
      super.tickMovement();
      this.getWorld().getProfiler().push("looting");
      if (!this.getWorld().isClient && this.canPickUpLoot() && this.isAlive() && !this.dead && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
         Vec3i lv = this.getItemPickUpRangeExpander();
         List list = this.getWorld().getNonSpectatingEntities(ItemEntity.class, this.getBoundingBox().expand((double)lv.getX(), (double)lv.getY(), (double)lv.getZ()));
         Iterator var3 = list.iterator();

         while(var3.hasNext()) {
            ItemEntity lv2 = (ItemEntity)var3.next();
            if (!lv2.isRemoved() && !lv2.getStack().isEmpty() && !lv2.cannotPickup() && this.canGather(lv2.getStack())) {
               this.loot(lv2);
            }
         }
      }

      this.getWorld().getProfiler().pop();
   }

   protected Vec3i getItemPickUpRangeExpander() {
      return ITEM_PICK_UP_RANGE_EXPANDER;
   }

   protected void loot(ItemEntity item) {
      ItemStack lv = item.getStack();
      ItemStack lv2 = this.tryEquip(lv.copy());
      if (!lv2.isEmpty()) {
         this.triggerItemPickedUpByEntityCriteria(item);
         this.sendPickup(item, lv2.getCount());
         lv.decrement(lv2.getCount());
         if (lv.isEmpty()) {
            item.discard();
         }
      }

   }

   public ItemStack tryEquip(ItemStack stack) {
      EquipmentSlot lv = getPreferredEquipmentSlot(stack);
      ItemStack lv2 = this.getEquippedStack(lv);
      boolean bl = this.prefersNewEquipment(stack, lv2);
      if (lv.isArmorSlot() && !bl) {
         lv = EquipmentSlot.MAINHAND;
         lv2 = this.getEquippedStack(lv);
         bl = lv2.isEmpty();
      }

      if (bl && this.canPickupItem(stack)) {
         double d = (double)this.getDropChance(lv);
         if (!lv2.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d) {
            this.dropStack(lv2);
         }

         if (lv.isArmorSlot() && stack.getCount() > 1) {
            ItemStack lv3 = stack.copyWithCount(1);
            this.equipLootStack(lv, lv3);
            return lv3;
         } else {
            this.equipLootStack(lv, stack);
            return stack;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   protected void equipLootStack(EquipmentSlot slot, ItemStack stack) {
      this.equipStack(slot, stack);
      this.updateDropChances(slot);
      this.persistent = true;
   }

   public void updateDropChances(EquipmentSlot slot) {
      switch (slot.getType()) {
         case HAND:
            this.handDropChances[slot.getEntitySlotId()] = 2.0F;
            break;
         case ARMOR:
            this.armorDropChances[slot.getEntitySlotId()] = 2.0F;
      }

   }

   protected boolean prefersNewEquipment(ItemStack newStack, ItemStack oldStack) {
      if (oldStack.isEmpty()) {
         return true;
      } else if (newStack.getItem() instanceof SwordItem) {
         if (!(oldStack.getItem() instanceof SwordItem)) {
            return true;
         } else {
            SwordItem lv = (SwordItem)newStack.getItem();
            SwordItem lv2 = (SwordItem)oldStack.getItem();
            if (lv.getAttackDamage() != lv2.getAttackDamage()) {
               return lv.getAttackDamage() > lv2.getAttackDamage();
            } else {
               return this.prefersNewDamageableItem(newStack, oldStack);
            }
         }
      } else if (newStack.getItem() instanceof BowItem && oldStack.getItem() instanceof BowItem) {
         return this.prefersNewDamageableItem(newStack, oldStack);
      } else if (newStack.getItem() instanceof CrossbowItem && oldStack.getItem() instanceof CrossbowItem) {
         return this.prefersNewDamageableItem(newStack, oldStack);
      } else if (newStack.getItem() instanceof ArmorItem) {
         if (EnchantmentHelper.hasBindingCurse(oldStack)) {
            return false;
         } else if (!(oldStack.getItem() instanceof ArmorItem)) {
            return true;
         } else {
            ArmorItem lv3 = (ArmorItem)newStack.getItem();
            ArmorItem lv4 = (ArmorItem)oldStack.getItem();
            if (lv3.getProtection() != lv4.getProtection()) {
               return lv3.getProtection() > lv4.getProtection();
            } else if (lv3.getToughness() != lv4.getToughness()) {
               return lv3.getToughness() > lv4.getToughness();
            } else {
               return this.prefersNewDamageableItem(newStack, oldStack);
            }
         }
      } else {
         if (newStack.getItem() instanceof MiningToolItem) {
            if (oldStack.getItem() instanceof BlockItem) {
               return true;
            }

            if (oldStack.getItem() instanceof MiningToolItem) {
               MiningToolItem lv5 = (MiningToolItem)newStack.getItem();
               MiningToolItem lv6 = (MiningToolItem)oldStack.getItem();
               if (lv5.getAttackDamage() != lv6.getAttackDamage()) {
                  return lv5.getAttackDamage() > lv6.getAttackDamage();
               }

               return this.prefersNewDamageableItem(newStack, oldStack);
            }
         }

         return false;
      }
   }

   public boolean prefersNewDamageableItem(ItemStack newStack, ItemStack oldStack) {
      if (newStack.getDamage() >= oldStack.getDamage() && (!newStack.hasNbt() || oldStack.hasNbt())) {
         if (newStack.hasNbt() && oldStack.hasNbt()) {
            return newStack.getNbt().getKeys().stream().anyMatch((key) -> {
               return !key.equals("Damage");
            }) && !oldStack.getNbt().getKeys().stream().anyMatch((key) -> {
               return !key.equals("Damage");
            });
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public boolean canPickupItem(ItemStack stack) {
      return true;
   }

   public boolean canGather(ItemStack stack) {
      return this.canPickupItem(stack);
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return true;
   }

   public boolean cannotDespawn() {
      return this.hasVehicle();
   }

   protected boolean isDisallowedInPeaceful() {
      return false;
   }

   public void checkDespawn() {
      if (this.getWorld().getDifficulty() == Difficulty.PEACEFUL && this.isDisallowedInPeaceful()) {
         this.discard();
      } else if (!this.isPersistent() && !this.cannotDespawn()) {
         Entity lv = this.getWorld().getClosestPlayer(this, -1.0);
         if (lv != null) {
            double d = lv.squaredDistanceTo((Entity)this);
            int i = this.getType().getSpawnGroup().getImmediateDespawnRange();
            int j = i * i;
            if (d > (double)j && this.canImmediatelyDespawn(d)) {
               this.discard();
            }

            int k = this.getType().getSpawnGroup().getDespawnStartRange();
            int l = k * k;
            if (this.despawnCounter > 600 && this.random.nextInt(800) == 0 && d > (double)l && this.canImmediatelyDespawn(d)) {
               this.discard();
            } else if (d < (double)l) {
               this.despawnCounter = 0;
            }
         }

      } else {
         this.despawnCounter = 0;
      }
   }

   protected final void tickNewAi() {
      ++this.despawnCounter;
      this.getWorld().getProfiler().push("sensing");
      this.visibilityCache.clear();
      this.getWorld().getProfiler().pop();
      int i = this.getWorld().getServer().getTicks() + this.getId();
      if (i % 2 != 0 && this.age > 1) {
         this.getWorld().getProfiler().push("targetSelector");
         this.targetSelector.tickGoals(false);
         this.getWorld().getProfiler().pop();
         this.getWorld().getProfiler().push("goalSelector");
         this.goalSelector.tickGoals(false);
         this.getWorld().getProfiler().pop();
      } else {
         this.getWorld().getProfiler().push("targetSelector");
         this.targetSelector.tick();
         this.getWorld().getProfiler().pop();
         this.getWorld().getProfiler().push("goalSelector");
         this.goalSelector.tick();
         this.getWorld().getProfiler().pop();
      }

      this.getWorld().getProfiler().push("navigation");
      this.navigation.tick();
      this.getWorld().getProfiler().pop();
      this.getWorld().getProfiler().push("mob tick");
      this.mobTick();
      this.getWorld().getProfiler().pop();
      this.getWorld().getProfiler().push("controls");
      this.getWorld().getProfiler().push("move");
      this.moveControl.tick();
      this.getWorld().getProfiler().swap("look");
      this.lookControl.tick();
      this.getWorld().getProfiler().swap("jump");
      this.jumpControl.tick();
      this.getWorld().getProfiler().pop();
      this.getWorld().getProfiler().pop();
      this.sendAiDebugData();
   }

   protected void sendAiDebugData() {
      DebugInfoSender.sendGoalSelector(this.getWorld(), this, this.goalSelector);
   }

   protected void mobTick() {
   }

   public int getMaxLookPitchChange() {
      return 40;
   }

   public int getMaxHeadRotation() {
      return 75;
   }

   public int getMaxLookYawChange() {
      return 10;
   }

   public void lookAtEntity(Entity targetEntity, float maxYawChange, float maxPitchChange) {
      double d = targetEntity.getX() - this.getX();
      double e = targetEntity.getZ() - this.getZ();
      double h;
      if (targetEntity instanceof LivingEntity lv) {
         h = lv.getEyeY() - this.getEyeY();
      } else {
         h = (targetEntity.getBoundingBox().minY + targetEntity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
      }

      double i = Math.sqrt(d * d + e * e);
      float j = (float)(MathHelper.atan2(e, d) * 57.2957763671875) - 90.0F;
      float k = (float)(-(MathHelper.atan2(h, i) * 57.2957763671875));
      this.setPitch(this.changeAngle(this.getPitch(), k, maxPitchChange));
      this.setYaw(this.changeAngle(this.getYaw(), j, maxYawChange));
   }

   private float changeAngle(float from, float to, float max) {
      float i = MathHelper.wrapDegrees(to - from);
      if (i > max) {
         i = max;
      }

      if (i < -max) {
         i = -max;
      }

      return from + i;
   }

   public static boolean canMobSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      BlockPos lv = pos.down();
      return spawnReason == SpawnReason.SPAWNER || world.getBlockState(lv).allowsSpawning(world, lv, type);
   }

   public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
      return true;
   }

   public boolean canSpawn(WorldView world) {
      return !world.containsFluid(this.getBoundingBox()) && world.doesNotIntersectEntities(this);
   }

   public int getLimitPerChunk() {
      return 4;
   }

   public boolean spawnsTooManyForEachTry(int count) {
      return false;
   }

   public int getSafeFallDistance() {
      if (this.getTarget() == null) {
         return 3;
      } else {
         int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
         i -= (3 - this.getWorld().getDifficulty().getId()) * 4;
         if (i < 0) {
            i = 0;
         }

         return i + 3;
      }
   }

   public Iterable getHandItems() {
      return this.handItems;
   }

   public Iterable getArmorItems() {
      return this.armorItems;
   }

   public ItemStack getEquippedStack(EquipmentSlot slot) {
      switch (slot.getType()) {
         case HAND:
            return (ItemStack)this.handItems.get(slot.getEntitySlotId());
         case ARMOR:
            return (ItemStack)this.armorItems.get(slot.getEntitySlotId());
         default:
            return ItemStack.EMPTY;
      }
   }

   public void equipStack(EquipmentSlot slot, ItemStack stack) {
      this.processEquippedStack(stack);
      switch (slot.getType()) {
         case HAND:
            this.onEquipStack(slot, (ItemStack)this.handItems.set(slot.getEntitySlotId(), stack), stack);
            break;
         case ARMOR:
            this.onEquipStack(slot, (ItemStack)this.armorItems.set(slot.getEntitySlotId(), stack), stack);
      }

   }

   protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
      super.dropEquipment(source, lootingMultiplier, allowDrops);
      EquipmentSlot[] var4 = EquipmentSlot.values();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         EquipmentSlot lv = var4[var6];
         ItemStack lv2 = this.getEquippedStack(lv);
         float f = this.getDropChance(lv);
         boolean bl2 = f > 1.0F;
         if (!lv2.isEmpty() && !EnchantmentHelper.hasVanishingCurse(lv2) && (allowDrops || bl2) && Math.max(this.random.nextFloat() - (float)lootingMultiplier * 0.01F, 0.0F) < f) {
            if (!bl2 && lv2.isDamageable()) {
               lv2.setDamage(lv2.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(lv2.getMaxDamage() - 3, 1))));
            }

            this.dropStack(lv2);
            this.equipStack(lv, ItemStack.EMPTY);
         }
      }

   }

   protected float getDropChance(EquipmentSlot slot) {
      float f;
      switch (slot.getType()) {
         case HAND:
            f = this.handDropChances[slot.getEntitySlotId()];
            break;
         case ARMOR:
            f = this.armorDropChances[slot.getEntitySlotId()];
            break;
         default:
            f = 0.0F;
      }

      return f;
   }

   protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
      if (random.nextFloat() < 0.15F * localDifficulty.getClampedLocalDifficulty()) {
         int i = random.nextInt(2);
         float f = this.getWorld().getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;
         if (random.nextFloat() < 0.095F) {
            ++i;
         }

         if (random.nextFloat() < 0.095F) {
            ++i;
         }

         if (random.nextFloat() < 0.095F) {
            ++i;
         }

         boolean bl = true;
         EquipmentSlot[] var6 = EquipmentSlot.values();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            EquipmentSlot lv = var6[var8];
            if (lv.getType() == EquipmentSlot.Type.ARMOR) {
               ItemStack lv2 = this.getEquippedStack(lv);
               if (!bl && random.nextFloat() < f) {
                  break;
               }

               bl = false;
               if (lv2.isEmpty()) {
                  Item lv3 = getEquipmentForSlot(lv, i);
                  if (lv3 != null) {
                     this.equipStack(lv, new ItemStack(lv3));
                  }
               }
            }
         }
      }

   }

   @Nullable
   public static Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int equipmentLevel) {
      switch (equipmentSlot) {
         case HEAD:
            if (equipmentLevel == 0) {
               return Items.LEATHER_HELMET;
            } else if (equipmentLevel == 1) {
               return Items.GOLDEN_HELMET;
            } else if (equipmentLevel == 2) {
               return Items.CHAINMAIL_HELMET;
            } else if (equipmentLevel == 3) {
               return Items.IRON_HELMET;
            } else if (equipmentLevel == 4) {
               return Items.DIAMOND_HELMET;
            }
         case CHEST:
            if (equipmentLevel == 0) {
               return Items.LEATHER_CHESTPLATE;
            } else if (equipmentLevel == 1) {
               return Items.GOLDEN_CHESTPLATE;
            } else if (equipmentLevel == 2) {
               return Items.CHAINMAIL_CHESTPLATE;
            } else if (equipmentLevel == 3) {
               return Items.IRON_CHESTPLATE;
            } else if (equipmentLevel == 4) {
               return Items.DIAMOND_CHESTPLATE;
            }
         case LEGS:
            if (equipmentLevel == 0) {
               return Items.LEATHER_LEGGINGS;
            } else if (equipmentLevel == 1) {
               return Items.GOLDEN_LEGGINGS;
            } else if (equipmentLevel == 2) {
               return Items.CHAINMAIL_LEGGINGS;
            } else if (equipmentLevel == 3) {
               return Items.IRON_LEGGINGS;
            } else if (equipmentLevel == 4) {
               return Items.DIAMOND_LEGGINGS;
            }
         case FEET:
            if (equipmentLevel == 0) {
               return Items.LEATHER_BOOTS;
            } else if (equipmentLevel == 1) {
               return Items.GOLDEN_BOOTS;
            } else if (equipmentLevel == 2) {
               return Items.CHAINMAIL_BOOTS;
            } else if (equipmentLevel == 3) {
               return Items.IRON_BOOTS;
            } else if (equipmentLevel == 4) {
               return Items.DIAMOND_BOOTS;
            }
         default:
            return null;
      }
   }

   protected void updateEnchantments(Random random, LocalDifficulty localDifficulty) {
      float f = localDifficulty.getClampedLocalDifficulty();
      this.enchantMainHandItem(random, f);
      EquipmentSlot[] var4 = EquipmentSlot.values();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         EquipmentSlot lv = var4[var6];
         if (lv.getType() == EquipmentSlot.Type.ARMOR) {
            this.enchantEquipment(random, f, lv);
         }
      }

   }

   protected void enchantMainHandItem(Random random, float power) {
      if (!this.getMainHandStack().isEmpty() && random.nextFloat() < 0.25F * power) {
         this.equipStack(EquipmentSlot.MAINHAND, EnchantmentHelper.enchant(random, this.getMainHandStack(), (int)(5.0F + power * (float)random.nextInt(18)), false));
      }

   }

   protected void enchantEquipment(Random random, float power, EquipmentSlot slot) {
      ItemStack lv = this.getEquippedStack(slot);
      if (!lv.isEmpty() && random.nextFloat() < 0.5F * power) {
         this.equipStack(slot, EnchantmentHelper.enchant(random, lv, (int)(5.0F + power * (float)random.nextInt(18)), false));
      }

   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      Random lv = world.getRandom();
      this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).addPersistentModifier(new EntityAttributeModifier("Random spawn bonus", lv.nextTriangular(0.0, 0.11485000000000001), EntityAttributeModifier.Operation.MULTIPLY_BASE));
      if (lv.nextFloat() < 0.05F) {
         this.setLeftHanded(true);
      } else {
         this.setLeftHanded(false);
      }

      return entityData;
   }

   public void setPersistent() {
      this.persistent = true;
   }

   public void setEquipmentDropChance(EquipmentSlot slot, float chance) {
      switch (slot.getType()) {
         case HAND:
            this.handDropChances[slot.getEntitySlotId()] = chance;
            break;
         case ARMOR:
            this.armorDropChances[slot.getEntitySlotId()] = chance;
      }

   }

   public boolean canPickUpLoot() {
      return this.canPickUpLoot;
   }

   public void setCanPickUpLoot(boolean canPickUpLoot) {
      this.canPickUpLoot = canPickUpLoot;
   }

   public boolean canEquip(ItemStack stack) {
      EquipmentSlot lv = getPreferredEquipmentSlot(stack);
      return this.getEquippedStack(lv).isEmpty() && this.canPickUpLoot();
   }

   public boolean isPersistent() {
      return this.persistent;
   }

   public final ActionResult interact(PlayerEntity player, Hand hand) {
      if (!this.isAlive()) {
         return ActionResult.PASS;
      } else if (this.getHoldingEntity() == player) {
         this.detachLeash(true, !player.getAbilities().creativeMode);
         this.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
         return ActionResult.success(this.getWorld().isClient);
      } else {
         ActionResult lv = this.interactWithItem(player, hand);
         if (lv.isAccepted()) {
            this.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
            return lv;
         } else {
            lv = this.interactMob(player, hand);
            if (lv.isAccepted()) {
               this.emitGameEvent(GameEvent.ENTITY_INTERACT, player);
               return lv;
            } else {
               return super.interact(player, hand);
            }
         }
      }
   }

   private ActionResult interactWithItem(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (lv.isOf(Items.LEAD) && this.canBeLeashedBy(player)) {
         this.attachLeash(player, true);
         lv.decrement(1);
         return ActionResult.success(this.getWorld().isClient);
      } else {
         if (lv.isOf(Items.NAME_TAG)) {
            ActionResult lv2 = lv.useOnEntity(player, this, hand);
            if (lv2.isAccepted()) {
               return lv2;
            }
         }

         if (lv.getItem() instanceof SpawnEggItem) {
            if (this.getWorld() instanceof ServerWorld) {
               SpawnEggItem lv3 = (SpawnEggItem)lv.getItem();
               Optional optional = lv3.spawnBaby(player, this, this.getType(), (ServerWorld)this.getWorld(), this.getPos(), lv);
               optional.ifPresent((entity) -> {
                  this.onPlayerSpawnedChild(player, entity);
               });
               return optional.isPresent() ? ActionResult.SUCCESS : ActionResult.PASS;
            } else {
               return ActionResult.CONSUME;
            }
         } else {
            return ActionResult.PASS;
         }
      }
   }

   protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
   }

   protected ActionResult interactMob(PlayerEntity player, Hand hand) {
      return ActionResult.PASS;
   }

   public boolean isInWalkTargetRange() {
      return this.isInWalkTargetRange(this.getBlockPos());
   }

   public boolean isInWalkTargetRange(BlockPos pos) {
      if (this.positionTargetRange == -1.0F) {
         return true;
      } else {
         return this.positionTarget.getSquaredDistance(pos) < (double)(this.positionTargetRange * this.positionTargetRange);
      }
   }

   public void setPositionTarget(BlockPos target, int range) {
      this.positionTarget = target;
      this.positionTargetRange = (float)range;
   }

   public BlockPos getPositionTarget() {
      return this.positionTarget;
   }

   public float getPositionTargetRange() {
      return this.positionTargetRange;
   }

   public void clearPositionTarget() {
      this.positionTargetRange = -1.0F;
   }

   public boolean hasPositionTarget() {
      return this.positionTargetRange != -1.0F;
   }

   @Nullable
   public MobEntity convertTo(EntityType entityType, boolean keepEquipment) {
      if (this.isRemoved()) {
         return null;
      } else {
         MobEntity lv = (MobEntity)entityType.create(this.getWorld());
         if (lv == null) {
            return null;
         } else {
            lv.copyPositionAndRotation(this);
            lv.setBaby(this.isBaby());
            lv.setAiDisabled(this.isAiDisabled());
            if (this.hasCustomName()) {
               lv.setCustomName(this.getCustomName());
               lv.setCustomNameVisible(this.isCustomNameVisible());
            }

            if (this.isPersistent()) {
               lv.setPersistent();
            }

            lv.setInvulnerable(this.isInvulnerable());
            if (keepEquipment) {
               lv.setCanPickUpLoot(this.canPickUpLoot());
               EquipmentSlot[] var4 = EquipmentSlot.values();
               int var5 = var4.length;

               for(int var6 = 0; var6 < var5; ++var6) {
                  EquipmentSlot lv2 = var4[var6];
                  ItemStack lv3 = this.getEquippedStack(lv2);
                  if (!lv3.isEmpty()) {
                     lv.equipStack(lv2, lv3.copyAndEmpty());
                     lv.setEquipmentDropChance(lv2, this.getDropChance(lv2));
                  }
               }
            }

            this.getWorld().spawnEntity(lv);
            if (this.hasVehicle()) {
               Entity lv4 = this.getVehicle();
               this.stopRiding();
               lv.startRiding(lv4, true);
            }

            this.discard();
            return lv;
         }
      }
   }

   protected void updateLeash() {
      if (this.leashNbt != null) {
         this.readLeashNbt();
      }

      if (this.holdingEntity != null) {
         if (!this.isAlive() || !this.holdingEntity.isAlive()) {
            this.detachLeash(true, true);
         }

      }
   }

   public void detachLeash(boolean sendPacket, boolean dropItem) {
      if (this.holdingEntity != null) {
         this.holdingEntity = null;
         this.leashNbt = null;
         if (!this.getWorld().isClient && dropItem) {
            this.dropItem(Items.LEAD);
         }

         if (!this.getWorld().isClient && sendPacket && this.getWorld() instanceof ServerWorld) {
            ((ServerWorld)this.getWorld()).getChunkManager().sendToOtherNearbyPlayers(this, new EntityAttachS2CPacket(this, (Entity)null));
         }
      }

   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return !this.isLeashed() && !(this instanceof Monster);
   }

   public boolean isLeashed() {
      return this.holdingEntity != null;
   }

   @Nullable
   public Entity getHoldingEntity() {
      if (this.holdingEntity == null && this.holdingEntityId != 0 && this.getWorld().isClient) {
         this.holdingEntity = this.getWorld().getEntityById(this.holdingEntityId);
      }

      return this.holdingEntity;
   }

   public void attachLeash(Entity entity, boolean sendPacket) {
      this.holdingEntity = entity;
      this.leashNbt = null;
      if (!this.getWorld().isClient && sendPacket && this.getWorld() instanceof ServerWorld) {
         ((ServerWorld)this.getWorld()).getChunkManager().sendToOtherNearbyPlayers(this, new EntityAttachS2CPacket(this, this.holdingEntity));
      }

      if (this.hasVehicle()) {
         this.stopRiding();
      }

   }

   public void setHoldingEntityId(int id) {
      this.holdingEntityId = id;
      this.detachLeash(false, false);
   }

   public boolean startRiding(Entity entity, boolean force) {
      boolean bl2 = super.startRiding(entity, force);
      if (bl2 && this.isLeashed()) {
         this.detachLeash(true, true);
      }

      return bl2;
   }

   private void readLeashNbt() {
      if (this.leashNbt != null && this.getWorld() instanceof ServerWorld) {
         if (this.leashNbt.containsUuid("UUID")) {
            UUID uUID = this.leashNbt.getUuid("UUID");
            Entity lv = ((ServerWorld)this.getWorld()).getEntity(uUID);
            if (lv != null) {
               this.attachLeash(lv, true);
               return;
            }
         } else if (this.leashNbt.contains("X", NbtElement.NUMBER_TYPE) && this.leashNbt.contains("Y", NbtElement.NUMBER_TYPE) && this.leashNbt.contains("Z", NbtElement.NUMBER_TYPE)) {
            BlockPos lv2 = NbtHelper.toBlockPos(this.leashNbt);
            this.attachLeash(LeashKnotEntity.getOrCreate(this.getWorld(), lv2), true);
            return;
         }

         if (this.age > 100) {
            this.dropItem(Items.LEAD);
            this.leashNbt = null;
         }
      }

   }

   public boolean canMoveVoluntarily() {
      return super.canMoveVoluntarily() && !this.isAiDisabled();
   }

   public void setAiDisabled(boolean aiDisabled) {
      byte b = (Byte)this.dataTracker.get(MOB_FLAGS);
      this.dataTracker.set(MOB_FLAGS, aiDisabled ? (byte)(b | 1) : (byte)(b & -2));
   }

   public void setLeftHanded(boolean leftHanded) {
      byte b = (Byte)this.dataTracker.get(MOB_FLAGS);
      this.dataTracker.set(MOB_FLAGS, leftHanded ? (byte)(b | 2) : (byte)(b & -3));
   }

   public void setAttacking(boolean attacking) {
      byte b = (Byte)this.dataTracker.get(MOB_FLAGS);
      this.dataTracker.set(MOB_FLAGS, attacking ? (byte)(b | 4) : (byte)(b & -5));
   }

   public boolean isAiDisabled() {
      return ((Byte)this.dataTracker.get(MOB_FLAGS) & 1) != 0;
   }

   public boolean isLeftHanded() {
      return ((Byte)this.dataTracker.get(MOB_FLAGS) & 2) != 0;
   }

   public boolean isAttacking() {
      return ((Byte)this.dataTracker.get(MOB_FLAGS) & 4) != 0;
   }

   public void setBaby(boolean baby) {
   }

   public Arm getMainArm() {
      return this.isLeftHanded() ? Arm.LEFT : Arm.RIGHT;
   }

   public double squaredAttackRange(LivingEntity target) {
      return (double)(this.getWidth() * 2.0F * this.getWidth() * 2.0F + target.getWidth());
   }

   public double getSquaredDistanceToAttackPosOf(LivingEntity target) {
      return Math.max(this.squaredDistanceTo(target.getAttackPos()), this.squaredDistanceTo(target.getPos()));
   }

   public boolean isInAttackRange(LivingEntity entity) {
      double d = this.getSquaredDistanceToAttackPosOf(entity);
      return d <= this.squaredAttackRange(entity);
   }

   public boolean tryAttack(Entity target) {
      float f = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
      float g = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
      if (target instanceof LivingEntity) {
         f += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), ((LivingEntity)target).getGroup());
         g += (float)EnchantmentHelper.getKnockback(this);
      }

      int i = EnchantmentHelper.getFireAspect(this);
      if (i > 0) {
         target.setOnFireFor(i * 4);
      }

      boolean bl = target.damage(this.getDamageSources().mobAttack(this), f);
      if (bl) {
         if (g > 0.0F && target instanceof LivingEntity) {
            ((LivingEntity)target).takeKnockback((double)(g * 0.5F), (double)MathHelper.sin(this.getYaw() * 0.017453292F), (double)(-MathHelper.cos(this.getYaw() * 0.017453292F)));
            this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
         }

         if (target instanceof PlayerEntity) {
            PlayerEntity lv = (PlayerEntity)target;
            this.disablePlayerShield(lv, this.getMainHandStack(), lv.isUsingItem() ? lv.getActiveItem() : ItemStack.EMPTY);
         }

         this.applyDamageEffects(this, target);
         this.onAttacking(target);
      }

      return bl;
   }

   private void disablePlayerShield(PlayerEntity player, ItemStack mobStack, ItemStack playerStack) {
      if (!mobStack.isEmpty() && !playerStack.isEmpty() && mobStack.getItem() instanceof AxeItem && playerStack.isOf(Items.SHIELD)) {
         float f = 0.25F + (float)EnchantmentHelper.getEfficiency(this) * 0.05F;
         if (this.random.nextFloat() < f) {
            player.getItemCooldownManager().set(Items.SHIELD, 100);
            this.getWorld().sendEntityStatus(player, EntityStatuses.BREAK_SHIELD);
         }
      }

   }

   protected boolean isAffectedByDaylight() {
      if (this.getWorld().isDay() && !this.getWorld().isClient) {
         float f = this.getBrightnessAtEyes();
         BlockPos lv = BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ());
         boolean bl = this.isWet() || this.inPowderSnow || this.wasInPowderSnow;
         if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !bl && this.getWorld().isSkyVisible(lv)) {
            return true;
         }
      }

      return false;
   }

   protected void swimUpward(TagKey fluid) {
      if (this.getNavigation().canSwim()) {
         super.swimUpward(fluid);
      } else {
         this.setVelocity(this.getVelocity().add(0.0, 0.3, 0.0));
      }

   }

   public void clearGoalsAndTasks() {
      this.clearGoals((goal) -> {
         return true;
      });
      this.getBrain().clear();
   }

   public void clearGoals(Predicate predicate) {
      this.goalSelector.clear(predicate);
   }

   protected void removeFromDimension() {
      super.removeFromDimension();
      this.detachLeash(true, false);
      this.getItemsEquipped().forEach((stack) -> {
         if (!stack.isEmpty()) {
            stack.setCount(0);
         }

      });
   }

   @Nullable
   public ItemStack getPickBlockStack() {
      SpawnEggItem lv = SpawnEggItem.forEntity(this.getType());
      return lv == null ? null : new ItemStack(lv);
   }

   static {
      MOB_FLAGS = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.BYTE);
      ITEM_PICK_UP_RANGE_EXPANDER = new Vec3i(1, 0, 1);
   }
}
