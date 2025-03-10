package net.minecraft.world.gen.surfacebuilder;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.chunk.BlockColumn;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseParametersKeys;

public class SurfaceBuilder {
   private static final BlockState WHITE_TERRACOTTA;
   private static final BlockState ORANGE_TERRACOTTA;
   private static final BlockState TERRACOTTA;
   private static final BlockState YELLOW_TERRACOTTA;
   private static final BlockState BROWN_TERRACOTTA;
   private static final BlockState RED_TERRACOTTA;
   private static final BlockState LIGHT_GRAY_TERRACOTTA;
   private static final BlockState PACKED_ICE;
   private static final BlockState SNOW_BLOCK;
   private final BlockState defaultState;
   private final int seaLevel;
   private final BlockState[] terracottaBands;
   private final DoublePerlinNoiseSampler terracottaBandsOffsetNoise;
   private final DoublePerlinNoiseSampler badlandsPillarNoise;
   private final DoublePerlinNoiseSampler badlandsPillarRoofNoise;
   private final DoublePerlinNoiseSampler badlandsSurfaceNoise;
   private final DoublePerlinNoiseSampler icebergPillarNoise;
   private final DoublePerlinNoiseSampler icebergPillarRoofNoise;
   private final DoublePerlinNoiseSampler icebergSurfaceNoise;
   private final RandomSplitter randomDeriver;
   private final DoublePerlinNoiseSampler surfaceNoise;
   private final DoublePerlinNoiseSampler surfaceSecondaryNoise;

   public SurfaceBuilder(NoiseConfig noiseConfig, BlockState defaultState, int seaLevel, RandomSplitter randomDeriver) {
      this.defaultState = defaultState;
      this.seaLevel = seaLevel;
      this.randomDeriver = randomDeriver;
      this.terracottaBandsOffsetNoise = noiseConfig.getOrCreateSampler(NoiseParametersKeys.CLAY_BANDS_OFFSET);
      this.terracottaBands = createTerracottaBands(randomDeriver.split(new Identifier("clay_bands")));
      this.surfaceNoise = noiseConfig.getOrCreateSampler(NoiseParametersKeys.SURFACE);
      this.surfaceSecondaryNoise = noiseConfig.getOrCreateSampler(NoiseParametersKeys.SURFACE_SECONDARY);
      this.badlandsPillarNoise = noiseConfig.getOrCreateSampler(NoiseParametersKeys.BADLANDS_PILLAR);
      this.badlandsPillarRoofNoise = noiseConfig.getOrCreateSampler(NoiseParametersKeys.BADLANDS_PILLAR_ROOF);
      this.badlandsSurfaceNoise = noiseConfig.getOrCreateSampler(NoiseParametersKeys.BADLANDS_SURFACE);
      this.icebergPillarNoise = noiseConfig.getOrCreateSampler(NoiseParametersKeys.ICEBERG_PILLAR);
      this.icebergPillarRoofNoise = noiseConfig.getOrCreateSampler(NoiseParametersKeys.ICEBERG_PILLAR_ROOF);
      this.icebergSurfaceNoise = noiseConfig.getOrCreateSampler(NoiseParametersKeys.ICEBERG_SURFACE);
   }

   public void buildSurface(NoiseConfig noiseConfig, BiomeAccess biomeAccess, Registry biomeRegistry, boolean useLegacyRandom, HeightContext heightContext, final Chunk chunk, ChunkNoiseSampler chunkNoiseSampler, MaterialRules.MaterialRule materialRule) {
      final BlockPos.Mutable lv = new BlockPos.Mutable();
      final ChunkPos lv2 = chunk.getPos();
      int i = lv2.getStartX();
      int j = lv2.getStartZ();
      BlockColumn lv3 = new BlockColumn() {
         public BlockState getState(int y) {
            return chunk.getBlockState(lv.setY(y));
         }

         public void setState(int y, BlockState state) {
            HeightLimitView lvx = chunk.getHeightLimitView();
            if (y >= lvx.getBottomY() && y < lvx.getTopY()) {
               chunk.setBlockState(lv.setY(y), state, false);
               if (!state.getFluidState().isEmpty()) {
                  chunk.markBlockForPostProcessing(lv);
               }
            }

         }

         public String toString() {
            return "ChunkBlockColumn " + lv2;
         }
      };
      Objects.requireNonNull(biomeAccess);
      MaterialRules.MaterialRuleContext lv4 = new MaterialRules.MaterialRuleContext(this, noiseConfig, chunk, chunkNoiseSampler, biomeAccess::getBiome, biomeRegistry, heightContext);
      MaterialRules.BlockStateRule lv5 = (MaterialRules.BlockStateRule)materialRule.apply(lv4);
      BlockPos.Mutable lv6 = new BlockPos.Mutable();

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            int m = i + k;
            int n = j + l;
            int o = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, k, l) + 1;
            lv.setX(m).setZ(n);
            RegistryEntry lv7 = biomeAccess.getBiome(lv6.set(m, useLegacyRandom ? 0 : o, n));
            if (lv7.matchesKey(BiomeKeys.ERODED_BADLANDS)) {
               this.placeBadlandsPillar(lv3, m, n, o, chunk);
            }

