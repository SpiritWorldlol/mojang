package net.minecraft.world.chunk;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;

public class ChunkCache implements BlockView, CollisionView {
   protected final int minX;
   protected final int minZ;
   protected final Chunk[][] chunks;
   protected boolean empty;
   protected final World world;
   private final Supplier plainsEntryGetter;

   public ChunkCache(World world, BlockPos minPos, BlockPos maxPos) {
      this.world = world;
      this.plainsEntryGetter = Suppliers.memoize(() -> {
         return world.getRegistryManager().get(RegistryKeys.BIOME).entryOf(BiomeKeys.PLAINS);
      });
      this.minX = ChunkSectionPos.getSectionCoord(minPos.getX());
      this.minZ = ChunkSectionPos.getSectionCoord(minPos.getZ());
      int i = ChunkSectionPos.getSectionCoord(maxPos.getX());
      int j = ChunkSectionPos.getSectionCoord(maxPos.getZ());
      this.chunks = new Chunk[i - this.minX + 1][j - this.minZ + 1];
      ChunkManager lv = world.getChunkManager();
      this.empty = true;

      int k;
      int l;
      for(k = this.minX; k <= i; ++k) {
         for(l = this.minZ; l <= j; ++l) {
            this.chunks[k - this.minX][l - this.minZ] = lv.getWorldChunk(k, l);
         }
      }

      for(k = ChunkSectionPos.getSectionCoord(minPos.getX()); k <= ChunkSectionPos.getSectionCoord(maxPos.getX()); ++k) {
         for(l = ChunkSectionPos.getSectionCoord(minPos.getZ()); l <= ChunkSectionPos.getSectionCoord(maxPos.getZ()); ++l) {
            Chunk lv2 = this.chunks[k - this.minX][l - this.minZ];
            if (lv2 != null && !lv2.areSectionsEmptyBetween(minPos.getY(), maxPos.getY())) {
               this.empty = false;
               return;
            }
         }
      }

   }

   private Chunk getChunk(BlockPos pos) {
      return this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
   }

   private Chunk getChunk(int chunkX, int chunkZ) {
      int k = chunkX - this.minX;
      int l = chunkZ - this.minZ;
      if (k >= 0 && k < this.chunks.length && l >= 0 && l < this.chunks[k].length) {
         Chunk lv = this.chunks[k][l];
         return (Chunk)(lv != null ? lv : new EmptyChunk(this.world, new ChunkPos(chunkX, chunkZ), (RegistryEntry)this.plainsEntryGetter.get()));
      } else {
         return new EmptyChunk(this.world, new ChunkPos(chunkX, chunkZ), (RegistryEntry)this.plainsEntryGetter.get());
      }
   }

   public WorldBorder getWorldBorder() {
      return this.world.getWorldBorder();
   }

   public BlockView getChunkAsView(int chunkX, int chunkZ) {
      return this.getChunk(chunkX, chunkZ);
   }

   public List getEntityCollisions(@Nullable Entity entity, Box box) {
      return List.of();
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      Chunk lv = this.getChunk(pos);
      return lv.getBlockEntity(pos);
   }

   public BlockState getBlockState(BlockPos pos) {
      if (this.isOutOfHeightLimit(pos)) {
         return Blocks.AIR.getDefaultState();
      } else {
         Chunk lv = this.getChunk(pos);
         return lv.getBlockState(pos);
      }
   }

   public FluidState getFluidState(BlockPos pos) {
      if (this.isOutOfHeightLimit(pos)) {
         return Fluids.EMPTY.getDefaultState();
      } else {
         Chunk lv = this.getChunk(pos);
         return lv.getFluidState(pos);
      }
   }

   public int getBottomY() {
      return this.world.getBottomY();
   }

   public int getHeight() {
      return this.world.getHeight();
   }

   public Profiler getProfiler() {
      return this.world.getProfiler();
   }
}
