package net.minecraft.world.chunk.light;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.ChunkProvider;

public final class ChunkBlockLightProvider extends ChunkLightProvider {
   private final BlockPos.Mutable mutablePos;

   public ChunkBlockLightProvider(ChunkProvider chunkProvider) {
      this(chunkProvider, new BlockLightStorage(chunkProvider));
   }

   @VisibleForTesting
   public ChunkBlockLightProvider(ChunkProvider chunkProvider, BlockLightStorage blockLightStorage) {
      super(chunkProvider, blockLightStorage);
      this.mutablePos = new BlockPos.Mutable();
   }

   protected void method_51529(long l) {
      long m = ChunkSectionPos.fromBlockPos(l);
      if (((BlockLightStorage)this.lightStorage).hasSection(m)) {
         BlockState lv = this.getStateForLighting(this.mutablePos.set(l));
         int i = this.getLightSourceLuminance(l, lv);
         int j = ((BlockLightStorage)this.lightStorage).get(l);
         if (i < j) {
            ((BlockLightStorage)this.lightStorage).set(l, 0);
            this.method_51565(l, ChunkLightProvider.class_8531.packWithAllDirectionsSet(j));
         } else {
            this.method_51565(l, field_44731);
         }

         if (i > 0) {
            this.method_51566(l, ChunkLightProvider.class_8531.method_51573(i, isTrivialForLighting(lv)));
         }

      }
   }

   protected void method_51531(long l, long m, int i) {
      BlockState lv = null;
      Direction[] var7 = DIRECTIONS;
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         Direction lv2 = var7[var9];
         if (ChunkLightProvider.class_8531.isDirectionBitSet(m, lv2)) {
            long n = BlockPos.offset(l, lv2);
            if (((BlockLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n))) {
               int j = ((BlockLightStorage)this.lightStorage).get(n);
               int k = i - 1;
               if (k > j) {
                  this.mutablePos.set(n);
                  BlockState lv3 = this.getStateForLighting(this.mutablePos);
                  int o = i - this.getOpacity(lv3, this.mutablePos);
                  if (o > j) {
                     if (lv == null) {
                        lv = ChunkLightProvider.class_8531.isTrivial(m) ? Blocks.AIR.getDefaultState() : this.getStateForLighting(this.mutablePos.set(l));
                     }

                     if (!this.shapesCoverFullCube(l, lv, n, lv3, lv2)) {
                        ((BlockLightStorage)this.lightStorage).set(n, o);
                        if (o > 1) {
                           this.method_51566(n, ChunkLightProvider.class_8531.method_51574(o, isTrivialForLighting(lv3), lv2.getOpposite()));
                        }
                     }
                  }
               }
            }
         }
      }

   }

   protected void method_51530(long l, long m) {
      int i = ChunkLightProvider.class_8531.getLightLevel(m);
      Direction[] var6 = DIRECTIONS;
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Direction lv = var6[var8];
         if (ChunkLightProvider.class_8531.isDirectionBitSet(m, lv)) {
            long n = BlockPos.offset(l, lv);
            if (((BlockLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n))) {
               int j = ((BlockLightStorage)this.lightStorage).get(n);
               if (j != 0) {
                  if (j <= i - 1) {
                     BlockState lv2 = this.getStateForLighting(this.mutablePos.set(n));
                     int k = this.getLightSourceLuminance(n, lv2);
                     ((BlockLightStorage)this.lightStorage).set(n, 0);
                     if (k < j) {
                        this.method_51565(n, ChunkLightProvider.class_8531.packWithOneDirectionCleared(j, lv.getOpposite()));
                     }

                     if (k > 0) {
                        this.method_51566(n, ChunkLightProvider.class_8531.method_51573(k, isTrivialForLighting(lv2)));
                     }
                  } else {
                     this.method_51566(n, ChunkLightProvider.class_8531.method_51579(j, false, lv.getOpposite()));
                  }
               }
            }
         }
      }

   }

   private int getLightSourceLuminance(long blockPos, BlockState blockState) {
      int i = blockState.getLuminance();
      return i > 0 && ((BlockLightStorage)this.lightStorage).isSectionInEnabledColumn(ChunkSectionPos.fromBlockPos(blockPos)) ? i : 0;
   }

   public void propagateLight(ChunkPos chunkPos) {
      this.setColumnEnabled(chunkPos, true);
      LightSourceView lv = this.chunkProvider.getChunk(chunkPos.x, chunkPos.z);
      if (lv != null) {
         lv.forEachLightSource((blockPos, blockState) -> {
            int i = blockState.getLuminance();
            this.method_51566(blockPos.asLong(), ChunkLightProvider.class_8531.method_51573(i, isTrivialForLighting(blockState)));
         });
      }

   }
}
