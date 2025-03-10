package net.minecraft.server.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerLightingProvider extends LightingProvider implements AutoCloseable {
   public static final int field_44692 = 1000;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final TaskExecutor processor;
   private final ObjectList pendingTasks = new ObjectArrayList();
   private final ThreadedAnvilChunkStorage chunkStorage;
   private final MessageListener executor;
   private final int taskBatchSize = 1000;
   private final AtomicBoolean ticking = new AtomicBoolean();

   public ServerLightingProvider(ChunkProvider chunkProvider, ThreadedAnvilChunkStorage chunkStorage, boolean hasBlockLight, TaskExecutor processor, MessageListener executor) {
      super(chunkProvider, true, hasBlockLight);
      this.chunkStorage = chunkStorage;
      this.executor = executor;
      this.processor = processor;
   }

   public void close() {
   }

   public int doLightUpdates() {
      throw (UnsupportedOperationException)Util.throwOrPause(new UnsupportedOperationException("Ran automatically on a different thread!"));
   }

   public void checkBlock(BlockPos pos) {
      BlockPos lv = pos.toImmutable();
      this.enqueue(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.checkBlock(lv);
      }, () -> {
         return "checkBlock " + lv;
      }));
   }

   protected void updateChunkStatus(ChunkPos pos) {
      this.enqueue(pos.x, pos.z, () -> {
         return 0;
      }, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.setRetainData(pos, false);
         super.setColumnEnabled(pos, false);

         int i;
         for(i = this.getBottomY(); i < this.getTopY(); ++i) {
            super.enqueueSectionData(LightType.BLOCK, ChunkSectionPos.from(pos, i), (ChunkNibbleArray)null);
            super.enqueueSectionData(LightType.SKY, ChunkSectionPos.from(pos, i), (ChunkNibbleArray)null);
         }

         for(i = this.world.getBottomSectionCoord(); i < this.world.getTopSectionCoord(); ++i) {
            super.setSectionStatus(ChunkSectionPos.from(pos, i), true);
         }

      }, () -> {
         return "updateChunkStatus " + pos + " true";
      }));
   }

   public void setSectionStatus(ChunkSectionPos pos, boolean notReady) {
      this.enqueue(pos.getSectionX(), pos.getSectionZ(), () -> {
         return 0;
      }, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.setSectionStatus(pos, notReady);
      }, () -> {
         return "updateSectionStatus " + pos + " " + notReady;
      }));
   }

   public void propagateLight(ChunkPos chunkPos) {
      this.enqueue(chunkPos.x, chunkPos.z, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.propagateLight(chunkPos);
      }, () -> {
         return "propagateLight " + chunkPos;
      }));
   }

   public void setColumnEnabled(ChunkPos pos, boolean retainData) {
      this.enqueue(pos.x, pos.z, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.setColumnEnabled(pos, retainData);
      }, () -> {
         return "enableLight " + pos + " " + retainData;
      }));
   }

   public void enqueueSectionData(LightType lightType, ChunkSectionPos pos, @Nullable ChunkNibbleArray nibbles) {
      this.enqueue(pos.getSectionX(), pos.getSectionZ(), () -> {
         return 0;
      }, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.enqueueSectionData(lightType, pos, nibbles);
      }, () -> {
         return "queueData " + pos;
      }));
   }

   private void enqueue(int x, int z, Stage stage, Runnable task) {
      this.enqueue(x, z, this.chunkStorage.getCompletedLevelSupplier(ChunkPos.toLong(x, z)), stage, task);
   }

   private void enqueue(int x, int z, IntSupplier completedLevelSupplier, Stage stage, Runnable task) {
      this.executor.send(ChunkTaskPrioritySystem.createMessage(() -> {
         this.pendingTasks.add(Pair.of(stage, task));
         if (this.pendingTasks.size() >= 1000) {
            this.runTasks();
         }

      }, ChunkPos.toLong(x, z), completedLevelSupplier));
   }

   public void setRetainData(ChunkPos pos, boolean retainData) {
      this.enqueue(pos.x, pos.z, () -> {
         return 0;
      }, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.setRetainData(pos, retainData);
      }, () -> {
         return "retainData " + pos;
      }));
   }

   public CompletableFuture initializeLight(Chunk chunk, boolean bl) {
      ChunkPos lv = chunk.getPos();
      this.enqueue(lv.x, lv.z, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         ChunkSection[] lvs = chunk.getSectionArray();

         for(int i = 0; i < chunk.countVerticalSections(); ++i) {
            ChunkSection lvx = lvs[i];
            if (!lvx.isEmpty()) {
               int j = this.world.sectionIndexToCoord(i);
               super.setSectionStatus(ChunkSectionPos.from(lv, j), false);
            }
         }

      }, () -> {
         return "initializeLight: " + lv;
      }));
      return CompletableFuture.supplyAsync(() -> {
         super.setColumnEnabled(lv, bl);
         super.setRetainData(lv, false);
         return chunk;
      }, (task) -> {
         this.enqueue(lv.x, lv.z, ServerLightingProvider.Stage.POST_UPDATE, task);
      });
   }

   public CompletableFuture light(Chunk chunk, boolean excludeBlocks) {
      ChunkPos lv = chunk.getPos();
      chunk.setLightOn(false);
      this.enqueue(lv.x, lv.z, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         if (!excludeBlocks) {
            super.propagateLight(lv);
         }

      }, () -> {
         return "lightChunk " + lv + " " + excludeBlocks;
      }));
      return CompletableFuture.supplyAsync(() -> {
         chunk.setLightOn(true);
         this.chunkStorage.releaseLightTicket(lv);
         return chunk;
      }, (task) -> {
         this.enqueue(lv.x, lv.z, ServerLightingProvider.Stage.POST_UPDATE, task);
      });
   }

   public void tick() {
      if ((!this.pendingTasks.isEmpty() || super.hasUpdates()) && this.ticking.compareAndSet(false, true)) {
         this.processor.send(() -> {
            this.runTasks();
            this.ticking.set(false);
         });
      }

   }

   private void runTasks() {
      int i = Math.min(this.pendingTasks.size(), 1000);
      ObjectListIterator objectListIterator = this.pendingTasks.iterator();

      int j;
      Pair pair;
      for(j = 0; objectListIterator.hasNext() && j < i; ++j) {
         pair = (Pair)objectListIterator.next();
         if (pair.getFirst() == ServerLightingProvider.Stage.PRE_UPDATE) {
            ((Runnable)pair.getSecond()).run();
         }
      }

      objectListIterator.back(j);
      super.doLightUpdates();

      for(j = 0; objectListIterator.hasNext() && j < i; ++j) {
         pair = (Pair)objectListIterator.next();
         if (pair.getFirst() == ServerLightingProvider.Stage.POST_UPDATE) {
            ((Runnable)pair.getSecond()).run();
         }

         objectListIterator.remove();
      }

   }

   private static enum Stage {
      PRE_UPDATE,
      POST_UPDATE;

      // $FF: synthetic method
      private static Stage[] method_36577() {
         return new Stage[]{PRE_UPDATE, POST_UPDATE};
      }
   }
}
