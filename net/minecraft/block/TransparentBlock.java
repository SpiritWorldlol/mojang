package net.minecraft.block;

import net.minecraft.util.math.Direction;

public class TransparentBlock extends Block {
   protected TransparentBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
      return stateFrom.isOf(this) ? true : super.isSideInvisible(state, stateFrom, direction);
   }
}
