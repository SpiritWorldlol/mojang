package net.minecraft.util.math;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;

public interface BlockPointer extends Position {
   double getX();

   double getY();

   double getZ();

   BlockPos getPos();

   BlockState getBlockState();

   BlockEntity getBlockEntity();

   ServerWorld getWorld();
}
