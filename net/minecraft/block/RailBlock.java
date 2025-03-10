package net.minecraft.block;

import net.minecraft.block.enums.RailShape;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RailBlock extends AbstractRailBlock {
   public static final EnumProperty SHAPE;

   protected RailBlock(AbstractBlock.Settings arg) {
      super(false, arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(SHAPE, RailShape.NORTH_SOUTH)).with(WATERLOGGED, false));
   }

   protected void updateBlockState(BlockState state, World world, BlockPos pos, Block neighbor) {
      if (neighbor.getDefaultState().emitsRedstonePower() && (new RailPlacementHelper(world, pos, state)).getNeighborCount() == 3) {
         this.updateBlockState(world, pos, state, false);
      }

   }

   public Property getShapeProperty() {
      return SHAPE;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      switch (rotation) {
         case CLOCKWISE_180:
            switch ((RailShape)state.get(SHAPE)) {
               case ASCENDING_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_NORTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
               case SOUTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
               case SOUTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
               case NORTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
            }
         case COUNTERCLOCKWISE_90:
            switch ((RailShape)state.get(SHAPE)) {
               case ASCENDING_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
               case ASCENDING_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_NORTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
               case SOUTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
               case SOUTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
               case NORTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
               case NORTH_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_SOUTH);
            }
         case CLOCKWISE_90:
            switch ((RailShape)state.get(SHAPE)) {
               case ASCENDING_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
               case ASCENDING_NORTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
               case SOUTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
               case SOUTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
               case NORTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
               case NORTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_SOUTH);
            }
         default:
            return state;
      }
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      RailShape lv = (RailShape)state.get(SHAPE);
      switch (mirror) {
         case LEFT_RIGHT:
            switch (lv) {
               case ASCENDING_NORTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_SOUTH:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
               case SOUTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
               case SOUTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
               case NORTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
               case NORTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
               default:
                  return super.mirror(state, mirror);
            }
         case FRONT_BACK:
            switch (lv) {
               case ASCENDING_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_NORTH:
               case ASCENDING_SOUTH:
               default:
                  break;
               case SOUTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
               case SOUTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_WEST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
               case NORTH_EAST:
                  return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
            }
      }

      return super.mirror(state, mirror);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(SHAPE, WATERLOGGED);
   }

   static {
      SHAPE = Properties.RAIL_SHAPE;
   }
}
