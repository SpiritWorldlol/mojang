package net.minecraft.world.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.GameEventTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.event.listener.Vibration;
import net.minecraft.world.event.listener.VibrationSelector;
import org.jetbrains.annotations.Nullable;

public interface Vibrations {
   GameEvent[] RESONATIONS = new GameEvent[]{GameEvent.RESONATE_1, GameEvent.RESONATE_2, GameEvent.RESONATE_3, GameEvent.RESONATE_4, GameEvent.RESONATE_5, GameEvent.RESONATE_6, GameEvent.RESONATE_7, GameEvent.RESONATE_8, GameEvent.RESONATE_9, GameEvent.RESONATE_10, GameEvent.RESONATE_11, GameEvent.RESONATE_12, GameEvent.RESONATE_13, GameEvent.RESONATE_14, GameEvent.RESONATE_15};
   ToIntFunction FREQUENCIES = (ToIntFunction)Util.make(new Object2IntOpenHashMap(), (frequencies) -> {
      frequencies.defaultReturnValue(0);
      frequencies.put(GameEvent.STEP, 1);
      frequencies.put(GameEvent.SWIM, 1);
      frequencies.put(GameEvent.FLAP, 1);
      frequencies.put(GameEvent.PROJECTILE_LAND, 2);
      frequencies.put(GameEvent.HIT_GROUND, 2);
      frequencies.put(GameEvent.SPLASH, 2);
      frequencies.put(GameEvent.ITEM_INTERACT_FINISH, 3);
      frequencies.put(GameEvent.PROJECTILE_SHOOT, 3);
      frequencies.put(GameEvent.INSTRUMENT_PLAY, 3);
      frequencies.put(GameEvent.ENTITY_ROAR, 4);
      frequencies.put(GameEvent.ENTITY_SHAKE, 4);
      frequencies.put(GameEvent.ELYTRA_GLIDE, 4);
      frequencies.put(GameEvent.ENTITY_DISMOUNT, 5);
      frequencies.put(GameEvent.EQUIP, 5);
      frequencies.put(GameEvent.ENTITY_INTERACT, 6);
      frequencies.put(GameEvent.SHEAR, 6);
      frequencies.put(GameEvent.ENTITY_MOUNT, 6);
      frequencies.put(GameEvent.ENTITY_DAMAGE, 7);
      frequencies.put(GameEvent.DRINK, 8);
      frequencies.put(GameEvent.EAT, 8);
      frequencies.put(GameEvent.CONTAINER_CLOSE, 9);
      frequencies.put(GameEvent.BLOCK_CLOSE, 9);
      frequencies.put(GameEvent.BLOCK_DEACTIVATE, 9);
      frequencies.put(GameEvent.BLOCK_DETACH, 9);
      frequencies.put(GameEvent.CONTAINER_OPEN, 10);
      frequencies.put(GameEvent.BLOCK_OPEN, 10);
      frequencies.put(GameEvent.BLOCK_ACTIVATE, 10);
      frequencies.put(GameEvent.BLOCK_ATTACH, 10);
      frequencies.put(GameEvent.PRIME_FUSE, 10);
      frequencies.put(GameEvent.NOTE_BLOCK_PLAY, 10);
      frequencies.put(GameEvent.BLOCK_CHANGE, 11);
      frequencies.put(GameEvent.BLOCK_DESTROY, 12);
      frequencies.put(GameEvent.FLUID_PICKUP, 12);
      frequencies.put(GameEvent.BLOCK_PLACE, 13);
      frequencies.put(GameEvent.FLUID_PLACE, 13);
      frequencies.put(GameEvent.ENTITY_PLACE, 14);
      frequencies.put(GameEvent.LIGHTNING_STRIKE, 14);
      frequencies.put(GameEvent.TELEPORT, 14);
      frequencies.put(GameEvent.ENTITY_DIE, 15);
      frequencies.put(GameEvent.EXPLODE, 15);

      for(int i = 1; i <= 15; ++i) {
         frequencies.put(getResonation(i), i);
      }

   });

   ListenerData getVibrationListenerData();

   Callback getVibrationCallback();

   static int getFrequency(GameEvent event) {
      return FREQUENCIES.applyAsInt(event);
   }

   static GameEvent getResonation(int frequency) {
      return RESONATIONS[frequency - 1];
   }

   static int getSignalStrength(float distance, int range) {
      double d = 15.0 / (double)range;
      return Math.max(1, 15 - MathHelper.floor(d * (double)distance));
   }

   public interface Callback {
      int getRange();

      PositionSource getPositionSource();

      boolean accepts(ServerWorld world, BlockPos pos, GameEvent event, GameEvent.Emitter emitter);

      void accept(ServerWorld world, BlockPos pos, GameEvent event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance);

