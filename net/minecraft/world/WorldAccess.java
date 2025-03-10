package net.minecraft.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.block.NeighborUpdater;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

public interface WorldAccess extends RegistryWorldView, LunarWorldView {
   default long getLunarTime() {
      return this.getLevelProperties().getTimeOfDay();
   }

   long getTickOrder();

   QueryableTickScheduler getBlockTickScheduler();

   private OrderedTick createOrderedTick(BlockPos pos, Object type, int delay, TickPriority priority) {
      return new OrderedTick(type, pos, this.getLevelProperties().getTime() + (long)delay, priority, this.getTickOrder());
   }

   private OrderedTick createOrderedTick(BlockPos pos, Object type, int delay) {
      return new OrderedTick(type, pos, this.getLevelProperties().getTime() + (long)delay, this.getTickOrder());
   }

   default void scheduleBlockTick(BlockPos pos, Block block, int delay, TickPriority priority) {
      this.getBlockTickScheduler().scheduleTick(this.createOrderedTick(pos, block, delay, priority));
   }

   default void scheduleBlockTick(BlockPos pos, Block block, int delay) {
      this.getBlockTickScheduler().scheduleTick(this.createOrderedTick(pos, block, delay));
   }

   QueryableTickScheduler getFluidTickScheduler();

   default void scheduleFluidTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) {
      this.getFluidTickScheduler().scheduleTick(this.createOrderedTick(pos, fluid, delay, priority));
   }

   default void scheduleFluidTick(BlockPos pos, Fluid fluid, int delay) {
      this.getFluidTickScheduler().scheduleTick(this.createOrderedTick(pos, fluid, delay));
   }

   WorldProperties getLevelProperties();

   LocalDifficulty getLocalDifficulty(BlockPos pos);

   @Nullable
   MinecraftServer getServer();

   default Difficulty getDifficulty() {
      return this.getLevelProperties().getDifficulty();
   }

   ChunkManager getChunkManager();

   default boolean isChunkLoaded(int chunkX, int chunkZ) {
      return this.getChunkManager().isChunkLoaded(chunkX, chunkZ);
   }

   Random getRandom();

   default void updateNeighbors(BlockPos pos, Block block) {
   }

   default void replaceWithStateForNeighborUpdate(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth) {
      NeighborUpdater.replaceWithStateForNeighborUpdate(this, direction, neighborState, pos, neighborPos, flags, maxUpdateDepth - 1);
   }

   default void playSound(@Nullable PlayerEntity except, BlockPos pos, SoundEvent sound, SoundCategory category) {
      this.playSound(except, pos, sound, category, 1.0F, 1.0F);
   }

   void playSound(@Nullable PlayerEntity except, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch);

   void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ);

   void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data);

   default void syncWorldEvent(int eventId, BlockPos pos, int data) {
      this.syncWorldEvent((PlayerEntity)null, eventId, pos, data);
   }

   void emitGameEvent(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter);

   default void emitGameEvent(@Nullable Entity entity, GameEvent event, Vec3d pos) {
      this.emitGameEvent(event, pos, new GameEvent.Emitter(entity, (BlockState)null));
   }

   default void emitGameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos) {
      this.emitGameEvent(event, pos, new GameEvent.Emitter(entity, (BlockState)null));
   }

   default void emitGameEvent(GameEvent event, BlockPos pos, GameEvent.Emitter emitter) {
      this.emitGameEvent(event, Vec3d.ofCenter(pos), emitter);
   }
}
