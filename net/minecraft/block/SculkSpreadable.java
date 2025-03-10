package net.minecraft.block;

import java.util.Collection;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public interface SculkSpreadable {
   SculkSpreadable VEIN_ONLY_SPREADER = new SculkSpreadable() {
      public boolean spread(WorldAccess world, BlockPos pos, BlockState state, @Nullable Collection directions, boolean markForPostProcessing) {
         if (directions == null) {
            return ((SculkVeinBlock)Blocks.SCULK_VEIN).getSamePositionOnlyGrower().grow(world.getBlockState(pos), world, pos, markForPostProcessing) > 0L;
         } else if (!directions.isEmpty()) {
            return !state.isAir() && !state.getFluidState().isOf(Fluids.WATER) ? false : SculkVeinBlock.place(world, pos, state, directions);
         } else {
            return SculkSpreadable.super.spread(world, pos, state, directions, markForPostProcessing);
         }
      }

      public int spread(SculkSpreadManager.Cursor cursor, WorldAccess world, BlockPos catalystPos, Random random, SculkSpreadManager spreadManager, boolean shouldConvertToBlock) {
         return cursor.getDecay() > 0 ? cursor.getCharge() : 0;
      }

      public int getDecay(int oldDecay) {
         return Math.max(oldDecay - 1, 0);
      }
   };

   default byte getUpdate() {
      return 1;
   }

   default void spreadAtSamePosition(WorldAccess world, BlockState state, BlockPos pos, Random random) {
   }

   default boolean method_41470(WorldAccess world, BlockPos pos, Random random) {
      return false;
   }

   default boolean spread(WorldAccess world, BlockPos pos, BlockState state, @Nullable Collection directions, boolean markForPostProcessing) {
      return ((MultifaceGrowthBlock)Blocks.SCULK_VEIN).getGrower().grow(state, world, pos, markForPostProcessing) > 0L;
   }

   default boolean shouldConvertToSpreadable() {
      return true;
   }

   default int getDecay(int oldDecay) {
      return 1;
   }

   int spread(SculkSpreadManager.Cursor cursor, WorldAccess world, BlockPos catalystPos, Random random, SculkSpreadManager spreadManager, boolean shouldConvertToBlock);
}
