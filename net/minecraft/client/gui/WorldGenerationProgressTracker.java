package net.minecraft.client.gui;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public class WorldGenerationProgressTracker implements WorldGenerationProgressListener {
   private final WorldGenerationProgressLogger progressLogger;
   private final Long2ObjectOpenHashMap chunkStatuses;
   private ChunkPos spawnPos = new ChunkPos(0, 0);
   private final int centerSize;
   private final int radius;
   private final int size;
   private boolean running;

   public WorldGenerationProgressTracker(int radius) {
      this.progressLogger = new WorldGenerationProgressLogger(radius);
      this.centerSize = radius * 2 + 1;
      this.radius = radius + ChunkStatus.getMaxDistanceFromFull();
      this.size = this.radius * 2 + 1;
      this.chunkStatuses = new Long2ObjectOpenHashMap();
   }

   public void start(ChunkPos spawnPos) {
      if (this.running) {
         this.progressLogger.start(spawnPos);
         this.spawnPos = spawnPos;
      }
   }

   public void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status) {
      if (this.running) {
         this.progressLogger.setChunkStatus(pos, status);
         if (status == null) {
            this.chunkStatuses.remove(pos.toLong());
         } else {
            this.chunkStatuses.put(pos.toLong(), status);
         }

      }
   }

   public void start() {
      this.running = true;
      this.chunkStatuses.clear();
      this.progressLogger.start();
   }

   public void stop() {
      this.running = false;
      this.progressLogger.stop();
   }

   public int getCenterSize() {
      return this.centerSize;
   }

   public int getSize() {
      return this.size;
   }

   public int getProgressPercentage() {
      return this.progressLogger.getProgressPercentage();
   }

   @Nullable
   public ChunkStatus getChunkStatus(int x, int z) {
      return (ChunkStatus)this.chunkStatuses.get(ChunkPos.toLong(x + this.spawnPos.x - this.radius, z + this.spawnPos.z - this.radius));
   }
}
