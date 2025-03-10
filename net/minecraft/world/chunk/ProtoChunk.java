package net.minecraft.world.chunk;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.SimpleTickScheduler;
import org.jetbrains.annotations.Nullable;

public class ProtoChunk extends Chunk {
   @Nullable
   private volatile LightingProvider lightingProvider;
   private volatile ChunkStatus status;
   private final List entities;
   private final Map carvingMasks;
   @Nullable
   private BelowZeroRetrogen belowZeroRetrogen;
   private final SimpleTickScheduler blockTickScheduler;
   private final SimpleTickScheduler fluidTickScheduler;

   public ProtoChunk(ChunkPos pos, UpgradeData upgradeData, HeightLimitView world, Registry biomeRegistry, @Nullable BlendingData blendingData) {
      this(pos, upgradeData, (ChunkSection[])null, new SimpleTickScheduler(), new SimpleTickScheduler(), world, biomeRegistry, blendingData);
   }

   public ProtoChunk(ChunkPos pos, UpgradeData upgradeData, @Nullable ChunkSection[] sections, SimpleTickScheduler blockTickScheduler, SimpleTickScheduler fluidTickScheduler, HeightLimitView world, Registry biomeRegistry, @Nullable BlendingData blendingData) {
      super(pos, upgradeData, world, biomeRegistry, 0L, sections, blendingData);
      this.status = ChunkStatus.EMPTY;
      this.entities = Lists.newArrayList();
      this.carvingMasks = new Object2ObjectArrayMap();
      this.blockTickScheduler = blockTickScheduler;
      this.fluidTickScheduler = fluidTickScheduler;
   }

   public BasicTickScheduler getBlockTickScheduler() {
      return this.blockTickScheduler;
   }

   public BasicTickScheduler getFluidTickScheduler() {
      return this.fluidTickScheduler;
   }

   public Chunk.TickSchedulers getTickSchedulers() {
      return new Chunk.TickSchedulers(this.blockTickScheduler, this.fluidTickScheduler);
   }

   public BlockState getBlockState(BlockPos pos) {
      int i = pos.getY();
      if (this.isOutOfHeightLimit(i)) {
         return Blocks.VOID_AIR.getDefaultState();
      } else {
         ChunkSection lv = this.getSection(this.getSectionIndex(i));
         return lv.isEmpty() ? Blocks.AIR.getDefaultState() : lv.getBlockState(pos.getX() & 15, i & 15, pos.getZ() & 15);
      }
   }

   public FluidState getFluidState(BlockPos pos) {
      int i = pos.getY();
      if (this.isOutOfHeightLimit(i)) {
         return Fluids.EMPTY.getDefaultState();
      } else {
         ChunkSection lv = this.getSection(this.getSectionIndex(i));
         return lv.isEmpty() ? Fluids.EMPTY.getDefaultState() : lv.getFluidState(pos.getX() & 15, i & 15, pos.getZ() & 15);
      }
   }

