package net.minecraft.block;

import java.util.Optional;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class AbstractPlantBlock extends AbstractPlantPartBlock implements Fertilizable {
   protected AbstractPlantBlock(AbstractBlock.Settings arg, Direction arg2, VoxelShape arg3, boolean bl) {
      super(arg, arg2, arg3, bl);
   }

   protected BlockState copyState(BlockState from, BlockState to) {
      return to;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (direction == this.growthDirection.getOpposite() && !state.canPlaceAt(world, pos)) {
         world.scheduleBlockTick(pos, this, 1);
      }

      AbstractPlantStemBlock lv = this.getStem();
      if (direction == this.growthDirection && !neighborState.isOf(this) && !neighborState.isOf(lv)) {
         return this.copyState(state, lv.getRandomGrowthState(world));
      } else {
         if (this.tickWater) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
         }

         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      }
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(this.getStem());
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      Optional optional = this.getStemHeadPos(world, pos, state.getBlock());
      return optional.isPresent() && this.getStem().chooseStemState(world.getBlockState(((BlockPos)optional.get()).offset(this.growthDirection)));
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      Optional optional = this.getStemHeadPos(world, pos, state.getBlock());
      if (optional.isPresent()) {
         BlockState lv = world.getBlockState((BlockPos)optional.get());
         ((AbstractPlantStemBlock)lv.getBlock()).grow(world, random, (BlockPos)optional.get(), lv);
      }

   }

   private Optional getStemHeadPos(BlockView world, BlockPos pos, Block block) {
      return BlockLocating.findColumnEnd(world, pos, block, this.growthDirection, this.getStem());
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      boolean bl = super.canReplace(state, context);
      return bl && context.getStack().isOf(this.getStem().asItem()) ? false : bl;
   }

   protected Block getPlant() {
      return this;
   }
}
