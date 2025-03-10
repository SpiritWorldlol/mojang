package net.minecraft.client.color.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface BlockColorProvider {
   int getColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex);
}
