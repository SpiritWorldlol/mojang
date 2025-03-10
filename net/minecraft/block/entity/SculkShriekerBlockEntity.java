package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Objects;
import java.util.OptionalInt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SculkShriekerBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LargeEntitySpawnHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.tag.GameEventTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.Vibrations;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SculkShriekerBlockEntity extends BlockEntity implements GameEventListener.Holder, Vibrations {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_38750 = 10;
   private static final int WARDEN_SPAWN_TRIES = 20;
   private static final int WARDEN_SPAWN_HORIZONTAL_RANGE = 5;
   private static final int WARDEN_SPAWN_VERTICAL_RANGE = 6;
   private static final int DARKNESS_RANGE = 40;
   private static final int SHRIEK_DELAY = 90;
   private static final Int2ObjectMap WARNING_SOUNDS = (Int2ObjectMap)Util.make(new Int2ObjectOpenHashMap(), (warningSounds) -> {
      warningSounds.put(1, SoundEvents.ENTITY_WARDEN_NEARBY_CLOSE);
      warningSounds.put(2, SoundEvents.ENTITY_WARDEN_NEARBY_CLOSER);
      warningSounds.put(3, SoundEvents.ENTITY_WARDEN_NEARBY_CLOSEST);
      warningSounds.put(4, SoundEvents.ENTITY_WARDEN_LISTENING_ANGRY);
   });
   private int warningLevel;
   private final Vibrations.Callback vibrationCallback = new VibrationCallback();
   private Vibrations.ListenerData vibrationListenerData = new Vibrations.ListenerData();
   private final Vibrations.VibrationListener vibrationListener = new Vibrations.VibrationListener(this);

   public SculkShriekerBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.SCULK_SHRIEKER, pos, state);
   }

   public Vibrations.ListenerData getVibrationListenerData() {
      return this.vibrationListenerData;
   }

   public Vibrations.Callback getVibrationCallback() {
      return this.vibrationCallback;
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("warning_level", NbtElement.NUMBER_TYPE)) {
         this.warningLevel = nbt.getInt("warning_level");
      }

      if (nbt.contains("listener", NbtElement.COMPOUND_TYPE)) {
         DataResult var10000 = Vibrations.ListenerData.CODEC.parse(new Dynamic(NbtOps.INSTANCE, nbt.getCompound("listener")));
         Logger var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         var10000.resultOrPartial(var10001::error).ifPresent((vibrationListener) -> {
            this.vibrationListenerData = vibrationListener;
         });
      }

   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.putInt("warning_level", this.warningLevel);
      DataResult var10000 = Vibrations.ListenerData.CODEC.encodeStart(NbtOps.INSTANCE, this.vibrationListenerData);
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((arg2) -> {
         nbt.put("listener", arg2);
      });
   }

   @Nullable
   public static ServerPlayerEntity findResponsiblePlayerFromEntity(@Nullable Entity entity) {
      if (entity instanceof ServerPlayerEntity lv) {
         return lv;
      } else {
         if (entity != null) {
            LivingEntity var2 = entity.getControllingPassenger();
            if (var2 instanceof ServerPlayerEntity) {
               lv = (ServerPlayerEntity)var2;
               return lv;
            }
         }

         Entity var3;
         if (entity instanceof ProjectileEntity lv2) {
            var3 = lv2.getOwner();
            if (var3 instanceof ServerPlayerEntity lv3) {
               return lv3;
            }
         }

         if (entity instanceof ItemEntity lv4) {
            var3 = lv4.getOwner();
            if (var3 instanceof ServerPlayerEntity lv3) {
               return lv3;
            }
         }

         return null;
      }
   }

   public void shriek(ServerWorld world, @Nullable ServerPlayerEntity player) {
      if (player != null) {
         BlockState lv = this.getCachedState();
         if (!(Boolean)lv.get(SculkShriekerBlock.SHRIEKING)) {
            this.warningLevel = 0;
            if (!this.canWarn(world) || this.trySyncWarningLevel(world, player)) {
               this.shriek(world, (Entity)player);
            }
         }
      }
   }

   private boolean trySyncWarningLevel(ServerWorld world, ServerPlayerEntity player) {
      OptionalInt optionalInt = SculkShriekerWarningManager.warnNearbyPlayers(world, this.getPos(), player);
      optionalInt.ifPresent((warningLevel) -> {
         this.warningLevel = warningLevel;
      });
      return optionalInt.isPresent();
   }

   private void shriek(ServerWorld world, @Nullable Entity entity) {
      BlockPos lv = this.getPos();
      BlockState lv2 = this.getCachedState();
      world.setBlockState(lv, (BlockState)lv2.with(SculkShriekerBlock.SHRIEKING, true), Block.NOTIFY_LISTENERS);
      world.scheduleBlockTick(lv, lv2.getBlock(), 90);
      world.syncWorldEvent(WorldEvents.SCULK_SHRIEKS, lv, 0);
      world.emitGameEvent(GameEvent.SHRIEK, lv, GameEvent.Emitter.of(entity));
   }

   private boolean canWarn(ServerWorld world) {
      return (Boolean)this.getCachedState().get(SculkShriekerBlock.CAN_SUMMON) && world.getDifficulty() != Difficulty.PEACEFUL && world.getGameRules().getBoolean(GameRules.DO_WARDEN_SPAWNING);
   }

   public void warn(ServerWorld world) {
      if (this.canWarn(world) && this.warningLevel > 0) {
         if (!this.trySpawnWarden(world)) {
            this.playWarningSound(world);
         }

         WardenEntity.addDarknessToClosePlayers(world, Vec3d.ofCenter(this.getPos()), (Entity)null, 40);
      }

   }

   private void playWarningSound(World world) {
      SoundEvent lv = (SoundEvent)WARNING_SOUNDS.get(this.warningLevel);
      if (lv != null) {
         BlockPos lv2 = this.getPos();
         int i = lv2.getX() + MathHelper.nextBetween(world.random, -10, 10);
         int j = lv2.getY() + MathHelper.nextBetween(world.random, -10, 10);
         int k = lv2.getZ() + MathHelper.nextBetween(world.random, -10, 10);
         world.playSound((PlayerEntity)null, (double)i, (double)j, (double)k, lv, SoundCategory.HOSTILE, 5.0F, 1.0F);
      }

   }

   private boolean trySpawnWarden(ServerWorld world) {
      return this.warningLevel < 4 ? false : LargeEntitySpawnHelper.trySpawnAt(EntityType.WARDEN, SpawnReason.TRIGGERED, world, this.getPos(), 20, 5, 6, LargeEntitySpawnHelper.Requirements.WARDEN).isPresent();
   }

   public Vibrations.VibrationListener getEventListener() {
      return this.vibrationListener;
   }

   // $FF: synthetic method
   public GameEventListener getEventListener() {
      return this.getEventListener();
   }

   private class VibrationCallback implements Vibrations.Callback {
      private static final int RANGE = 8;
      private final PositionSource positionSource;

      public VibrationCallback() {
         this.positionSource = new BlockPositionSource(SculkShriekerBlockEntity.this.pos);
      }

      public int getRange() {
         return 8;
      }

      public PositionSource getPositionSource() {
         return this.positionSource;
      }

      public TagKey getTag() {
         return GameEventTags.SHRIEKER_CAN_LISTEN;
      }

      public boolean accepts(ServerWorld world, BlockPos pos, GameEvent event, GameEvent.Emitter emitter) {
         return !(Boolean)SculkShriekerBlockEntity.this.getCachedState().get(SculkShriekerBlock.SHRIEKING) && SculkShriekerBlockEntity.findResponsiblePlayerFromEntity(emitter.sourceEntity()) != null;
      }

      public void accept(ServerWorld world, BlockPos pos, GameEvent event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance) {
         SculkShriekerBlockEntity.this.shriek(world, SculkShriekerBlockEntity.findResponsiblePlayerFromEntity(entity != null ? entity : sourceEntity));
      }

      public void onListen() {
         SculkShriekerBlockEntity.this.markDirty();
      }

      public boolean requiresTickingChunksAround() {
         return true;
      }
   }
}
