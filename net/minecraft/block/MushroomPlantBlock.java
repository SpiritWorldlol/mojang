package net.minecraft.block;

import java.util.Iterator;
import java.util.Optional;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class MushroomPlantBlock extends PlantBlock implements Fertilizable {
   protected static final float field_31195 = 3.0F;
   protected static final VoxelShape SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);
   private final RegistryKey featureKey;

   public MushroomPlantBlock(AbstractBlock.Settings settings, RegistryKey featureKey) {
      super(settings);
      this.featureKey = featureKey;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (random.nextInt(25) == 0) {
         int i = 5;
         int j = true;
         Iterator var7 = BlockPos.iterate(pos.add(-4, -1, -4), pos.add(4, 1, 4)).iterator();

         while(var7.hasNext()) {
            BlockPos lv = (BlockPos)var7.next();
            if (world.getBlockState(lv).isOf(this)) {
               --i;
               if (i <= 0) {
                  return;
               }
            }
         }

         BlockPos lv2 = pos.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);

         for(int k = 0; k < 4; ++k) {
            if (world.isAir(lv2) && state.canPlaceAt(world, lv2)) {
               pos = lv2;
            }

            lv2 = pos.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
         }

         if (world.isAir(lv2) && state.canPlaceAt(world, lv2)) {
            world.setBlockState(lv2, state, Block.NOTIFY_LISTENERS);
         }
      }

   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return floor.isOpaqueFullCube(world, pos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos lv = pos.down();
      BlockState lv2 = world.getBlockState(lv);
      if (lv2.isIn(BlockTags.MUSHROOM_GROW_BLOCK)) {
         return true;
      } else {
         return world.getBaseLightLevel(pos, 0) < 13 && this.canPlantOnTop(lv2, world, lv);
      }
   }

   public boolean trySpawningBigMushroom(ServerWorld world, BlockPos pos, BlockState state, Random random) {
      Optional optional = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).getEntry(this.featureKey);
      if (optional.isEmpty()) {
         return false;
      } else {
         world.removeBlock(pos, false);
         if (((ConfiguredFeature)((RegistryEntry)optional.get()).value()).generate(world, world.getChunkManager().getChunkGenerator(), random, pos)) {
            return true;
         } else {
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            return false;
         }
      }
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return (double)random.nextFloat() < 0.4;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      this.trySpawningBigMushroom(world, pos, state, random);
   }
}
