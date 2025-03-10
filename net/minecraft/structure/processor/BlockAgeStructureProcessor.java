package net.minecraft.structure.processor;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class BlockAgeStructureProcessor extends StructureProcessor {
   public static final Codec CODEC;
   private static final float field_31681 = 0.5F;
   private static final float field_31682 = 0.5F;
   private static final float field_31683 = 0.15F;
   private static final BlockState[] AGEABLE_SLABS;
   private final float mossiness;

   public BlockAgeStructureProcessor(float mossiness) {
      this.mossiness = mossiness;
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
      Random lv = data.getRandom(currentBlockInfo.pos());
      BlockState lv2 = currentBlockInfo.state();
      BlockPos lv3 = currentBlockInfo.pos();
      BlockState lv4 = null;
      if (!lv2.isOf(Blocks.STONE_BRICKS) && !lv2.isOf(Blocks.STONE) && !lv2.isOf(Blocks.CHISELED_STONE_BRICKS)) {
         if (lv2.isIn(BlockTags.STAIRS)) {
            lv4 = this.processStairs(lv, currentBlockInfo.state());
         } else if (lv2.isIn(BlockTags.SLABS)) {
            lv4 = this.processSlabs(lv);
         } else if (lv2.isIn(BlockTags.WALLS)) {
            lv4 = this.processWalls(lv);
         } else if (lv2.isOf(Blocks.OBSIDIAN)) {
            lv4 = this.processObsidian(lv);
         }
      } else {
         lv4 = this.processBlocks(lv);
      }

      return lv4 != null ? new StructureTemplate.StructureBlockInfo(lv3, lv4, currentBlockInfo.nbt()) : currentBlockInfo;
   }

   @Nullable
   private BlockState processBlocks(Random random) {
      if (random.nextFloat() >= 0.5F) {
         return null;
      } else {
         BlockState[] lvs = new BlockState[]{Blocks.CRACKED_STONE_BRICKS.getDefaultState(), randomStairProperties(random, Blocks.STONE_BRICK_STAIRS)};
         BlockState[] lvs2 = new BlockState[]{Blocks.MOSSY_STONE_BRICKS.getDefaultState(), randomStairProperties(random, Blocks.MOSSY_STONE_BRICK_STAIRS)};
         return this.process(random, lvs, lvs2);
      }
   }

   @Nullable
   private BlockState processStairs(Random random, BlockState state) {
      Direction lv = (Direction)state.get(StairsBlock.FACING);
      BlockHalf lv2 = (BlockHalf)state.get(StairsBlock.HALF);
      if (random.nextFloat() >= 0.5F) {
         return null;
      } else {
         BlockState[] lvs = new BlockState[]{(BlockState)((BlockState)Blocks.MOSSY_STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, lv)).with(StairsBlock.HALF, lv2), Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState()};
         return this.process(random, AGEABLE_SLABS, lvs);
      }
   }

   @Nullable
   private BlockState processSlabs(Random random) {
      return random.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_SLAB.getDefaultState() : null;
   }

   @Nullable
   private BlockState processWalls(Random random) {
      return random.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_WALL.getDefaultState() : null;
   }

   @Nullable
   private BlockState processObsidian(Random random) {
      return random.nextFloat() < 0.15F ? Blocks.CRYING_OBSIDIAN.getDefaultState() : null;
   }

   private static BlockState randomStairProperties(Random random, Block stairs) {
      return (BlockState)((BlockState)stairs.getDefaultState().with(StairsBlock.FACING, Direction.Type.HORIZONTAL.random(random))).with(StairsBlock.HALF, (BlockHalf)Util.getRandom((Object[])BlockHalf.values(), random));
   }

   private BlockState process(Random random, BlockState[] regularStates, BlockState[] mossyStates) {
      return random.nextFloat() < this.mossiness ? randomState(random, mossyStates) : randomState(random, regularStates);
   }

   private static BlockState randomState(Random random, BlockState[] states) {
      return states[random.nextInt(states.length)];
   }

   protected StructureProcessorType getType() {
      return StructureProcessorType.BLOCK_AGE;
   }

   static {
      CODEC = Codec.FLOAT.fieldOf("mossiness").xmap(BlockAgeStructureProcessor::new, (processor) -> {
         return processor.mossiness;
      }).codec();
      AGEABLE_SLABS = new BlockState[]{Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_BRICK_SLAB.getDefaultState()};
   }
}
