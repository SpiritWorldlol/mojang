package net.minecraft.block;

import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class SproutsBlock extends PlantBlock {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0);

   public SproutsBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return floor.isIn(BlockTags.NYLIUM) || floor.isOf(Blocks.SOUL_SOIL) || super.canPlantOnTop(floor, world, pos);
   }
}
