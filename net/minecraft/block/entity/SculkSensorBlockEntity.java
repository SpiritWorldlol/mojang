package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.Vibrations;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SculkSensorBlockEntity extends BlockEntity implements GameEventListener.Holder, Vibrations {
   private static final Logger LOGGER = LogUtils.getLogger();
   private Vibrations.ListenerData listenerData;
   private final Vibrations.VibrationListener listener;
   private final Vibrations.Callback callback;
   private int lastVibrationFrequency;

   protected SculkSensorBlockEntity(BlockEntityType arg, BlockPos arg2, BlockState arg3) {
      super(arg, arg2, arg3);
      this.callback = this.createCallback();
      this.listenerData = new Vibrations.ListenerData();
      this.listener = new Vibrations.VibrationListener(this);
   }

   public SculkSensorBlockEntity(BlockPos pos, BlockState state) {
      this(BlockEntityType.SCULK_SENSOR, pos, state);
   }

   public Vibrations.Callback createCallback() {
      return new VibrationCallback(this.getPos());
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.lastVibrationFrequency = nbt.getInt("last_vibration_frequency");
      if (nbt.contains("listener", NbtElement.COMPOUND_TYPE)) {
         DataResult var10000 = Vibrations.ListenerData.CODEC.parse(new Dynamic(NbtOps.INSTANCE, nbt.getCompound("listener")));
         Logger var10001 = LOGGER;
         Objects.requireNonNull(var10001);
         var10000.resultOrPartial(var10001::error).ifPresent((listener) -> {
            this.listenerData = listener;
         });
      }

   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.putInt("last_vibration_frequency", this.lastVibrationFrequency);
      DataResult var10000 = Vibrations.ListenerData.CODEC.encodeStart(NbtOps.INSTANCE, this.listenerData);
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((listenerNbt) -> {
         nbt.put("listener", listenerNbt);
      });
   }

   public Vibrations.ListenerData getVibrationListenerData() {
      return this.listenerData;
   }

   public Vibrations.Callback getVibrationCallback() {
      return this.callback;
   }

   public int getLastVibrationFrequency() {
      return this.lastVibrationFrequency;
   }

   public void setLastVibrationFrequency(int lastVibrationFrequency) {
      this.lastVibrationFrequency = lastVibrationFrequency;
   }

   public Vibrations.VibrationListener getEventListener() {
      return this.listener;
   }

   // $FF: synthetic method
   public GameEventListener getEventListener() {
      return this.getEventListener();
   }

   protected class VibrationCallback implements Vibrations.Callback {
      public static final int RANGE = 8;
      protected final BlockPos pos;
      private final PositionSource positionSource;

      public VibrationCallback(BlockPos pos) {
         this.pos = pos;
         this.positionSource = new BlockPositionSource(pos);
      }

      public int getRange() {
         return 8;
      }

      public PositionSource getPositionSource() {
         return this.positionSource;
      }

      public boolean triggersAvoidCriterion() {
         return true;
      }

      public boolean accepts(ServerWorld world, BlockPos pos, GameEvent event, @Nullable GameEvent.Emitter emitter) {
         return !pos.equals(this.pos) || event != GameEvent.BLOCK_DESTROY && event != GameEvent.BLOCK_PLACE ? SculkSensorBlock.isInactive(SculkSensorBlockEntity.this.getCachedState()) : false;
      }

      public void accept(ServerWorld world, BlockPos pos, GameEvent event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance) {
         BlockState lv = SculkSensorBlockEntity.this.getCachedState();
         if (SculkSensorBlock.isInactive(lv)) {
            SculkSensorBlockEntity.this.setLastVibrationFrequency(Vibrations.getFrequency(event));
            int i = Vibrations.getSignalStrength(distance, this.getRange());
            Block var10 = lv.getBlock();
            if (var10 instanceof SculkSensorBlock) {
               SculkSensorBlock lv2 = (SculkSensorBlock)var10;
               lv2.setActive(sourceEntity, world, this.pos, lv, i, SculkSensorBlockEntity.this.getLastVibrationFrequency());
            }
         }

      }

      public void onListen() {
         SculkSensorBlockEntity.this.markDirty();
      }

      public boolean requiresTickingChunksAround() {
         return true;
      }
   }
}
