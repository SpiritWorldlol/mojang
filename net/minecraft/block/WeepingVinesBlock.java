package net.minecraft.block;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;

public class WeepingVinesBlock extends AbstractPlantStemBlock {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(4.0, 9.0, 4.0, 12.0, 16.0, 12.0);

   public WeepingVinesBlock(AbstractBlock.Settings arg) {
      super(arg, Direction.DOWN, SHAPE, false, 0.1);
   }

   protected int getGrowthLength(Random random) {
      return VineLogic.getGrowthLength(random);
   }

   protected Block getPlant() {
      return Blocks.WEEPING_VINES_PLANT;
   }

   protected boolean chooseStemState(BlockState state) {
      return VineLogic.isValidForWeepingStem(state);
   }
}
