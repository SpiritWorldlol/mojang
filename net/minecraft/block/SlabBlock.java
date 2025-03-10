package net.minecraft.block;

import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class SlabBlock extends Block implements Waterloggable {
   public static final EnumProperty TYPE;
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape BOTTOM_SHAPE;
   protected static final VoxelShape TOP_SHAPE;

   public SlabBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.getDefaultState().with(TYPE, SlabType.BOTTOM)).with(WATERLOGGED, false));
   }

   public boolean hasSidedTransparency(BlockState state) {
      return state.get(TYPE) != SlabType.DOUBLE;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(TYPE, WATERLOGGED);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      SlabType lv = (SlabType)state.get(TYPE);
      switch (lv) {
         case DOUBLE:
            return VoxelShapes.fullCube();
         case TOP:
            return TOP_SHAPE;
         default:
            return BOTTOM_SHAPE;
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockPos lv = ctx.getBlockPos();
      BlockState lv2 = ctx.getWorld().getBlockState(lv);
      if (lv2.isOf(this)) {
         return (BlockState)((BlockState)lv2.with(TYPE, SlabType.DOUBLE)).with(WATERLOGGED, false);
      } else {
         FluidState lv3 = ctx.getWorld().getFluidState(lv);
         BlockState lv4 = (BlockState)((BlockState)this.getDefaultState().with(TYPE, SlabType.BOTTOM)).with(WATERLOGGED, lv3.getFluid() == Fluids.WATER);
         Direction lv5 = ctx.getSide();
         return lv5 != Direction.DOWN && (lv5 == Direction.UP || !(ctx.getHitPos().y - (double)lv.getY() > 0.5)) ? lv4 : (BlockState)lv4.with(TYPE, SlabType.TOP);
      }
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      ItemStack lv = context.getStack();
      SlabType lv2 = (SlabType)state.get(TYPE);
      if (lv2 != SlabType.DOUBLE && lv.isOf(this.asItem())) {
         if (context.canReplaceExisting()) {
            boolean bl = context.getHitPos().y - (double)context.getBlockPos().getY() > 0.5;
            Direction lv3 = context.getSide();
            if (lv2 == SlabType.BOTTOM) {
               return lv3 == Direction.UP || bl && lv3.getAxis().isHorizontal();
            } else {
               return lv3 == Direction.DOWN || !bl && lv3.getAxis().isHorizontal();
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
      return state.get(TYPE) != SlabType.DOUBLE ? Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState) : false;
   }

   public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
      return state.get(TYPE) != SlabType.DOUBLE ? Waterloggable.super.canFillWithFluid(world, pos, state, fluid) : false;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      switch (type) {
         case LAND:
            return false;
         case WATER:
            return world.getFluidState(pos).isIn(FluidTags.WATER);
         case AIR:
            return false;
         default:
            return false;
      }
   }

   static {
      TYPE = Properties.SLAB_TYPE;
      WATERLOGGED = Properties.WATERLOGGED;
      BOTTOM_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
      TOP_SHAPE = Block.createCuboidShape(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
   }
}
