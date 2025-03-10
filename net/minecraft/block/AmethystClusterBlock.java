package net.minecraft.block;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class AmethystClusterBlock extends AmethystBlock implements Waterloggable {
   public static final BooleanProperty WATERLOGGED;
   public static final DirectionProperty FACING;
   protected final VoxelShape northShape;
   protected final VoxelShape southShape;
   protected final VoxelShape eastShape;
   protected final VoxelShape westShape;
   protected final VoxelShape upShape;
   protected final VoxelShape downShape;

   public AmethystClusterBlock(int height, int xzOffset, AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, false)).with(FACING, Direction.UP));
      this.upShape = Block.createCuboidShape((double)xzOffset, 0.0, (double)xzOffset, (double)(16 - xzOffset), (double)height, (double)(16 - xzOffset));
      this.downShape = Block.createCuboidShape((double)xzOffset, (double)(16 - height), (double)xzOffset, (double)(16 - xzOffset), 16.0, (double)(16 - xzOffset));
      this.northShape = Block.createCuboidShape((double)xzOffset, (double)xzOffset, (double)(16 - height), (double)(16 - xzOffset), (double)(16 - xzOffset), 16.0);
      this.southShape = Block.createCuboidShape((double)xzOffset, (double)xzOffset, 0.0, (double)(16 - xzOffset), (double)(16 - xzOffset), (double)height);
      this.eastShape = Block.createCuboidShape(0.0, (double)xzOffset, (double)xzOffset, (double)height, (double)(16 - xzOffset), (double)(16 - xzOffset));
      this.westShape = Block.createCuboidShape((double)(16 - height), (double)xzOffset, (double)xzOffset, 16.0, (double)(16 - xzOffset), (double)(16 - xzOffset));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Direction lv = (Direction)state.get(FACING);
      switch (lv) {
         case NORTH:
            return this.northShape;
         case SOUTH:
            return this.southShape;
         case EAST:
            return this.eastShape;
         case WEST:
            return this.westShape;
         case DOWN:
            return this.downShape;
         case UP:
         default:
            return this.upShape;
      }
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      Direction lv = (Direction)state.get(FACING);
      BlockPos lv2 = pos.offset(lv.getOpposite());
      return world.getBlockState(lv2).isSideSolidFullSquare(world, lv2, lv);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return direction == ((Direction)state.get(FACING)).getOpposite() && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      WorldAccess lv = ctx.getWorld();
      BlockPos lv2 = ctx.getBlockPos();
      return (BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, lv.getFluidState(lv2).getFluid() == Fluids.WATER)).with(FACING, ctx.getSide());
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(WATERLOGGED, FACING);
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
      FACING = Properties.FACING;
   }
}
