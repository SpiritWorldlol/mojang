package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CobwebBlock extends Block {
   public CobwebBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      entity.slowMovement(state, new Vec3d(0.25, 0.05000000074505806, 0.25));
   }
}
