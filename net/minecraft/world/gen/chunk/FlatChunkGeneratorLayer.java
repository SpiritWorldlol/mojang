package net.minecraft.world.gen.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.world.dimension.DimensionType;

public class FlatChunkGeneratorLayer {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.intRange(0, DimensionType.MAX_HEIGHT).fieldOf("height").forGetter(FlatChunkGeneratorLayer::getThickness), Registries.BLOCK.getCodec().fieldOf("block").orElse(Blocks.AIR).forGetter((layer) -> {
         return layer.getBlockState().getBlock();
      })).apply(instance, FlatChunkGeneratorLayer::new);
   });
   private final Block block;
   private final int thickness;

   public FlatChunkGeneratorLayer(int thickness, Block block) {
      this.thickness = thickness;
      this.block = block;
   }

   public int getThickness() {
      return this.thickness;
   }

   public BlockState getBlockState() {
      return this.block.getDefaultState();
   }

   public String toString() {
      String var10000 = this.thickness != 1 ? this.thickness + "*" : "";
      return var10000 + Registries.BLOCK.getId(this.block);
   }
}