   @Nullable
   public BlockState setBlockState(BlockPos pos, BlockState state, boolean moved) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      if (j >= this.getBottomY() && j < this.getTopY()) {
         int l = this.getSectionIndex(j);
         ChunkSection lv = this.getSection(l);
         boolean bl2 = lv.isEmpty();
         if (bl2 && state.isOf(Blocks.AIR)) {
            return state;
         } else {
            int m = ChunkSectionPos.getLocalCoord(i);
            int n = ChunkSectionPos.getLocalCoord(j);
            int o = ChunkSectionPos.getLocalCoord(k);
            BlockState lv2 = lv.setBlockState(m, n, o, state);
            if (this.status.isAtLeast(ChunkStatus.INITIALIZE_LIGHT)) {
               boolean bl3 = lv.isEmpty();
               if (bl3 != bl2) {
                  this.lightingProvider.setSectionStatus(pos, bl3);
               }

               if (ChunkLightProvider.needsLightUpdate(this, pos, lv2, state)) {
                  this.chunkSkyLight.isSkyLightAccessible(this, m, j, o);
                  this.lightingProvider.checkBlock(pos);
               }
            }

            EnumSet enumSet = this.getStatus().getHeightmapTypes();
            EnumSet enumSet2 = null;
            Iterator var16 = enumSet.iterator();

            Heightmap.Type lv3;
            while(var16.hasNext()) {
               lv3 = (Heightmap.Type)var16.next();
               Heightmap lv4 = (Heightmap)this.heightmaps.get(lv3);
               if (lv4 == null) {
                  if (enumSet2 == null) {
                     enumSet2 = EnumSet.noneOf(Heightmap.Type.class);
                  }

                  enumSet2.add(lv3);
               }
            }

            if (enumSet2 != null) {
               Heightmap.populateHeightmaps(this, enumSet2);
            }

            var16 = enumSet.iterator();

            while(var16.hasNext()) {
               lv3 = (Heightmap.Type)var16.next();
               ((Heightmap)this.heightmaps.get(lv3)).trackUpdate(m, j, o, state);
            }

            return lv2;
         }
      } else {
         return Blocks.VOID_AIR.getDefaultState();
      }
   }

   public void setBlockEntity(BlockEntity blockEntity) {
      this.blockEntities.put(blockEntity.getPos(), blockEntity);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      return (BlockEntity)this.blockEntities.get(pos);
   }

   public Map getBlockEntities() {
      return this.blockEntities;
   }

   public void addEntity(NbtCompound entityNbt) {
      this.entities.add(entityNbt);
   }

   public void addEntity(Entity entity) {
      if (!entity.hasVehicle()) {
         NbtCompound lv = new NbtCompound();
         entity.saveNbt(lv);
         this.addEntity(lv);
      }
   }

   public void setStructureStart(Structure structure, StructureStart start) {
      BelowZeroRetrogen lv = this.getBelowZeroRetrogen();
      if (lv != null && start.hasChildren()) {
         BlockBox lv2 = start.getBoundingBox();
         HeightLimitView lv3 = this.getHeightLimitView();
         if (lv2.getMinY() < lv3.getBottomY() || lv2.getMaxY() >= lv3.getTopY()) {
            return;
         }
      }

      super.setStructureStart(structure, start);
   }

   public List getEntities() {
      return this.entities;
   }

   public ChunkStatus getStatus() {
      return this.status;
   }

   public void setStatus(ChunkStatus status) {
      this.status = status;
      if (this.belowZeroRetrogen != null && status.isAtLeast(this.belowZeroRetrogen.getTargetStatus())) {
         this.setBelowZeroRetrogen((BelowZeroRetrogen)null);
      }

      this.setNeedsSaving(true);
   }

   public RegistryEntry getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
      if (this.method_51526().isAtLeast(ChunkStatus.BIOMES)) {
         return super.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
      } else {
         throw new IllegalStateException("Asking for biomes before we have biomes");
      }
   }

   public static short getPackedSectionRelative(BlockPos pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      int l = i & 15;
      int m = j & 15;
      int n = k & 15;
      return (short)(l | m << 4 | n << 8);
   }

   public static BlockPos joinBlockPos(short sectionRel, int sectionY, ChunkPos chunkPos) {
      int j = ChunkSectionPos.getOffsetPos(chunkPos.x, sectionRel & 15);
      int k = ChunkSectionPos.getOffsetPos(sectionY, sectionRel >>> 4 & 15);
      int l = ChunkSectionPos.getOffsetPos(chunkPos.z, sectionRel >>> 8 & 15);
      return new BlockPos(j, k, l);
   }

   public void markBlockForPostProcessing(BlockPos pos) {
      if (!this.isOutOfHeightLimit(pos)) {
         Chunk.getList(this.postProcessingLists, this.getSectionIndex(pos.getY())).add(getPackedSectionRelative(pos));
      }

   }

   public void markBlockForPostProcessing(short packedPos, int index) {
      Chunk.getList(this.postProcessingLists, index).add(packedPos);
   }

   public Map getBlockEntityNbts() {
      return Collections.unmodifiableMap(this.blockEntityNbts);
   }

   @Nullable
   public NbtCompound getPackedBlockEntityNbt(BlockPos pos) {
      BlockEntity lv = this.getBlockEntity(pos);
      return lv != null ? lv.createNbtWithIdentifyingData() : (NbtCompound)this.blockEntityNbts.get(pos);
   }

   public void removeBlockEntity(BlockPos pos) {
      this.blockEntities.remove(pos);
      this.blockEntityNbts.remove(pos);
   }

   @Nullable
   public CarvingMask getCarvingMask(GenerationStep.Carver step) {
      return (CarvingMask)this.carvingMasks.get(step);
   }

   public CarvingMask getOrCreateCarvingMask(GenerationStep.Carver step) {
      return (CarvingMask)this.carvingMasks.computeIfAbsent(step, (step2) -> {
         return new CarvingMask(this.getHeight(), this.getBottomY());
      });
   }

   public void setCarvingMask(GenerationStep.Carver step, CarvingMask carvingMask) {
      this.carvingMasks.put(step, carvingMask);
   }

   public void setLightingProvider(LightingProvider lightingProvider) {
      this.lightingProvider = lightingProvider;
   }

   public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen belowZeroRetrogen) {
      this.belowZeroRetrogen = belowZeroRetrogen;
   }

   @Nullable
   public BelowZeroRetrogen getBelowZeroRetrogen() {
      return this.belowZeroRetrogen;
   }

   private static ChunkTickScheduler createProtoTickScheduler(SimpleTickScheduler tickScheduler) {
      return new ChunkTickScheduler(tickScheduler.getTicks());
   }

   public ChunkTickScheduler getBlockProtoTickScheduler() {
      return createProtoTickScheduler(this.blockTickScheduler);
   }

   public ChunkTickScheduler getFluidProtoTickScheduler() {
      return createProtoTickScheduler(this.fluidTickScheduler);
   }

   public HeightLimitView getHeightLimitView() {
      return (HeightLimitView)(this.hasBelowZeroRetrogen() ? BelowZeroRetrogen.BELOW_ZERO_VIEW : this);
   }
}
