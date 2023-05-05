package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;

public class SkyLightStorage extends LightStorage {
   protected SkyLightStorage(ChunkProvider chunkProvider) {
      super(LightType.SKY, chunkProvider, new Data(new Long2ObjectOpenHashMap(), new Long2IntOpenHashMap(), Integer.MAX_VALUE));
   }

   protected int getLight(long blockPos) {
      return this.getLight(blockPos, false);
   }

   protected int getLight(long blockPos, boolean cached) {
      long m = ChunkSectionPos.fromBlockPos(blockPos);
      int i = ChunkSectionPos.unpackY(m);
      Data lv = cached ? (Data)this.storage : (Data)this.uncachedStorage;
      int j = lv.columnToTopSection.get(ChunkSectionPos.withZeroY(m));
      if (j != lv.minSectionY && i < j) {
         ChunkNibbleArray lv2 = this.getLightSection(lv, m);
         if (lv2 == null) {
            for(blockPos = BlockPos.removeChunkSectionLocalY(blockPos); lv2 == null; lv2 = this.getLightSection(lv, m)) {
               ++i;
               if (i >= j) {
                  return 15;
               }

               m = ChunkSectionPos.offset(m, Direction.UP);
            }
         }

         return lv2.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
      } else {
         return cached && !this.isSectionInEnabledColumn(m) ? 0 : 15;
      }
   }

   protected void onLoadSection(long sectionPos) {
      int i = ChunkSectionPos.unpackY(sectionPos);
      if (((Data)this.storage).minSectionY > i) {
         ((Data)this.storage).minSectionY = i;
         ((Data)this.storage).columnToTopSection.defaultReturnValue(((Data)this.storage).minSectionY);
      }

      long m = ChunkSectionPos.withZeroY(sectionPos);
      int j = ((Data)this.storage).columnToTopSection.get(m);
      if (j < i + 1) {
         ((Data)this.storage).columnToTopSection.put(m, i + 1);
      }

   }

   protected void onUnloadSection(long sectionPos) {
      long m = ChunkSectionPos.withZeroY(sectionPos);
      int i = ChunkSectionPos.unpackY(sectionPos);
      if (((Data)this.storage).columnToTopSection.get(m) == i + 1) {
         long n;
         for(n = sectionPos; !this.hasSection(n) && this.isAboveMinHeight(i); n = ChunkSectionPos.offset(n, Direction.DOWN)) {
            --i;
         }

         if (this.hasSection(n)) {
            ((Data)this.storage).columnToTopSection.put(m, i + 1);
         } else {
            ((Data)this.storage).columnToTopSection.remove(m);
         }
      }

   }

   protected ChunkNibbleArray createSection(long sectionPos) {
      ChunkNibbleArray lv = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
      if (lv != null) {
         return lv;
      } else {
         int i = ((Data)this.storage).columnToTopSection.get(ChunkSectionPos.withZeroY(sectionPos));
         if (i != ((Data)this.storage).minSectionY && ChunkSectionPos.unpackY(sectionPos) < i) {
            ChunkNibbleArray lv2;
            for(long m = ChunkSectionPos.offset(sectionPos, Direction.UP); (lv2 = this.getLightSection(m, true)) == null; m = ChunkSectionPos.offset(m, Direction.UP)) {
            }

            return copy(lv2);
         } else {
            return this.isSectionInEnabledColumn(sectionPos) ? new ChunkNibbleArray(15) : new ChunkNibbleArray();
         }
      }
   }

   private static ChunkNibbleArray copy(ChunkNibbleArray source) {
      if (source.isArrayUninitialized()) {
         return source.copy();
      } else {
         byte[] bs = source.asByteArray();
         byte[] cs = new byte[2048];

         for(int i = 0; i < 16; ++i) {
            System.arraycopy(bs, 0, cs, i * 128, 128);
         }

         return new ChunkNibbleArray(cs);
      }
   }

   protected boolean isAboveMinHeight(int sectionY) {
      return sectionY >= ((Data)this.storage).minSectionY;
   }

   protected boolean isAtOrAboveTopmostSection(long sectionPos) {
      long m = ChunkSectionPos.withZeroY(sectionPos);
      int i = ((Data)this.storage).columnToTopSection.get(m);
      return i == ((Data)this.storage).minSectionY || ChunkSectionPos.unpackY(sectionPos) >= i;
   }

   protected int getTopSectionForColumn(long columnPos) {
      return ((Data)this.storage).columnToTopSection.get(columnPos);
   }

   protected int getMinSectionY() {
      return ((Data)this.storage).minSectionY;
   }

   protected static final class Data extends ChunkToNibbleArrayMap {
      int minSectionY;
      final Long2IntOpenHashMap columnToTopSection;

      public Data(Long2ObjectOpenHashMap arrays, Long2IntOpenHashMap columnToTopSection, int minSectionY) {
         super(arrays);
         this.columnToTopSection = columnToTopSection;
         columnToTopSection.defaultReturnValue(minSectionY);
         this.minSectionY = minSectionY;
      }

      public Data copy() {
         return new Data(this.arrays.clone(), this.columnToTopSection.clone(), this.minSectionY);
      }

      // $FF: synthetic method
      public ChunkToNibbleArrayMap copy() {
         return this.copy();
      }
   }
}