      default TagKey getTag() {
         return GameEventTags.VIBRATIONS;
      }

      default boolean triggersAvoidCriterion() {
         return false;
      }

      default boolean requiresTickingChunksAround() {
         return false;
      }

      default int getDelay(float distance) {
         return MathHelper.floor(distance);
      }

      default boolean canAccept(GameEvent gameEvent, GameEvent.Emitter emitter) {
         if (!gameEvent.isIn(this.getTag())) {
            return false;
         } else {
            Entity lv = emitter.sourceEntity();
            if (lv != null) {
               if (lv.isSpectator()) {
                  return false;
               }

               if (lv.bypassesSteppingEffects() && gameEvent.isIn(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
                  if (this.triggersAvoidCriterion() && lv instanceof ServerPlayerEntity) {
                     ServerPlayerEntity lv2 = (ServerPlayerEntity)lv;
                     Criteria.AVOID_VIBRATION.trigger(lv2);
                  }

                  return false;
               }

               if (lv.occludeVibrationSignals()) {
                  return false;
               }
            }

            if (emitter.affectedState() != null) {
               return !emitter.affectedState().isIn(BlockTags.DAMPENS_VIBRATIONS);
            } else {
               return true;
            }
         }
      }

      default void onListen() {
      }
   }

   public interface Ticker {
      static void tick(World world, ListenerData listenerData, Callback callback) {
         if (world instanceof ServerWorld lv) {
            if (listenerData.vibration == null) {
               tryListen(lv, listenerData, callback);
            }

            if (listenerData.vibration != null) {
               boolean bl = listenerData.getDelay() > 0;
               spawnVibrationParticle(lv, listenerData, callback);
               listenerData.tickDelay();
               if (listenerData.getDelay() <= 0) {
                  bl = accept(lv, listenerData, callback, listenerData.vibration);
               }

               if (bl) {
                  callback.onListen();
               }

            }
         }
      }

      private static void tryListen(ServerWorld world, ListenerData listenerData, Callback callback) {
         listenerData.getSelector().getVibrationToTick(world.getTime()).ifPresent((vibration) -> {
            listenerData.setVibration(vibration);
            Vec3d lv = vibration.pos();
            listenerData.setDelay(callback.getDelay(vibration.distance()));
            world.spawnParticles(new VibrationParticleEffect(callback.getPositionSource(), listenerData.getDelay()), lv.x, lv.y, lv.z, 1, 0.0, 0.0, 0.0, 0.0);
            callback.onListen();
            listenerData.getSelector().clear();
         });
      }

      private static void spawnVibrationParticle(ServerWorld world, ListenerData listenerData, Callback callback) {
         if (listenerData.shouldSpawnParticle()) {
            if (listenerData.vibration == null) {
               listenerData.setSpawnParticle(false);
            } else {
               Vec3d lv = listenerData.vibration.pos();
               PositionSource lv2 = callback.getPositionSource();
               Vec3d lv3 = (Vec3d)lv2.getPos(world).orElse(lv);
               int i = listenerData.getDelay();
               int j = callback.getDelay(listenerData.vibration.distance());
               double d = 1.0 - (double)i / (double)j;
               double e = MathHelper.lerp(d, lv.x, lv3.x);
               double f = MathHelper.lerp(d, lv.y, lv3.y);
               double g = MathHelper.lerp(d, lv.z, lv3.z);
               boolean bl = world.spawnParticles(new VibrationParticleEffect(lv2, i), e, f, g, 1, 0.0, 0.0, 0.0, 0.0) > 0;
               if (bl) {
                  listenerData.setSpawnParticle(false);
               }

            }
         }
      }

      private static boolean accept(ServerWorld world, ListenerData listenerData, Callback callback, Vibration vibration) {
         BlockPos lv = BlockPos.ofFloored(vibration.pos());
         BlockPos lv2 = (BlockPos)callback.getPositionSource().getPos(world).map(BlockPos::ofFloored).orElse(lv);
         if (callback.requiresTickingChunksAround() && !areChunksTickingAround(world, lv2)) {
            return false;
         } else {
            callback.accept(world, lv, vibration.gameEvent(), (Entity)vibration.getEntity(world).orElse((Object)null), (Entity)vibration.getOwner(world).orElse((Object)null), Vibrations.VibrationListener.getTravelDelay(lv, lv2));
            listenerData.setVibration((Vibration)null);
            return true;
         }
      }

