package net.minecraft.client.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HangingSignEditScreen;
import net.minecraft.client.gui.screen.ingame.JigsawBlockScreen;
import net.minecraft.client.gui.screen.ingame.MinecartCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.sound.AmbientSoundLoops;
import net.minecraft.client.sound.AmbientSoundPlayer;
import net.minecraft.client.sound.BiomeEffectSoundPlayer;
import net.minecraft.client.sound.BubbleColumnSoundPlayer;
import net.minecraft.client.sound.ElytraSoundInstance;
import net.minecraft.client.sound.MinecartInsideSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CommandBlockExecutor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientPlayerEntity extends AbstractClientPlayerEntity {
   public static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_32671 = 20;
   private static final int field_32672 = 600;
   private static final int field_32673 = 100;
   private static final float field_32674 = 0.6F;
   private static final double field_32675 = 0.35;
   private static final double MAX_SOFT_COLLISION_RADIANS = 0.13962633907794952;
   private static final float field_38337 = 0.3F;
   public final ClientPlayNetworkHandler networkHandler;
   private final StatHandler statHandler;
   private final ClientRecipeBook recipeBook;
   private final List tickables = Lists.newArrayList();
   private int clientPermissionLevel = 0;
   private double lastX;
   private double lastBaseY;
   private double lastZ;
   private float lastYaw;
   private float lastPitch;
   private boolean lastOnGround;
   private boolean inSneakingPose;
   private boolean lastSneaking;
   private boolean lastSprinting;
   private int ticksSinceLastPositionPacketSent;
   private boolean healthInitialized;
   @Nullable
   private String serverBrand;
   public Input input;
   protected final MinecraftClient client;
   protected int ticksLeftToDoubleTapSprint;
   public float renderYaw;
   public float renderPitch;
   public float lastRenderYaw;
   public float lastRenderPitch;
   private int field_3938;
   private float mountJumpStrength;
   public float nextNauseaStrength;
   public float lastNauseaStrength;
   private boolean usingItem;
   @Nullable
   private Hand activeHand;
   private boolean riding;
   private boolean autoJumpEnabled = true;
   private int ticksToNextAutojump;
   private boolean falling;
   private int underwaterVisibilityTicks;
   private boolean showsDeathScreen = true;

   public ClientPlayerEntity(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting) {
      super(world, networkHandler.getProfile());
      this.client = client;
      this.networkHandler = networkHandler;
      this.statHandler = stats;
      this.recipeBook = recipeBook;
      this.lastSneaking = lastSneaking;
      this.lastSprinting = lastSprinting;
      this.tickables.add(new AmbientSoundPlayer(this, client.getSoundManager()));
      this.tickables.add(new BubbleColumnSoundPlayer(this));
      this.tickables.add(new BiomeEffectSoundPlayer(this, client.getSoundManager(), world.getBiomeAccess()));
   }

   public boolean damage(DamageSource source, float amount) {
      return false;
   }

   public void heal(float amount) {
   }

   public boolean startRiding(Entity entity, boolean force) {
      if (!super.startRiding(entity, force)) {
         return false;
      } else {
         if (entity instanceof AbstractMinecartEntity) {
            this.client.getSoundManager().play(new MinecartInsideSoundInstance(this, (AbstractMinecartEntity)entity, true));
            this.client.getSoundManager().play(new MinecartInsideSoundInstance(this, (AbstractMinecartEntity)entity, false));
         }

         return true;
      }
   }

   public void dismountVehicle() {
      super.dismountVehicle();
      this.riding = false;
   }

   public float getPitch(float tickDelta) {
      return this.getPitch();
   }

   public float getYaw(float tickDelta) {
      return this.hasVehicle() ? super.getYaw(tickDelta) : this.getYaw();
   }

   public void tick() {
      if (this.getWorld().isPosLoaded(this.getBlockX(), this.getBlockZ())) {
         super.tick();
         if (this.hasVehicle()) {
            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.getYaw(), this.getPitch(), this.isOnGround()));
            this.networkHandler.sendPacket(new PlayerInputC2SPacket(this.sidewaysSpeed, this.forwardSpeed, this.input.jumping, this.input.sneaking));
            Entity lv = this.getRootVehicle();
            if (lv != this && lv.isLogicalSideForUpdatingMovement()) {
               this.networkHandler.sendPacket(new VehicleMoveC2SPacket(lv));
               this.sendSprintingPacket();
            }
         } else {
            this.sendMovementPackets();
         }

         Iterator var3 = this.tickables.iterator();

         while(var3.hasNext()) {
            ClientPlayerTickable lv2 = (ClientPlayerTickable)var3.next();
            lv2.tick();
         }

      }
   }

   public float getMoodPercentage() {
      Iterator var1 = this.tickables.iterator();

      ClientPlayerTickable lv;
      do {
         if (!var1.hasNext()) {
            return 0.0F;
         }

         lv = (ClientPlayerTickable)var1.next();
      } while(!(lv instanceof BiomeEffectSoundPlayer));

      return ((BiomeEffectSoundPlayer)lv).getMoodPercentage();
   }

   private void sendMovementPackets() {
      this.sendSprintingPacket();
      boolean bl = this.isSneaking();
      if (bl != this.lastSneaking) {
         ClientCommandC2SPacket.Mode lv = bl ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
         this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, lv));
         this.lastSneaking = bl;
      }

      if (this.isCamera()) {
         double d = this.getX() - this.lastX;
         double e = this.getY() - this.lastBaseY;
         double f = this.getZ() - this.lastZ;
         double g = (double)(this.getYaw() - this.lastYaw);
         double h = (double)(this.getPitch() - this.lastPitch);
         ++this.ticksSinceLastPositionPacketSent;
         boolean bl2 = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
         boolean bl3 = g != 0.0 || h != 0.0;
         if (this.hasVehicle()) {
            Vec3d lv2 = this.getVelocity();
            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(lv2.x, -999.0, lv2.z, this.getYaw(), this.getPitch(), this.isOnGround()));
            bl2 = false;
         } else if (bl2 && bl3) {
            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch(), this.isOnGround()));
         } else if (bl2) {
            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.getX(), this.getY(), this.getZ(), this.isOnGround()));
         } else if (bl3) {
            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.getYaw(), this.getPitch(), this.isOnGround()));
         } else if (this.lastOnGround != this.isOnGround()) {
            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(this.isOnGround()));
         }

         if (bl2) {
            this.lastX = this.getX();
            this.lastBaseY = this.getY();
            this.lastZ = this.getZ();
            this.ticksSinceLastPositionPacketSent = 0;
         }

         if (bl3) {
            this.lastYaw = this.getYaw();
            this.lastPitch = this.getPitch();
         }

         this.lastOnGround = this.isOnGround();
         this.autoJumpEnabled = (Boolean)this.client.options.getAutoJump().getValue();
      }

   }

   private void sendSprintingPacket() {
      boolean bl = this.isSprinting();
      if (bl != this.lastSprinting) {
         ClientCommandC2SPacket.Mode lv = bl ? ClientCommandC2SPacket.Mode.START_SPRINTING : ClientCommandC2SPacket.Mode.STOP_SPRINTING;
         this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, lv));
         this.lastSprinting = bl;
      }

   }

   public boolean dropSelectedItem(boolean entireStack) {
      PlayerActionC2SPacket.Action lv = entireStack ? PlayerActionC2SPacket.Action.DROP_ALL_ITEMS : PlayerActionC2SPacket.Action.DROP_ITEM;
      ItemStack lv2 = this.getInventory().dropSelectedItem(entireStack);
      this.networkHandler.sendPacket(new PlayerActionC2SPacket(lv, BlockPos.ORIGIN, Direction.DOWN));
      return !lv2.isEmpty();
   }

   public void swingHand(Hand hand) {
      super.swingHand(hand);
      this.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
   }

   public void requestRespawn() {
      this.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
   }

   protected void applyDamage(DamageSource source, float amount) {
      if (!this.isInvulnerableTo(source)) {
         this.setHealth(this.getHealth() - amount);
      }
   }

   public void closeHandledScreen() {
      this.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(this.currentScreenHandler.syncId));
      this.closeScreen();
   }

   public void closeScreen() {
      super.closeHandledScreen();
      this.client.setScreen((Screen)null);
   }

   public void updateHealth(float health) {
      if (this.healthInitialized) {
         float g = this.getHealth() - health;
         if (g <= 0.0F) {
            this.setHealth(health);
            if (g < 0.0F) {
               this.timeUntilRegen = 10;
            }
         } else {
            this.lastDamageTaken = g;
            this.timeUntilRegen = 20;
            this.setHealth(health);
            this.maxHurtTime = 10;
            this.hurtTime = this.maxHurtTime;
         }
      } else {
         this.setHealth(health);
         this.healthInitialized = true;
      }

   }

   public void sendAbilitiesUpdate() {
      this.networkHandler.sendPacket(new UpdatePlayerAbilitiesC2SPacket(this.getAbilities()));
   }

   public boolean isMainPlayer() {
      return true;
   }

   public boolean isHoldingOntoLadder() {
      return !this.getAbilities().flying && super.isHoldingOntoLadder();
   }

   public boolean shouldSpawnSprintingParticles() {
      return !this.getAbilities().flying && super.shouldSpawnSprintingParticles();
   }

   public boolean shouldDisplaySoulSpeedEffects() {
      return !this.getAbilities().flying && super.shouldDisplaySoulSpeedEffects();
   }

   protected void startRidingJump() {
      this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_RIDING_JUMP, MathHelper.floor(this.getMountJumpStrength() * 100.0F)));
   }

   public void openRidingInventory() {
      this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.OPEN_INVENTORY));
   }

   public void setServerBrand(@Nullable String serverBrand) {
      this.serverBrand = serverBrand;
   }

   @Nullable
   public String getServerBrand() {
      return this.serverBrand;
   }

   public StatHandler getStatHandler() {
      return this.statHandler;
   }

   public ClientRecipeBook getRecipeBook() {
      return this.recipeBook;
   }

   public void onRecipeDisplayed(Recipe recipe) {
      if (this.recipeBook.shouldDisplay(recipe)) {
         this.recipeBook.onRecipeDisplayed(recipe);
         this.networkHandler.sendPacket(new RecipeBookDataC2SPacket(recipe));
      }

   }

   protected int getPermissionLevel() {
      return this.clientPermissionLevel;
   }

   public void setClientPermissionLevel(int clientPermissionLevel) {
      this.clientPermissionLevel = clientPermissionLevel;
   }

   public void sendMessage(Text message, boolean overlay) {
      this.client.getMessageHandler().onGameMessage(message, overlay);
   }

   private void pushOutOfBlocks(double x, double z) {
      BlockPos lv = BlockPos.ofFloored(x, this.getY(), z);
      if (this.wouldCollideAt(lv)) {
         double f = x - (double)lv.getX();
         double g = z - (double)lv.getZ();
         Direction lv2 = null;
         double h = Double.MAX_VALUE;
         Direction[] lvs = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
         Direction[] var14 = lvs;
         int var15 = lvs.length;

         for(int var16 = 0; var16 < var15; ++var16) {
            Direction lv3 = var14[var16];
            double i = lv3.getAxis().choose(f, 0.0, g);
            double j = lv3.getDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - i : i;
            if (j < h && !this.wouldCollideAt(lv.offset(lv3))) {
               h = j;
               lv2 = lv3;
            }
         }

         if (lv2 != null) {
            Vec3d lv4 = this.getVelocity();
            if (lv2.getAxis() == Direction.Axis.X) {
               this.setVelocity(0.1 * (double)lv2.getOffsetX(), lv4.y, lv4.z);
            } else {
               this.setVelocity(lv4.x, lv4.y, 0.1 * (double)lv2.getOffsetZ());
            }
         }

      }
   }

   private boolean wouldCollideAt(BlockPos pos) {
      Box lv = this.getBoundingBox();
      Box lv2 = (new Box((double)pos.getX(), lv.minY, (double)pos.getZ(), (double)pos.getX() + 1.0, lv.maxY, (double)pos.getZ() + 1.0)).contract(1.0E-7);
      return this.getWorld().canCollide(this, lv2);
   }

   public void setExperience(float progress, int total, int level) {
      this.experienceProgress = progress;
      this.totalExperience = total;
      this.experienceLevel = level;
   }

   public void sendMessage(Text message) {
      this.client.inGameHud.getChatHud().addMessage(message);
   }

   public void handleStatus(byte status) {
      if (status >= EntityStatuses.SET_OP_LEVEL_0 && status <= EntityStatuses.SET_OP_LEVEL_4) {
         this.setClientPermissionLevel(status - EntityStatuses.SET_OP_LEVEL_0);
      } else {
         super.handleStatus(status);
      }

   }

   public void setShowsDeathScreen(boolean showsDeathScreen) {
      this.showsDeathScreen = showsDeathScreen;
   }

   public boolean showsDeathScreen() {
      return this.showsDeathScreen;
   }

   public void playSound(SoundEvent sound, float volume, float pitch) {
      this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch, false);
   }

   public void playSound(SoundEvent event, SoundCategory category, float volume, float pitch) {
      this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), event, category, volume, pitch, false);
   }

   public boolean canMoveVoluntarily() {
      return true;
   }

   public void setCurrentHand(Hand hand) {
      ItemStack lv = this.getStackInHand(hand);
      if (!lv.isEmpty() && !this.isUsingItem()) {
         super.setCurrentHand(hand);
         this.usingItem = true;
         this.activeHand = hand;
      }
   }

   public boolean isUsingItem() {
      return this.usingItem;
   }

   public void clearActiveItem() {
      super.clearActiveItem();
      this.usingItem = false;
   }

   public Hand getActiveHand() {
      return (Hand)Objects.requireNonNullElse(this.activeHand, Hand.MAIN_HAND);
   }

   public void onTrackedDataSet(TrackedData data) {
      super.onTrackedDataSet(data);
      if (LIVING_FLAGS.equals(data)) {
         boolean bl = ((Byte)this.dataTracker.get(LIVING_FLAGS) & 1) > 0;
         Hand lv = ((Byte)this.dataTracker.get(LIVING_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;
         if (bl && !this.usingItem) {
            this.setCurrentHand(lv);
         } else if (!bl && this.usingItem) {
            this.clearActiveItem();
         }
      }

      if (FLAGS.equals(data) && this.isFallFlying() && !this.falling) {
         this.client.getSoundManager().play(new ElytraSoundInstance(this));
      }

   }

   @Nullable
   public JumpingMount getJumpingMount() {
      Entity var2 = this.getControllingVehicle();
      JumpingMount var10000;
      if (var2 instanceof JumpingMount lv) {
         if (lv.canJump()) {
            var10000 = lv;
            return var10000;
         }
      }

      var10000 = null;
      return var10000;
   }

   public float getMountJumpStrength() {
      return this.mountJumpStrength;
   }

   public boolean shouldFilterText() {
      return this.client.shouldFilterText();
   }

   public void openEditSignScreen(SignBlockEntity sign, boolean front) {
      if (sign instanceof HangingSignBlockEntity lv) {
         this.client.setScreen(new HangingSignEditScreen(lv, front, this.client.shouldFilterText()));
      } else {
         this.client.setScreen(new SignEditScreen(sign, front, this.client.shouldFilterText()));
      }

   }

   public void openCommandBlockMinecartScreen(CommandBlockExecutor commandBlockExecutor) {
      this.client.setScreen(new MinecartCommandBlockScreen(commandBlockExecutor));
   }

   public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
      this.client.setScreen(new CommandBlockScreen(commandBlock));
   }

   public void openStructureBlockScreen(StructureBlockBlockEntity structureBlock) {
      this.client.setScreen(new StructureBlockScreen(structureBlock));
   }

   public void openJigsawScreen(JigsawBlockEntity jigsaw) {
      this.client.setScreen(new JigsawBlockScreen(jigsaw));
   }

   public void useBook(ItemStack book, Hand hand) {
      if (book.isOf(Items.WRITABLE_BOOK)) {
         this.client.setScreen(new BookEditScreen(this, book, hand));
      }

   }

   public void addCritParticles(Entity target) {
      this.client.particleManager.addEmitter(target, ParticleTypes.CRIT);
   }

   public void addEnchantedHitParticles(Entity target) {
      this.client.particleManager.addEmitter(target, ParticleTypes.ENCHANTED_HIT);
   }

   public boolean isSneaking() {
      return this.input != null && this.input.sneaking;
   }

   public boolean isInSneakingPose() {
      return this.inSneakingPose;
   }

   public boolean shouldSlowDown() {
      return this.isInSneakingPose() || this.isCrawling();
   }

   public void tickNewAi() {
      super.tickNewAi();
      if (this.isCamera()) {
         this.sidewaysSpeed = this.input.movementSideways;
         this.forwardSpeed = this.input.movementForward;
         this.jumping = this.input.jumping;
         this.lastRenderYaw = this.renderYaw;
         this.lastRenderPitch = this.renderPitch;
         this.renderPitch += (this.getPitch() - this.renderPitch) * 0.5F;
         this.renderYaw += (this.getYaw() - this.renderYaw) * 0.5F;
      }

   }

   protected boolean isCamera() {
      return this.client.getCameraEntity() == this;
   }

   public void init() {
      this.setPose(EntityPose.STANDING);
      if (this.getWorld() != null) {
         for(double d = this.getY(); d > (double)this.getWorld().getBottomY() && d < (double)this.getWorld().getTopY(); ++d) {
            this.setPosition(this.getX(), d, this.getZ());
            if (this.getWorld().isSpaceEmpty(this)) {
               break;
            }
         }

         this.setVelocity(Vec3d.ZERO);
         this.setPitch(0.0F);
      }

      this.setHealth(this.getMaxHealth());
      this.deathTime = 0;
   }

   public void tickMovement() {
      if (this.ticksLeftToDoubleTapSprint > 0) {
         --this.ticksLeftToDoubleTapSprint;
      }

      this.updateNausea();
      boolean bl = this.input.jumping;
      boolean bl2 = this.input.sneaking;
      boolean bl3 = this.isWalking();
      this.inSneakingPose = !this.getAbilities().flying && !this.isSwimming() && this.wouldPoseNotCollide(EntityPose.CROUCHING) && (this.isSneaking() || !this.isSleeping() && !this.wouldPoseNotCollide(EntityPose.STANDING));
      float f = MathHelper.clamp(0.3F + EnchantmentHelper.getSwiftSneakSpeedBoost(this), 0.0F, 1.0F);
      this.input.tick(this.shouldSlowDown(), f);
      this.client.getTutorialManager().onMovement(this.input);
      if (this.isUsingItem() && !this.hasVehicle()) {
         Input var10000 = this.input;
         var10000.movementSideways *= 0.2F;
         var10000 = this.input;
         var10000.movementForward *= 0.2F;
         this.ticksLeftToDoubleTapSprint = 0;
      }

      boolean bl4 = false;
      if (this.ticksToNextAutojump > 0) {
         --this.ticksToNextAutojump;
         bl4 = true;
         this.input.jumping = true;
      }

      if (!this.noClip) {
         this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
         this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
         this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
         this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
      }

      if (bl2) {
         this.ticksLeftToDoubleTapSprint = 0;
      }

      boolean bl5 = this.canStartSprinting();
      boolean bl6 = this.hasVehicle() ? this.getVehicle().isOnGround() : this.isOnGround();
      boolean bl7 = !bl2 && !bl3;
      if ((bl6 || this.isSubmergedInWater()) && bl7 && bl5) {
         if (this.ticksLeftToDoubleTapSprint <= 0 && !this.client.options.sprintKey.isPressed()) {
            this.ticksLeftToDoubleTapSprint = 7;
         } else {
            this.setSprinting(true);
         }
      }

      if ((!this.isTouchingWater() || this.isSubmergedInWater()) && bl5 && this.client.options.sprintKey.isPressed()) {
         this.setSprinting(true);
      }

      boolean bl8;
      if (this.isSprinting()) {
         bl8 = !this.input.hasForwardMovement() || !this.canSprint();
         boolean bl9 = bl8 || this.horizontalCollision && !this.collidedSoftly || this.isTouchingWater() && !this.isSubmergedInWater();
         if (this.isSwimming()) {
            if (!this.isOnGround() && !this.input.sneaking && bl8 || !this.isTouchingWater()) {
               this.setSprinting(false);
            }
         } else if (bl9) {
            this.setSprinting(false);
         }
      }

      bl8 = false;
      if (this.getAbilities().allowFlying) {
         if (this.client.interactionManager.isFlyingLocked()) {
            if (!this.getAbilities().flying) {
               this.getAbilities().flying = true;
               bl8 = true;
               this.sendAbilitiesUpdate();
            }
         } else if (!bl && this.input.jumping && !bl4) {
            if (this.abilityResyncCountdown == 0) {
               this.abilityResyncCountdown = 7;
            } else if (!this.isSwimming()) {
               this.getAbilities().flying = !this.getAbilities().flying;
               bl8 = true;
               this.sendAbilitiesUpdate();
               this.abilityResyncCountdown = 0;
            }
         }
      }

      if (this.input.jumping && !bl8 && !bl && !this.getAbilities().flying && !this.hasVehicle() && !this.isClimbing()) {
         ItemStack lv = this.getEquippedStack(EquipmentSlot.CHEST);
         if (lv.isOf(Items.ELYTRA) && ElytraItem.isUsable(lv) && this.checkFallFlying()) {
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
         }
      }

      this.falling = this.isFallFlying();
      if (this.isTouchingWater() && this.input.sneaking && this.shouldSwimInFluids()) {
         this.knockDownwards();
      }

      int i;
      if (this.isSubmergedIn(FluidTags.WATER)) {
         i = this.isSpectator() ? 10 : 1;
         this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks + i, 0, 600);
      } else if (this.underwaterVisibilityTicks > 0) {
         this.isSubmergedIn(FluidTags.WATER);
         this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks - 10, 0, 600);
      }

      if (this.getAbilities().flying && this.isCamera()) {
         i = 0;
         if (this.input.sneaking) {
            --i;
         }

         if (this.input.jumping) {
            ++i;
         }

         if (i != 0) {
            this.setVelocity(this.getVelocity().add(0.0, (double)((float)i * this.getAbilities().getFlySpeed() * 3.0F), 0.0));
         }
      }

      JumpingMount lv2 = this.getJumpingMount();
      if (lv2 != null && lv2.getJumpCooldown() == 0) {
         if (this.field_3938 < 0) {
            ++this.field_3938;
            if (this.field_3938 == 0) {
               this.mountJumpStrength = 0.0F;
            }
         }

         if (bl && !this.input.jumping) {
            this.field_3938 = -10;
            lv2.setJumpStrength(MathHelper.floor(this.getMountJumpStrength() * 100.0F));
            this.startRidingJump();
         } else if (!bl && this.input.jumping) {
            this.field_3938 = 0;
            this.mountJumpStrength = 0.0F;
         } else if (bl) {
            ++this.field_3938;
            if (this.field_3938 < 10) {
               this.mountJumpStrength = (float)this.field_3938 * 0.1F;
            } else {
               this.mountJumpStrength = 0.8F + 2.0F / (float)(this.field_3938 - 9) * 0.1F;
            }
         }
      } else {
         this.mountJumpStrength = 0.0F;
      }

      super.tickMovement();
      if (this.isOnGround() && this.getAbilities().flying && !this.client.interactionManager.isFlyingLocked()) {
         this.getAbilities().flying = false;
         this.sendAbilitiesUpdate();
      }

   }

   protected void updatePostDeath() {
      ++this.deathTime;
      if (this.deathTime == 20) {
         this.remove(Entity.RemovalReason.KILLED);
      }

   }

   private void updateNausea() {
      this.lastNauseaStrength = this.nextNauseaStrength;
      if (this.inNetherPortal) {
         if (this.client.currentScreen != null && !this.client.currentScreen.shouldPause() && !(this.client.currentScreen instanceof DeathScreen) && !(this.client.currentScreen instanceof DownloadingTerrainScreen)) {
            if (this.client.currentScreen instanceof HandledScreen) {
               this.closeHandledScreen();
            }

            this.client.setScreen((Screen)null);
         }

         if (this.nextNauseaStrength == 0.0F) {
            this.client.getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_PORTAL_TRIGGER, this.random.nextFloat() * 0.4F + 0.8F, 0.25F));
         }

         this.nextNauseaStrength += 0.0125F;
         if (this.nextNauseaStrength >= 1.0F) {
            this.nextNauseaStrength = 1.0F;
         }

         this.inNetherPortal = false;
      } else if (this.hasStatusEffect(StatusEffects.NAUSEA) && !this.getStatusEffect(StatusEffects.NAUSEA).isDurationBelow(60)) {
         this.nextNauseaStrength += 0.006666667F;
         if (this.nextNauseaStrength > 1.0F) {
            this.nextNauseaStrength = 1.0F;
         }
      } else {
         if (this.nextNauseaStrength > 0.0F) {
            this.nextNauseaStrength -= 0.05F;
         }

         if (this.nextNauseaStrength < 0.0F) {
            this.nextNauseaStrength = 0.0F;
         }
      }

      this.tickPortalCooldown();
   }

   public void tickRiding() {
      super.tickRiding();
      this.riding = false;
      Entity var2 = this.getControllingVehicle();
      if (var2 instanceof BoatEntity lv) {
         lv.setInputs(this.input.pressingLeft, this.input.pressingRight, this.input.pressingForward, this.input.pressingBack);
         this.riding |= this.input.pressingLeft || this.input.pressingRight || this.input.pressingForward || this.input.pressingBack;
      }

   }

   public boolean isRiding() {
      return this.riding;
   }

   @Nullable
   public StatusEffectInstance removeStatusEffectInternal(@Nullable StatusEffect type) {
      if (type == StatusEffects.NAUSEA) {
         this.lastNauseaStrength = 0.0F;
         this.nextNauseaStrength = 0.0F;
      }

      return super.removeStatusEffectInternal(type);
   }

   public void move(MovementType movementType, Vec3d movement) {
      double d = this.getX();
      double e = this.getZ();
      super.move(movementType, movement);
      this.autoJump((float)(this.getX() - d), (float)(this.getZ() - e));
   }

   public boolean isAutoJumpEnabled() {
      return this.autoJumpEnabled;
   }

   protected void autoJump(float dx, float dz) {
      if (this.shouldAutoJump()) {
         Vec3d lv = this.getPos();
         Vec3d lv2 = lv.add((double)dx, 0.0, (double)dz);
         Vec3d lv3 = new Vec3d((double)dx, 0.0, (double)dz);
         float h = this.getMovementSpeed();
         float i = (float)lv3.lengthSquared();
         float l;
         if (i <= 0.001F) {
            Vec2f lv4 = this.input.getMovementInput();
            float j = h * lv4.x;
            float k = h * lv4.y;
            l = MathHelper.sin(this.getYaw() * 0.017453292F);
            float m = MathHelper.cos(this.getYaw() * 0.017453292F);
            lv3 = new Vec3d((double)(j * m - k * l), lv3.y, (double)(k * m + j * l));
            i = (float)lv3.lengthSquared();
            if (i <= 0.001F) {
               return;
            }
         }

         float n = MathHelper.inverseSqrt(i);
         Vec3d lv5 = lv3.multiply((double)n);
         Vec3d lv6 = this.getRotationVecClient();
         l = (float)(lv6.x * lv5.x + lv6.z * lv5.z);
         if (!(l < -0.15F)) {
            ShapeContext lv7 = ShapeContext.of(this);
            BlockPos lv8 = BlockPos.ofFloored(this.getX(), this.getBoundingBox().maxY, this.getZ());
            BlockState lv9 = this.getWorld().getBlockState(lv8);
            if (lv9.getCollisionShape(this.getWorld(), lv8, lv7).isEmpty()) {
               lv8 = lv8.up();
               BlockState lv10 = this.getWorld().getBlockState(lv8);
               if (lv10.getCollisionShape(this.getWorld(), lv8, lv7).isEmpty()) {
                  float o = 7.0F;
                  float p = 1.2F;
                  if (this.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                     p += (float)(this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.75F;
                  }

                  float q = Math.max(h * 7.0F, 1.0F / n);
                  Vec3d lv12 = lv2.add(lv5.multiply((double)q));
                  float r = this.getWidth();
                  float s = this.getHeight();
                  Box lv13 = (new Box(lv, lv12.add(0.0, (double)s, 0.0))).expand((double)r, 0.0, (double)r);
                  Vec3d lv11 = lv.add(0.0, 0.5099999904632568, 0.0);
                  lv12 = lv12.add(0.0, 0.5099999904632568, 0.0);
                  Vec3d lv14 = lv5.crossProduct(new Vec3d(0.0, 1.0, 0.0));
                  Vec3d lv15 = lv14.multiply((double)(r * 0.5F));
                  Vec3d lv16 = lv11.subtract(lv15);
                  Vec3d lv17 = lv12.subtract(lv15);
                  Vec3d lv18 = lv11.add(lv15);
                  Vec3d lv19 = lv12.add(lv15);
                  Iterable iterable = this.getWorld().getCollisions(this, lv13);
                  Iterator iterator = StreamSupport.stream(iterable.spliterator(), false).flatMap((shape) -> {
                     return shape.getBoundingBoxes().stream();
                  }).iterator();
                  float t = Float.MIN_VALUE;

                  label73:
                  while(iterator.hasNext()) {
                     Box lv20 = (Box)iterator.next();
                     if (lv20.intersects(lv16, lv17) || lv20.intersects(lv18, lv19)) {
                        t = (float)lv20.maxY;
                        Vec3d lv21 = lv20.getCenter();
                        BlockPos lv22 = BlockPos.ofFloored(lv21);
                        int u = 1;

                        while(true) {
                           if (!((float)u < p)) {
                              break label73;
                           }

                           BlockPos lv23 = lv22.up(u);
                           BlockState lv24 = this.getWorld().getBlockState(lv23);
                           VoxelShape lv25;
                           if (!(lv25 = lv24.getCollisionShape(this.getWorld(), lv23, lv7)).isEmpty()) {
                              t = (float)lv25.getMax(Direction.Axis.Y) + (float)lv23.getY();
                              if ((double)t - this.getY() > (double)p) {
                                 return;
                              }
                           }

                           if (u > 1) {
                              lv8 = lv8.up();
                              BlockState lv26 = this.getWorld().getBlockState(lv8);
                              if (!lv26.getCollisionShape(this.getWorld(), lv8, lv7).isEmpty()) {
                                 return;
                              }
                           }

                           ++u;
                        }
                     }
                  }

                  if (t != Float.MIN_VALUE) {
                     float v = (float)((double)t - this.getY());
                     if (!(v <= 0.5F) && !(v > p)) {
                        this.ticksToNextAutojump = 1;
                     }
                  }
               }
            }
         }
      }
   }

   protected boolean hasCollidedSoftly(Vec3d adjustedMovement) {
      float f = this.getYaw() * 0.017453292F;
      double d = (double)MathHelper.sin(f);
      double e = (double)MathHelper.cos(f);
      double g = (double)this.sidewaysSpeed * e - (double)this.forwardSpeed * d;
      double h = (double)this.forwardSpeed * e + (double)this.sidewaysSpeed * d;
      double i = MathHelper.square(g) + MathHelper.square(h);
      double j = MathHelper.square(adjustedMovement.x) + MathHelper.square(adjustedMovement.z);
      if (!(i < 9.999999747378752E-6) && !(j < 9.999999747378752E-6)) {
         double k = g * adjustedMovement.x + h * adjustedMovement.z;
         double l = Math.acos(k / Math.sqrt(i * j));
         return l < 0.13962633907794952;
      } else {
         return false;
      }
   }

   private boolean shouldAutoJump() {
      return this.isAutoJumpEnabled() && this.ticksToNextAutojump <= 0 && this.isOnGround() && !this.clipAtLedge() && !this.hasVehicle() && this.hasMovementInput() && (double)this.getJumpVelocityMultiplier() >= 1.0;
   }

   private boolean hasMovementInput() {
      Vec2f lv = this.input.getMovementInput();
      return lv.x != 0.0F || lv.y != 0.0F;
   }

   private boolean canStartSprinting() {
      return !this.isSprinting() && this.isWalking() && this.canSprint() && !this.isUsingItem() && !this.hasStatusEffect(StatusEffects.BLINDNESS) && (!this.hasVehicle() || this.canVehicleSprint(this.getVehicle())) && !this.isFallFlying();
   }

   private boolean canVehicleSprint(Entity vehicle) {
      return vehicle.canSprintAsVehicle() && vehicle.isLogicalSideForUpdatingMovement();
   }

   private boolean isWalking() {
      double d = 0.8;
      return this.isSubmergedInWater() ? this.input.hasForwardMovement() : (double)this.input.movementForward >= 0.8;
   }

   private boolean canSprint() {
      return this.hasVehicle() || (float)this.getHungerManager().getFoodLevel() > 6.0F || this.getAbilities().allowFlying;
   }

   public float getUnderwaterVisibility() {
      if (!this.isSubmergedIn(FluidTags.WATER)) {
         return 0.0F;
      } else {
         float f = 600.0F;
         float g = 100.0F;
         if ((float)this.underwaterVisibilityTicks >= 600.0F) {
            return 1.0F;
         } else {
            float h = MathHelper.clamp((float)this.underwaterVisibilityTicks / 100.0F, 0.0F, 1.0F);
            float i = (float)this.underwaterVisibilityTicks < 100.0F ? 0.0F : MathHelper.clamp(((float)this.underwaterVisibilityTicks - 100.0F) / 500.0F, 0.0F, 1.0F);
            return h * 0.6F + i * 0.39999998F;
         }
      }
   }

   public boolean isSubmergedInWater() {
      return this.isSubmergedInWater;
   }

   protected boolean updateWaterSubmersionState() {
      boolean bl = this.isSubmergedInWater;
      boolean bl2 = super.updateWaterSubmersionState();
      if (this.isSpectator()) {
         return this.isSubmergedInWater;
      } else {
         if (!bl && bl2) {
            this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
            this.client.getSoundManager().play(new AmbientSoundLoops.Underwater(this));
         }

         if (bl && !bl2) {
            this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
         }

         return this.isSubmergedInWater;
      }
   }

   public Vec3d getLeashPos(float delta) {
      if (this.client.options.getPerspective().isFirstPerson()) {
         float g = MathHelper.lerp(delta * 0.5F, this.getYaw(), this.prevYaw) * 0.017453292F;
         float h = MathHelper.lerp(delta * 0.5F, this.getPitch(), this.prevPitch) * 0.017453292F;
         double d = this.getMainArm() == Arm.RIGHT ? -1.0 : 1.0;
         Vec3d lv = new Vec3d(0.39 * d, -0.6, 0.3);
         return lv.rotateX(-h).rotateY(-g).add(this.getCameraPosVec(delta));
      } else {
         return super.getLeashPos(delta);
      }
   }

   public void onPickupSlotClick(ItemStack cursorStack, ItemStack slotStack, ClickType clickType) {
      this.client.getTutorialManager().onPickupSlotClick(cursorStack, slotStack, clickType);
   }

   public float getBodyYaw() {
      return this.getYaw();
   }
}
