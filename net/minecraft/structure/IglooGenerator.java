package net.minecraft.structure;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class IglooGenerator {
   public static final int OFFSET_Y = 90;
   static final Identifier TOP_TEMPLATE = new Identifier("igloo/top");
   private static final Identifier MIDDLE_TEMPLATE = new Identifier("igloo/middle");
   private static final Identifier BOTTOM_TEMPLATE = new Identifier("igloo/bottom");
   static final Map OFFSETS;
   static final Map OFFSETS_FROM_TOP;

   public static void addPieces(StructureTemplateManager manager, BlockPos pos, BlockRotation rotation, StructurePiecesHolder holder, Random random) {
      if (random.nextDouble() < 0.5) {
         int i = random.nextInt(8) + 4;
         holder.addPiece(new Piece(manager, BOTTOM_TEMPLATE, pos, rotation, i * 3));

         for(int j = 0; j < i - 1; ++j) {
            holder.addPiece(new Piece(manager, MIDDLE_TEMPLATE, pos, rotation, j * 3));
         }
      }

      holder.addPiece(new Piece(manager, TOP_TEMPLATE, pos, rotation, 0));
   }

   static {
      OFFSETS = ImmutableMap.of(TOP_TEMPLATE, new BlockPos(3, 5, 5), MIDDLE_TEMPLATE, new BlockPos(1, 3, 1), BOTTOM_TEMPLATE, new BlockPos(3, 6, 7));
      OFFSETS_FROM_TOP = ImmutableMap.of(TOP_TEMPLATE, BlockPos.ORIGIN, MIDDLE_TEMPLATE, new BlockPos(2, -3, 4), BOTTOM_TEMPLATE, new BlockPos(0, -3, -2));
   }

   public static class Piece extends SimpleStructurePiece {
      public Piece(StructureTemplateManager manager, Identifier identifier, BlockPos pos, BlockRotation rotation, int yOffset) {
         super(StructurePieceType.IGLOO, 0, manager, identifier, identifier.toString(), createPlacementData(rotation, identifier), getPosOffset(identifier, pos, yOffset));
      }

      public Piece(StructureTemplateManager manager, NbtCompound nbt) {
         super(StructurePieceType.IGLOO, nbt, manager, (identifier) -> {
            return createPlacementData(BlockRotation.valueOf(nbt.getString("Rot")), identifier);
         });
      }

      private static StructurePlacementData createPlacementData(BlockRotation rotation, Identifier identifier) {
         return (new StructurePlacementData()).setRotation(rotation).setMirror(BlockMirror.NONE).setPosition((BlockPos)IglooGenerator.OFFSETS.get(identifier)).addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
      }

      private static BlockPos getPosOffset(Identifier identifier, BlockPos pos, int yOffset) {
         return pos.add((Vec3i)IglooGenerator.OFFSETS_FROM_TOP.get(identifier)).down(yOffset);
      }

      protected void writeNbt(StructureContext context, NbtCompound nbt) {
         super.writeNbt(context, nbt);
         nbt.putString("Rot", this.placementData.getRotation().name());
      }

      protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {
         if ("chest".equals(metadata)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            BlockEntity lv = world.getBlockEntity(pos.down());
            if (lv instanceof ChestBlockEntity) {
               ((ChestBlockEntity)lv).setLootTable(LootTables.IGLOO_CHEST_CHEST, random.nextLong());
            }

         }
      }

      public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
         Identifier lv = new Identifier(this.templateIdString);
         StructurePlacementData lv2 = createPlacementData(this.placementData.getRotation(), lv);
         BlockPos lv3 = (BlockPos)IglooGenerator.OFFSETS_FROM_TOP.get(lv);
         BlockPos lv4 = this.pos.add(StructureTemplate.transform(lv2, new BlockPos(3 - lv3.getX(), 0, -lv3.getZ())));
         int i = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, lv4.getX(), lv4.getZ());
         BlockPos lv5 = this.pos;
         this.pos = this.pos.add(0, i - 90 - 1, 0);
         super.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);
         if (lv.equals(IglooGenerator.TOP_TEMPLATE)) {
            BlockPos lv6 = this.pos.add(StructureTemplate.transform(lv2, new BlockPos(3, 0, 5)));
            BlockState lv7 = world.getBlockState(lv6.down());
            if (!lv7.isAir() && !lv7.isOf(Blocks.LADDER)) {
               world.setBlockState(lv6, Blocks.SNOW_BLOCK.getDefaultState(), Block.NOTIFY_ALL);
            }
         }

         this.pos = lv5;
      }
   }
}