            int p = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, k, l) + 1;
            lv4.initHorizontalContext(m, n);
            int q = 0;
            int r = Integer.MIN_VALUE;
            int s = Integer.MAX_VALUE;
            int t = chunk.getBottomY();

            for(int u = p; u >= t; --u) {
               BlockState lv8 = lv3.getState(u);
               if (lv8.isAir()) {
                  q = 0;
                  r = Integer.MIN_VALUE;
               } else if (!lv8.getFluidState().isEmpty()) {
                  if (r == Integer.MIN_VALUE) {
                     r = u + 1;
                  }
               } else {
                  int v;
                  BlockState lv9;
                  if (s >= u) {
                     s = DimensionType.field_35479;

                     for(v = u - 1; v >= t - 1; --v) {
                        lv9 = lv3.getState(v);
                        if (!this.isDefaultBlock(lv9)) {
                           s = v + 1;
                           break;
                        }
                     }
                  }

                  ++q;
                  v = u - s + 1;
                  lv4.initVerticalContext(q, v, r, m, u, n);
                  if (lv8 == this.defaultState) {
                     lv9 = lv5.tryApply(m, u, n);
                     if (lv9 != null) {
                        lv3.setState(u, lv9);
                     }
                  }
               }
            }

            if (lv7.matchesKey(BiomeKeys.FROZEN_OCEAN) || lv7.matchesKey(BiomeKeys.DEEP_FROZEN_OCEAN)) {
               this.placeIceberg(lv4.method_39551(), (Biome)lv7.value(), lv3, lv6, m, n, o);
            }
         }
      }

   }

   protected int method_39552(int i, int j) {
      double d = this.surfaceNoise.sample((double)i, 0.0, (double)j);
      return (int)(d * 2.75 + 3.0 + this.randomDeriver.split(i, 0, j).nextDouble() * 0.25);
   }

   protected double method_39555(int i, int j) {
      return this.surfaceSecondaryNoise.sample((double)i, 0.0, (double)j);
   }

   private boolean isDefaultBlock(BlockState state) {
      return !state.isAir() && state.getFluidState().isEmpty();
   }

   /** @deprecated */
   @Deprecated
   public Optional applyMaterialRule(MaterialRules.MaterialRule rule, CarverContext context, Function posToBiome, Chunk chunk, ChunkNoiseSampler chunkNoiseSampler, BlockPos pos, boolean hasFluid) {
      MaterialRules.MaterialRuleContext lv = new MaterialRules.MaterialRuleContext(this, context.getNoiseConfig(), chunk, chunkNoiseSampler, posToBiome, context.getRegistryManager().get(RegistryKeys.BIOME), context);
      MaterialRules.BlockStateRule lv2 = (MaterialRules.BlockStateRule)rule.apply(lv);
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      lv.initHorizontalContext(i, k);
      lv.initVerticalContext(1, 1, hasFluid ? j + 1 : Integer.MIN_VALUE, i, j, k);
      BlockState lv3 = lv2.tryApply(i, j, k);
      return Optional.ofNullable(lv3);
   }

   private void placeBadlandsPillar(BlockColumn column, int x, int z, int surfaceY, HeightLimitView chunk) {
      double d = 0.2;
      double e = Math.min(Math.abs(this.badlandsSurfaceNoise.sample((double)x, 0.0, (double)z) * 8.25), this.badlandsPillarNoise.sample((double)x * 0.2, 0.0, (double)z * 0.2) * 15.0);
      if (!(e <= 0.0)) {
         double f = 0.75;
         double g = 1.5;
         double h = Math.abs(this.badlandsPillarRoofNoise.sample((double)x * 0.75, 0.0, (double)z * 0.75) * 1.5);
         double l = 64.0 + Math.min(e * e * 2.5, Math.ceil(h * 50.0) + 24.0);
         int m = MathHelper.floor(l);
         if (surfaceY <= m) {
            int n;
            for(n = m; n >= chunk.getBottomY(); --n) {
               BlockState lv = column.getState(n);
               if (lv.isOf(this.defaultState.getBlock())) {
                  break;
               }

               if (lv.isOf(Blocks.WATER)) {
                  return;
               }
            }

            for(n = m; n >= chunk.getBottomY() && column.getState(n).isAir(); --n) {
               column.setState(n, this.defaultState);
            }

         }
      }
   }

   private void placeIceberg(int minY, Biome biome, BlockColumn column, BlockPos.Mutable mutablePos, int x, int z, int surfaceY) {
      double d = 1.28;
      double e = Math.min(Math.abs(this.icebergSurfaceNoise.sample((double)x, 0.0, (double)z) * 8.25), this.icebergPillarNoise.sample((double)x * 1.28, 0.0, (double)z * 1.28) * 15.0);
      if (!(e <= 1.8)) {
         double f = 1.17;
         double g = 1.5;
         double h = Math.abs(this.icebergPillarRoofNoise.sample((double)x * 1.17, 0.0, (double)z * 1.17) * 1.5);
         double m = Math.min(e * e * 1.2, Math.ceil(h * 40.0) + 14.0);
         if (biome.shouldGenerateLowerFrozenOceanSurface(mutablePos.set(x, 63, z))) {
            m -= 2.0;
         }

         double n;
         if (m > 2.0) {
            n = (double)this.seaLevel - m - 7.0;
            m += (double)this.seaLevel;
         } else {
            m = 0.0;
            n = 0.0;
         }

         double o = m;
         Random lv = this.randomDeriver.split(x, 0, z);
         int p = 2 + lv.nextInt(4);
         int q = this.seaLevel + 18 + lv.nextInt(10);
         int r = 0;

         for(int s = Math.max(surfaceY, (int)m + 1); s >= minY; --s) {
            if (column.getState(s).isAir() && s < (int)o && lv.nextDouble() > 0.01 || column.getState(s).isOf(Blocks.WATER) && s > (int)n && s < this.seaLevel && n != 0.0 && lv.nextDouble() > 0.15) {
               if (r <= p && s > q) {
                  column.setState(s, SNOW_BLOCK);
                  ++r;
               } else {
                  column.setState(s, PACKED_ICE);
               }
            }
         }

      }
   }

   private static BlockState[] createTerracottaBands(Random random) {
      BlockState[] lvs = new BlockState[192];
      Arrays.fill(lvs, TERRACOTTA);

      int i;
      for(i = 0; i < lvs.length; ++i) {
         i += random.nextInt(5) + 1;
         if (i < lvs.length) {
            lvs[i] = ORANGE_TERRACOTTA;
         }
      }

      addTerracottaBands(random, lvs, 1, YELLOW_TERRACOTTA);
      addTerracottaBands(random, lvs, 2, BROWN_TERRACOTTA);
      addTerracottaBands(random, lvs, 1, RED_TERRACOTTA);
      i = random.nextBetween(9, 15);
      int j = 0;

      for(int k = 0; j < i && k < lvs.length; k += random.nextInt(16) + 4) {
         lvs[k] = WHITE_TERRACOTTA;
         if (k - 1 > 0 && random.nextBoolean()) {
            lvs[k - 1] = LIGHT_GRAY_TERRACOTTA;
         }

         if (k + 1 < lvs.length && random.nextBoolean()) {
            lvs[k + 1] = LIGHT_GRAY_TERRACOTTA;
         }

         ++j;
      }

      return lvs;
   }

   private static void addTerracottaBands(Random random, BlockState[] terracottaBands, int minBandSize, BlockState state) {
      int j = random.nextBetween(6, 15);

      for(int k = 0; k < j; ++k) {
         int l = minBandSize + random.nextInt(3);
         int m = random.nextInt(terracottaBands.length);

         for(int n = 0; m + n < terracottaBands.length && n < l; ++n) {
            terracottaBands[m + n] = state;
         }
      }

   }

   protected BlockState getTerracottaBlock(int x, int y, int z) {
      int l = (int)Math.round(this.terracottaBandsOffsetNoise.sample((double)x, 0.0, (double)z) * 4.0);
      return this.terracottaBands[(y + l + this.terracottaBands.length) % this.terracottaBands.length];
   }

   static {
      WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.getDefaultState();
      ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.getDefaultState();
      TERRACOTTA = Blocks.TERRACOTTA.getDefaultState();
      YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.getDefaultState();
      BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.getDefaultState();
      RED_TERRACOTTA = Blocks.RED_TERRACOTTA.getDefaultState();
      LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.getDefaultState();
      PACKED_ICE = Blocks.PACKED_ICE.getDefaultState();
      SNOW_BLOCK = Blocks.SNOW_BLOCK.getDefaultState();
   }
}
