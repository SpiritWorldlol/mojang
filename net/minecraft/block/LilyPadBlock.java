package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class LilyPadBlock extends PlantBlock {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 1.5, 15.0);

   protected LilyPadBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      super.onEntityCollision(state, world, pos, entity);
      if (world instanceof ServerWorld && entity instanceof BoatEntity) {
         world.breakBlock(new BlockPos(pos), true, entity);
      }

   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      FluidState lv = world.getFluidState(pos);
      FluidState lv2 = world.getFluidState(pos.up());
      return (lv.getFluid() == Fluids.WATER || floor.getBlock() instanceof IceBlock) && lv2.getFluid() == Fluids.EMPTY;
   }
}
