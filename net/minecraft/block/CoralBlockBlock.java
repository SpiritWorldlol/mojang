package net.minecraft.block;

import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class CoralBlockBlock extends Block {
   private final Block deadCoralBlock;

   public CoralBlockBlock(Block deadCoralBlock, AbstractBlock.Settings settings) {
      super(settings);
      this.deadCoralBlock = deadCoralBlock;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!this.isInWater(world, pos)) {
         world.setBlockState(pos, this.deadCoralBlock.getDefaultState(), Block.NOTIFY_LISTENERS);
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (!this.isInWater(world, pos)) {
         world.scheduleBlockTick(pos, this, 60 + world.getRandom().nextInt(40));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   protected boolean isInWater(BlockView world, BlockPos pos) {
      Direction[] var3 = Direction.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Direction lv = var3[var5];
         FluidState lv2 = world.getFluidState(pos.offset(lv));
         if (lv2.isIn(FluidTags.WATER)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      if (!this.isInWater(ctx.getWorld(), ctx.getBlockPos())) {
         ctx.getWorld().scheduleBlockTick(ctx.getBlockPos(), this, 60 + ctx.getWorld().getRandom().nextInt(40));
      }

      return this.getDefaultState();
   }
}
