package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.CalibratedSculkSensorBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.Vibrations;
import org.jetbrains.annotations.Nullable;

public class CalibratedSculkSensorBlockEntity extends SculkSensorBlockEntity {
   public CalibratedSculkSensorBlockEntity(BlockPos arg, BlockState arg2) {
      super(BlockEntityType.CALIBRATED_SCULK_SENSOR, arg, arg2);
   }

   public Vibrations.Callback createCallback() {
      return new Callback(this.getPos());
   }

   protected class Callback extends SculkSensorBlockEntity.VibrationCallback {
      public Callback(BlockPos pos) {
         super(pos);
      }

      public int getRange() {
         return 16;
      }

      public boolean accepts(ServerWorld world, BlockPos pos, GameEvent event, @Nullable GameEvent.Emitter emitter) {
         int i = this.getCalibrationFrequency(world, this.pos, CalibratedSculkSensorBlockEntity.this.getCachedState());
         return i != 0 && Vibrations.getFrequency(event) != i ? false : super.accepts(world, pos, event, emitter);
      }

      private int getCalibrationFrequency(World world, BlockPos pos, BlockState state) {
         Direction lv = ((Direction)state.get(CalibratedSculkSensorBlock.FACING)).getOpposite();
         return world.getEmittedRedstonePower(pos.offset(lv), lv);
      }
   }
}
