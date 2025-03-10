package net.minecraft.server;

import com.mojang.logging.LogUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class WorldGenerationProgressLogger implements WorldGenerationProgressListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final int totalCount;
   private int generatedCount;
   private long startTime;
   private long nextMessageTime = Long.MAX_VALUE;

   public WorldGenerationProgressLogger(int radius) {
      int j = radius * 2 + 1;
      this.totalCount = j * j;
   }

   public void start(ChunkPos spawnPos) {
      this.nextMessageTime = Util.getMeasuringTimeMs();
      this.startTime = this.nextMessageTime;
   }

   public void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status) {
      if (status == ChunkStatus.FULL) {
         ++this.generatedCount;
      }

      int i = this.getProgressPercentage();
      if (Util.getMeasuringTimeMs() > this.nextMessageTime) {
         this.nextMessageTime += 500L;
         LOGGER.info(Text.translatable("menu.preparingSpawn", MathHelper.clamp(i, 0, 100)).getString());
      }

   }

   public void start() {
   }

   public void stop() {
      LOGGER.info("Time elapsed: {} ms", Util.getMeasuringTimeMs() - this.startTime);
      this.nextMessageTime = Long.MAX_VALUE;
   }

   public int getProgressPercentage() {
      return MathHelper.floor((float)this.generatedCount * 100.0F / (float)this.totalCount);
   }
}
