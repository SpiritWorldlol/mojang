package net.minecraft.block;

import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class CoralWallFanBlock extends DeadCoralWallFanBlock {
   private final Block deadCoralBlock;

   protected CoralWallFanBlock(Block deadCoralBlock, AbstractBlock.Settings settings) {
      super(settings);
      this.deadCoralBlock = deadCoralBlock;
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      this.checkLivingConditions(state, world, pos);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!isInWater(state, world, pos)) {
         world.setBlockState(pos, (BlockState)((BlockState)this.deadCoralBlock.getDefaultState().with(WATERLOGGED, false)).with(FACING, (Direction)state.get(FACING)), Block.NOTIFY_LISTENERS);
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos)) {
         return Blocks.AIR.getDefaultState();
      } else {
         if ((Boolean)state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
         }

         this.checkLivingConditions(state, world, pos);
         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      }
   }
}
