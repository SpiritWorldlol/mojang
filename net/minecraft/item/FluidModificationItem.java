package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface FluidModificationItem {
   default void onEmptied(@Nullable PlayerEntity player, World world, ItemStack stack, BlockPos pos) {
   }

   boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult);
}