      private static boolean areChunksTickingAround(World world, BlockPos pos) {
         ChunkPos lv = new ChunkPos(pos);

         for(int i = lv.x - 1; i < lv.x + 1; ++i) {
            for(int j = lv.z - 1; j < lv.z + 1; ++j) {
               Chunk lv2 = world.getChunkManager().getWorldChunk(i, j);
               if (lv2 == null || !world.shouldTickBlocksInChunk(lv2.getPos().toLong())) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public static class VibrationListener implements GameEventListener {
      private final Vibrations receiver;

      public VibrationListener(Vibrations receiver) {
         this.receiver = receiver;
      }

      public PositionSource getPositionSource() {
         return this.receiver.getVibrationCallback().getPositionSource();
      }

      public int getRange() {
         return this.receiver.getVibrationCallback().getRange();
      }

      public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
         ListenerData lv = this.receiver.getVibrationListenerData();
         Callback lv2 = this.receiver.getVibrationCallback();
         if (lv.getVibration() != null) {
            return false;
         } else if (!lv2.canAccept(event, emitter)) {
            return false;
         } else {
            Optional optional = lv2.getPositionSource().getPos(world);
            if (optional.isEmpty()) {
               return false;
            } else {
               Vec3d lv3 = (Vec3d)optional.get();
               if (!lv2.accepts(world, BlockPos.ofFloored(emitterPos), event, emitter)) {
                  return false;
               } else if (isOccluded(world, emitterPos, lv3)) {
                  return false;
               } else {
                  this.listen(world, lv, event, emitter, emitterPos, lv3);
                  return true;
               }
            }
         }
      }

      public void forceListen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
         this.receiver.getVibrationCallback().getPositionSource().getPos(world).ifPresent((pos) -> {
            this.listen(world, this.receiver.getVibrationListenerData(), event, emitter, emitterPos, pos);
         });
      }

      private void listen(ServerWorld world, ListenerData listenerData, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos, Vec3d listenerPos) {
         listenerData.vibrationSelector.tryAccept(new Vibration(event, (float)emitterPos.distanceTo(listenerPos), emitterPos, emitter.sourceEntity()), world.getTime());
      }

      public static float getTravelDelay(BlockPos emitterPos, BlockPos listenerPos) {
         return (float)Math.sqrt(emitterPos.getSquaredDistance(listenerPos));
      }

      private static boolean isOccluded(World world, Vec3d emitterPos, Vec3d listenerPos) {
         Vec3d lv = new Vec3d((double)MathHelper.floor(emitterPos.x) + 0.5, (double)MathHelper.floor(emitterPos.y) + 0.5, (double)MathHelper.floor(emitterPos.z) + 0.5);
         Vec3d lv2 = new Vec3d((double)MathHelper.floor(listenerPos.x) + 0.5, (double)MathHelper.floor(listenerPos.y) + 0.5, (double)MathHelper.floor(listenerPos.z) + 0.5);
         Direction[] var5 = Direction.values();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Direction lv3 = var5[var7];
            Vec3d lv4 = lv.offset(lv3, 9.999999747378752E-6);
            if (world.raycast(new BlockStateRaycastContext(lv4, lv2, (state) -> {
               return state.isIn(BlockTags.OCCLUDES_VIBRATION_SIGNALS);
            })).getType() != HitResult.Type.BLOCK) {
               return false;
            }
         }

         return true;
      }
   }

   public static final class ListenerData {
      public static Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Vibration.CODEC.optionalFieldOf("event").forGetter((listenerData) -> {
            return Optional.ofNullable(listenerData.vibration);
         }), VibrationSelector.CODEC.fieldOf("selector").forGetter(ListenerData::getSelector), Codecs.NONNEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(ListenerData::getDelay)).apply(instance, (vibration, selector, delay) -> {
            return new ListenerData((Vibration)vibration.orElse((Object)null), selector, delay, true);
         });
      });
      public static final String LISTENER_NBT_KEY = "listener";
      @Nullable
      Vibration vibration;
      private int delay;
      final VibrationSelector vibrationSelector;
      private boolean spawnParticle;

      private ListenerData(@Nullable Vibration vibration, VibrationSelector vibrationSelector, int delay, boolean spawnParticle) {
         this.vibration = vibration;
         this.delay = delay;
         this.vibrationSelector = vibrationSelector;
         this.spawnParticle = spawnParticle;
      }

      public ListenerData() {
         this((Vibration)null, new VibrationSelector(), 0, false);
      }

      public VibrationSelector getSelector() {
         return this.vibrationSelector;
      }

      @Nullable
      public Vibration getVibration() {
         return this.vibration;
      }

      public void setVibration(@Nullable Vibration vibration) {
         this.vibration = vibration;
      }

      public int getDelay() {
         return this.delay;
      }

      public void setDelay(int delay) {
         this.delay = delay;
      }

      public void tickDelay() {
         this.delay = Math.max(0, this.delay - 1);
      }

      public boolean shouldSpawnParticle() {
         return this.spawnParticle;
      }

      public void setSpawnParticle(boolean spawnParticle) {
         this.spawnParticle = spawnParticle;
      }
   }
}
