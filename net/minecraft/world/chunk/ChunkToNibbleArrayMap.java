package net.minecraft.world.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkToNibbleArrayMap {
   private static final int field_31705 = 2;
   private final long[] cachePositions = new long[2];
   private final ChunkNibbleArray[] cacheArrays = new ChunkNibbleArray[2];
   private boolean cacheEnabled;
   protected final Long2ObjectOpenHashMap arrays;

   protected ChunkToNibbleArrayMap(Long2ObjectOpenHashMap arrays) {
      this.arrays = arrays;
      this.clearCache();
      this.cacheEnabled = true;
   }

   public abstract ChunkToNibbleArrayMap copy();

   public ChunkNibbleArray replaceWithCopy(long pos) {
      ChunkNibbleArray lv = ((ChunkNibbleArray)this.arrays.get(pos)).copy();
      this.arrays.put(pos, lv);
      this.clearCache();
      return lv;
   }

   public boolean containsKey(long chunkPos) {
      return this.arrays.containsKey(chunkPos);
   }

   @Nullable
   public ChunkNibbleArray get(long chunkPos) {
      if (this.cacheEnabled) {
         for(int i = 0; i < 2; ++i) {
            if (chunkPos == this.cachePositions[i]) {
               return this.cacheArrays[i];
            }
         }
      }

      ChunkNibbleArray lv = (ChunkNibbleArray)this.arrays.get(chunkPos);
      if (lv == null) {
         return null;
      } else {
         if (this.cacheEnabled) {
            for(int j = 1; j > 0; --j) {
               this.cachePositions[j] = this.cachePositions[j - 1];
               this.cacheArrays[j] = this.cacheArrays[j - 1];
            }

            this.cachePositions[0] = chunkPos;
            this.cacheArrays[0] = lv;
         }

         return lv;
      }
   }

   @Nullable
   public ChunkNibbleArray removeChunk(long chunkPos) {
      return (ChunkNibbleArray)this.arrays.remove(chunkPos);
   }

   public void put(long pos, ChunkNibbleArray data) {
      this.arrays.put(pos, data);
   }

   public void clearCache() {
      for(int i = 0; i < 2; ++i) {
         this.cachePositions[i] = Long.MAX_VALUE;
         this.cacheArrays[i] = null;
      }

   }

   public void disableCache() {
      this.cacheEnabled = false;
   }
}
