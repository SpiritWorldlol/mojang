package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkLightProvider implements ChunkLightingView {
   public static final int field_44729 = 15;
   protected static final int field_44730 = 1;
   protected static final long field_44731 = ChunkLightProvider.class_8531.packWithAllDirectionsSet(1);
   private static final int field_44732 = 512;
   protected static final Direction[] DIRECTIONS = Direction.values();
   protected final ChunkProvider chunkProvider;
   protected final LightStorage lightStorage;
   private final LongOpenHashSet blockPositionsToCheck = new LongOpenHashSet(512, 0.5F);
   private final LongArrayFIFOQueue field_44734 = new LongArrayFIFOQueue();
   private final LongArrayFIFOQueue field_44735 = new LongArrayFIFOQueue();
   private final BlockPos.Mutable reusableBlockPos = new BlockPos.Mutable();
   private static final int field_31709 = 2;
   private final long[] cachedChunkPositions = new long[2];
   private final LightSourceView[] cachedChunks = new LightSourceView[2];

   protected ChunkLightProvider(ChunkProvider chunkProvider, LightStorage lightStorage) {
      this.chunkProvider = chunkProvider;
      this.lightStorage = lightStorage;
      this.clearChunkCache();
   }

   public static boolean needsLightUpdate(BlockView blockView, BlockPos pos, BlockState oldState, BlockState newState) {
      if (newState == oldState) {
         return false;
      } else {
         return newState.getOpacity(blockView, pos) != oldState.getOpacity(blockView, pos) || newState.getLuminance() != oldState.getLuminance() || newState.hasSidedTransparency() || oldState.hasSidedTransparency();
      }
   }

   public static int getRealisticOpacity(BlockView world, BlockState state1, BlockPos pos1, BlockState state2, BlockPos pos2, Direction direction, int opacity2) {
      boolean bl = isTrivialForLighting(state1);
      boolean bl2 = isTrivialForLighting(state2);
      if (bl && bl2) {
         return opacity2;
      } else {
         VoxelShape lv = bl ? VoxelShapes.empty() : state1.getCullingShape(world, pos1);
         VoxelShape lv2 = bl2 ? VoxelShapes.empty() : state2.getCullingShape(world, pos2);
         return VoxelShapes.adjacentSidesCoverSquare(lv, lv2, direction) ? 16 : opacity2;
      }
   }

   public static VoxelShape getOpaqueShape(BlockView blockView, BlockPos pos, BlockState blockState, Direction direction) {
      return isTrivialForLighting(blockState) ? VoxelShapes.empty() : blockState.getCullingFace(blockView, pos, direction);
   }

   protected static boolean isTrivialForLighting(BlockState blockState) {
      return !blockState.isOpaque() || !blockState.hasSidedTransparency();
   }

   protected BlockState getStateForLighting(BlockPos pos) {
      int i = ChunkSectionPos.getSectionCoord(pos.getX());
      int j = ChunkSectionPos.getSectionCoord(pos.getZ());
      LightSourceView lv = this.getChunk(i, j);
      return lv == null ? Blocks.BEDROCK.getDefaultState() : lv.getBlockState(pos);
   }

   protected int getOpacity(BlockState state, BlockPos pos) {
      return Math.max(1, state.getOpacity(this.chunkProvider.getWorld(), pos));
   }

   protected boolean shapesCoverFullCube(long sourceId, BlockState sourceState, long targetId, BlockState targetState, Direction direction) {
      VoxelShape lv = this.getOpaqueShape(sourceState, sourceId, direction);
      VoxelShape lv2 = this.getOpaqueShape(targetState, targetId, direction.getOpposite());
      return VoxelShapes.unionCoversFullCube(lv, lv2);
   }

   protected VoxelShape getOpaqueShape(BlockState blockState, long pos, Direction direction) {
      return getOpaqueShape(this.chunkProvider.getWorld(), this.reusableBlockPos.set(pos), blockState, direction);
   }

   @Nullable
   protected LightSourceView getChunk(int chunkX, int chunkZ) {
      long l = ChunkPos.toLong(chunkX, chunkZ);

      for(int k = 0; k < 2; ++k) {
         if (l == this.cachedChunkPositions[k]) {
            return this.cachedChunks[k];
         }
      }

      LightSourceView lv = this.chunkProvider.getChunk(chunkX, chunkZ);

      for(int m = 1; m > 0; --m) {
         this.cachedChunkPositions[m] = this.cachedChunkPositions[m - 1];
         this.cachedChunks[m] = this.cachedChunks[m - 1];
      }

      this.cachedChunkPositions[0] = l;
      this.cachedChunks[0] = lv;
      return lv;
   }

   private void clearChunkCache() {
      Arrays.fill(this.cachedChunkPositions, ChunkPos.MARKER);
      Arrays.fill(this.cachedChunks, (Object)null);
   }

   public void checkBlock(BlockPos pos) {
      this.blockPositionsToCheck.add(pos.asLong());
   }

   public void enqueueSectionData(long sectionPos, @Nullable ChunkNibbleArray lightArray) {
      this.lightStorage.enqueueSectionData(sectionPos, lightArray);
   }

   public void setRetainColumn(ChunkPos pos, boolean retainData) {
      this.lightStorage.setRetainColumn(ChunkSectionPos.withZeroY(pos.x, pos.z), retainData);
   }

   public void setSectionStatus(ChunkSectionPos pos, boolean notReady) {
      this.lightStorage.setSectionStatus(pos.asLong(), notReady);
   }

   public void setColumnEnabled(ChunkPos pos, boolean retainData) {
      this.lightStorage.setColumnEnabled(ChunkSectionPos.withZeroY(pos.x, pos.z), retainData);
   }

   public int doLightUpdates() {
      LongIterator longIterator = this.blockPositionsToCheck.iterator();

      while(longIterator.hasNext()) {
         this.method_51529(longIterator.nextLong());
      }

      this.blockPositionsToCheck.clear();
      this.blockPositionsToCheck.trim(512);
      int i = 0;
      i += this.method_51570();
      i += this.method_51567();
      this.clearChunkCache();
      this.lightStorage.updateLight(this);
      this.lightStorage.notifyChanges();
      return i;
   }

   private int method_51567() {
      int i;
      for(i = 0; !this.field_44735.isEmpty(); ++i) {
         long l = this.field_44735.dequeueLong();
         long m = this.field_44735.dequeueLong();
         int j = this.lightStorage.get(l);
         int k = ChunkLightProvider.class_8531.getLightLevel(m);
         if (ChunkLightProvider.class_8531.method_51582(m) && j < k) {
            this.lightStorage.set(l, k);
            j = k;
         }

         if (j == k) {
            this.method_51531(l, m, j);
         }
      }

      return i;
   }

   private int method_51570() {
      int i;
      for(i = 0; !this.field_44734.isEmpty(); ++i) {
         long l = this.field_44734.dequeueLong();
         long m = this.field_44734.dequeueLong();
         this.method_51530(l, m);
      }

      return i;
   }

   protected void method_51565(long blockPos, long flags) {
      this.field_44734.enqueue(blockPos);
      this.field_44734.enqueue(flags);
   }

   protected void method_51566(long blockPos, long flags) {
      this.field_44735.enqueue(blockPos);
      this.field_44735.enqueue(flags);
   }

   public boolean hasUpdates() {
      return this.lightStorage.hasLightUpdates() || !this.blockPositionsToCheck.isEmpty() || !this.field_44734.isEmpty() || !this.field_44735.isEmpty();
   }

   @Nullable
   public ChunkNibbleArray getLightSection(ChunkSectionPos pos) {
      return this.lightStorage.getLightSection(pos.asLong());
   }

   public int getLightLevel(BlockPos pos) {
      return this.lightStorage.getLight(pos.asLong());
   }

   public String displaySectionLevel(long sectionPos) {
      return this.getStatus(sectionPos).getSigil();
   }

   public LightStorage.Status getStatus(long sectionPos) {
      return this.lightStorage.getStatus(sectionPos);
   }

   protected abstract void method_51529(long blockPos);

   protected abstract void method_51531(long blockPos, long m, int i);

   protected abstract void method_51530(long blockPos, long m);

   public static class class_8531 {
      private static final int DIRECTION_BIT_OFFSET = 4;
      private static final int field_44738 = 6;
      private static final long field_44739 = 15L;
      private static final long DIRECTION_BIT_MASK = 1008L;
      private static final long field_44741 = 1024L;
      private static final long field_44742 = 2048L;

      public static long packWithOneDirectionCleared(int lightLevel, Direction direction) {
         long l = clearDirectionBit(1008L, direction);
         return withLightLevel(l, lightLevel);
      }

      public static long packWithAllDirectionsSet(int lightLevel) {
         return withLightLevel(1008L, lightLevel);
      }

      public static long method_51573(int lightLevel, boolean trivial) {
         long l = 1008L;
         l |= 2048L;
         if (trivial) {
            l |= 1024L;
         }

         return withLightLevel(l, lightLevel);
      }

      public static long method_51574(int lightLevel, boolean trivial, Direction direction) {
         long l = clearDirectionBit(1008L, direction);
         if (trivial) {
            l |= 1024L;
         }

         return withLightLevel(l, lightLevel);
      }

      public static long method_51579(int lightLevel, boolean trivial, Direction direction) {
         long l = 0L;
         if (trivial) {
            l |= 1024L;
         }

         l = setDirectionBit(l, direction);
         return withLightLevel(l, lightLevel);
      }

      public static long method_51578(boolean down, boolean north, boolean south, boolean west, boolean east) {
         long l = withLightLevel(0L, 15);
         if (down) {
            l = setDirectionBit(l, Direction.DOWN);
         }

         if (north) {
            l = setDirectionBit(l, Direction.NORTH);
         }

         if (south) {
            l = setDirectionBit(l, Direction.SOUTH);
         }

         if (west) {
            l = setDirectionBit(l, Direction.WEST);
         }

         if (east) {
            l = setDirectionBit(l, Direction.EAST);
         }

         return l;
      }

      public static int getLightLevel(long packed) {
         return (int)(packed & 15L);
      }

      public static boolean isTrivial(long packed) {
         return (packed & 1024L) != 0L;
      }

      public static boolean method_51582(long packed) {
         return (packed & 2048L) != 0L;
      }

      public static boolean isDirectionBitSet(long packed, Direction direction) {
         return (packed & 1L << direction.ordinal() + 4) != 0L;
      }

      private static long withLightLevel(long packed, int lightLevel) {
         return packed & -16L | (long)lightLevel & 15L;
      }

      private static long setDirectionBit(long packed, Direction direction) {
         return packed | 1L << direction.ordinal() + 4;
      }

      private static long clearDirectionBit(long packed, Direction direction) {
         return packed & ~(1L << direction.ordinal() + 4);
      }
   }
}
