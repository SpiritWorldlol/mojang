package net.minecraft.world.chunk.light;

import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public final class ChunkSkyLightProvider extends ChunkLightProvider {
   private static final long field_44743 = ChunkLightProvider.class_8531.packWithAllDirectionsSet(15);
   private static final long field_44744;
   private static final long field_44745;
   private final BlockPos.Mutable field_44746;
   private final ChunkSkyLight field_44747;

   public ChunkSkyLightProvider(ChunkProvider chunkProvider) {
      this(chunkProvider, new SkyLightStorage(chunkProvider));
   }

   @VisibleForTesting
   protected ChunkSkyLightProvider(ChunkProvider chunkProvider, SkyLightStorage lightStorage) {
      super(chunkProvider, lightStorage);
      this.field_44746 = new BlockPos.Mutable();
      this.field_44747 = new ChunkSkyLight(chunkProvider.getWorld());
   }

   private static boolean method_51584(int i) {
      return i == 15;
   }

   private int method_51585(int x, int z, int k) {
      ChunkSkyLight lv = this.method_51589(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
      return lv == null ? k : lv.get(ChunkSectionPos.getLocalCoord(x), ChunkSectionPos.getLocalCoord(z));
   }

   @Nullable
   private ChunkSkyLight method_51589(int chunkX, int chunkZ) {
      LightSourceView lv = this.chunkProvider.getChunk(chunkX, chunkZ);
      return lv != null ? lv.getChunkSkyLight() : null;
   }

   protected void method_51529(long l) {
      int i = BlockPos.unpackLongX(l);
      int j = BlockPos.unpackLongY(l);
      int k = BlockPos.unpackLongZ(l);
      long m = ChunkSectionPos.fromBlockPos(l);
      int n = ((SkyLightStorage)this.lightStorage).isSectionInEnabledColumn(m) ? this.method_51585(i, k, Integer.MAX_VALUE) : Integer.MAX_VALUE;
      if (n != Integer.MAX_VALUE) {
         this.method_51590(i, k, n);
      }

      if (((SkyLightStorage)this.lightStorage).hasSection(m)) {
         boolean bl = j >= n;
         if (bl) {
            this.method_51565(l, field_44744);
            this.method_51566(l, field_44745);
         } else {
            int o = ((SkyLightStorage)this.lightStorage).get(l);
            if (o > 0) {
               ((SkyLightStorage)this.lightStorage).set(l, 0);
               this.method_51565(l, ChunkLightProvider.class_8531.packWithAllDirectionsSet(o));
            } else {
               this.method_51565(l, field_44731);
            }
         }

      }
   }

   private void method_51590(int i, int j, int k) {
      int l = ChunkSectionPos.getBlockCoord(((SkyLightStorage)this.lightStorage).getMinSectionY());
      this.method_51586(i, j, k, l);
      this.method_51591(i, j, k, l);
   }

   private void method_51586(int x, int z, int k, int l) {
      if (k > l) {
         int m = ChunkSectionPos.getSectionCoord(x);
         int n = ChunkSectionPos.getSectionCoord(z);
         int o = k - 1;

         for(int p = ChunkSectionPos.getSectionCoord(o); ((SkyLightStorage)this.lightStorage).isAboveMinHeight(p); --p) {
            if (((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(m, p, n))) {
               int q = ChunkSectionPos.getBlockCoord(p);
               int r = q + 15;

               for(int s = Math.min(r, o); s >= q; --s) {
                  long t = BlockPos.asLong(x, s, z);
                  if (!method_51584(((SkyLightStorage)this.lightStorage).get(t))) {
                     return;
                  }

                  ((SkyLightStorage)this.lightStorage).set(t, 0);
                  this.method_51565(t, s == k - 1 ? field_44743 : field_44744);
               }
            }
         }

      }
   }

   private void method_51591(int i, int j, int k, int l) {
      int m = ChunkSectionPos.getSectionCoord(i);
      int n = ChunkSectionPos.getSectionCoord(j);
      int o = Math.max(Math.max(this.method_51585(i - 1, j, Integer.MIN_VALUE), this.method_51585(i + 1, j, Integer.MIN_VALUE)), Math.max(this.method_51585(i, j - 1, Integer.MIN_VALUE), this.method_51585(i, j + 1, Integer.MIN_VALUE)));
      int p = Math.max(k, l);

      for(long q = ChunkSectionPos.asLong(m, ChunkSectionPos.getSectionCoord(p), n); !((SkyLightStorage)this.lightStorage).isAtOrAboveTopmostSection(q); q = ChunkSectionPos.offset(q, Direction.UP)) {
         if (((SkyLightStorage)this.lightStorage).hasSection(q)) {
            int r = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(q));
            int s = r + 15;

            for(int t = Math.max(r, p); t <= s; ++t) {
               long u = BlockPos.asLong(i, t, j);
               if (method_51584(((SkyLightStorage)this.lightStorage).get(u))) {
                  return;
               }

               ((SkyLightStorage)this.lightStorage).set(u, 15);
               if (t < o || t == k) {
                  this.method_51566(u, field_44745);
               }
            }
         }
      }

   }

   protected void method_51531(long l, long m, int i) {
      BlockState lv = null;
      int j = this.getNumberOfSectionsBelowPos(l);
      Direction[] var8 = DIRECTIONS;
      int var9 = var8.length;

      for(int var10 = 0; var10 < var9; ++var10) {
         Direction lv2 = var8[var10];
         if (ChunkLightProvider.class_8531.isDirectionBitSet(m, lv2)) {
            long n = BlockPos.offset(l, lv2);
            if (((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n))) {
               int k = ((SkyLightStorage)this.lightStorage).get(n);
               int o = i - 1;
               if (o > k) {
                  this.field_44746.set(n);
                  BlockState lv3 = this.getStateForLighting(this.field_44746);
                  int p = i - this.getOpacity(lv3, this.field_44746);
                  if (p > k) {
                     if (lv == null) {
                        lv = ChunkLightProvider.class_8531.isTrivial(m) ? Blocks.AIR.getDefaultState() : this.getStateForLighting(this.field_44746.set(l));
                     }

                     if (!this.shapesCoverFullCube(l, lv, n, lv3, lv2)) {
                        ((SkyLightStorage)this.lightStorage).set(n, p);
                        if (p > 1) {
                           this.method_51566(n, ChunkLightProvider.class_8531.method_51574(p, isTrivialForLighting(lv3), lv2.getOpposite()));
                        }

                        this.method_51587(n, lv2, p, true, j);
                     }
                  }
               }
            }
         }
      }

   }

   protected void method_51530(long l, long m) {
      int i = this.getNumberOfSectionsBelowPos(l);
      int j = ChunkLightProvider.class_8531.getLightLevel(m);
      Direction[] var7 = DIRECTIONS;
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         Direction lv = var7[var9];
         if (ChunkLightProvider.class_8531.isDirectionBitSet(m, lv)) {
            long n = BlockPos.offset(l, lv);
            if (((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(n))) {
               int k = ((SkyLightStorage)this.lightStorage).get(n);
               if (k != 0) {
                  if (k <= j - 1) {
                     ((SkyLightStorage)this.lightStorage).set(n, 0);
                     this.method_51565(n, ChunkLightProvider.class_8531.packWithOneDirectionCleared(k, lv.getOpposite()));
                     this.method_51587(n, lv, k, false, i);
                  } else {
                     this.method_51566(n, ChunkLightProvider.class_8531.method_51579(k, false, lv.getOpposite()));
                  }
               }
            }
         }
      }

   }

   private int getNumberOfSectionsBelowPos(long blockPos) {
      int i = BlockPos.unpackLongY(blockPos);
      int j = ChunkSectionPos.getLocalCoord(i);
      if (j != 0) {
         return 0;
      } else {
         int k = BlockPos.unpackLongX(blockPos);
         int m = BlockPos.unpackLongZ(blockPos);
         int n = ChunkSectionPos.getLocalCoord(k);
         int o = ChunkSectionPos.getLocalCoord(m);
         if (n != 0 && n != 15 && o != 0 && o != 15) {
            return 0;
         } else {
            int p = ChunkSectionPos.getSectionCoord(k);
            int q = ChunkSectionPos.getSectionCoord(i);
            int r = ChunkSectionPos.getSectionCoord(m);

            int s;
            for(s = 0; !((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(p, q - s - 1, r)) && ((SkyLightStorage)this.lightStorage).isAboveMinHeight(q - s - 1); ++s) {
            }

            return s;
         }
      }
   }

   private void method_51587(long blockPos, Direction direction, int lightLevel, boolean bl, int j) {
      if (j != 0) {
         int k = BlockPos.unpackLongX(blockPos);
         int m = BlockPos.unpackLongZ(blockPos);
         if (exitsChunkXZ(direction, ChunkSectionPos.getLocalCoord(k), ChunkSectionPos.getLocalCoord(m))) {
            int n = BlockPos.unpackLongY(blockPos);
            int o = ChunkSectionPos.getSectionCoord(k);
            int p = ChunkSectionPos.getSectionCoord(m);
            int q = ChunkSectionPos.getSectionCoord(n) - 1;
            int r = q - j + 1;

            while(true) {
               while(q >= r) {
                  if (!((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(o, q, p))) {
                     --q;
                  } else {
                     int s = ChunkSectionPos.getBlockCoord(q);

                     for(int t = 15; t >= 0; --t) {
                        long u = BlockPos.asLong(k, s + t, m);
                        if (bl) {
                           ((SkyLightStorage)this.lightStorage).set(u, lightLevel);
                           if (lightLevel > 1) {
                              this.method_51566(u, ChunkLightProvider.class_8531.method_51574(lightLevel, true, direction.getOpposite()));
                           }
                        } else {
                           ((SkyLightStorage)this.lightStorage).set(u, 0);
                           this.method_51565(u, ChunkLightProvider.class_8531.packWithOneDirectionCleared(lightLevel, direction.getOpposite()));
                        }
                     }

                     --q;
                  }
               }

               return;
            }
         }
      }
   }

   private static boolean exitsChunkXZ(Direction direction, int localX, int localZ) {
      boolean var10000;
      switch (direction) {
         case NORTH:
            var10000 = localZ == 15;
            break;
         case SOUTH:
            var10000 = localZ == 0;
            break;
         case WEST:
            var10000 = localX == 15;
            break;
         case EAST:
            var10000 = localX == 0;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   public void setColumnEnabled(ChunkPos pos, boolean retainData) {
      super.setColumnEnabled(pos, retainData);
      if (retainData) {
         ChunkSkyLight lv = (ChunkSkyLight)Objects.requireNonNullElse(this.method_51589(pos.x, pos.z), this.field_44747);
         int i = ChunkSectionPos.getSectionCoord(lv.getMaxSurfaceY());
         long l = ChunkSectionPos.withZeroY(pos.x, pos.z);
         int j = ((SkyLightStorage)this.lightStorage).getTopSectionForColumn(l);
         int k = Math.max(((SkyLightStorage)this.lightStorage).getMinSectionY(), i);

         for(int m = j - 1; m >= k; --m) {
            ChunkNibbleArray lv2 = ((SkyLightStorage)this.lightStorage).method_51547(ChunkSectionPos.asLong(pos.x, m, pos.z));
            if (lv2 != null && lv2.isUninitialized()) {
               lv2.clear(15);
            }
         }
      }

   }

   public void propagateLight(ChunkPos chunkPos) {
      long l = ChunkSectionPos.withZeroY(chunkPos.x, chunkPos.z);
      ((SkyLightStorage)this.lightStorage).setColumnEnabled(l, true);
      ChunkSkyLight lv = (ChunkSkyLight)Objects.requireNonNullElse(this.method_51589(chunkPos.x, chunkPos.z), this.field_44747);
      ChunkSkyLight lv2 = (ChunkSkyLight)Objects.requireNonNullElse(this.method_51589(chunkPos.x, chunkPos.z - 1), this.field_44747);
      ChunkSkyLight lv3 = (ChunkSkyLight)Objects.requireNonNullElse(this.method_51589(chunkPos.x, chunkPos.z + 1), this.field_44747);
      ChunkSkyLight lv4 = (ChunkSkyLight)Objects.requireNonNullElse(this.method_51589(chunkPos.x - 1, chunkPos.z), this.field_44747);
      ChunkSkyLight lv5 = (ChunkSkyLight)Objects.requireNonNullElse(this.method_51589(chunkPos.x + 1, chunkPos.z), this.field_44747);
      int i = ((SkyLightStorage)this.lightStorage).getTopSectionForColumn(l);
      int j = ((SkyLightStorage)this.lightStorage).getMinSectionY();
      int k = ChunkSectionPos.getBlockCoord(chunkPos.x);
      int m = ChunkSectionPos.getBlockCoord(chunkPos.z);

      for(int n = i - 1; n >= j; --n) {
         long o = ChunkSectionPos.asLong(chunkPos.x, n, chunkPos.z);
         ChunkNibbleArray lv6 = ((SkyLightStorage)this.lightStorage).method_51547(o);
         if (lv6 != null) {
            int p = ChunkSectionPos.getBlockCoord(n);
            int q = p + 15;
            boolean bl = false;

            for(int r = 0; r < 16; ++r) {
               for(int s = 0; s < 16; ++s) {
                  int t = lv.get(s, r);
                  if (t <= q) {
                     int u = r == 0 ? lv2.get(s, 15) : lv.get(s, r - 1);
                     int v = r == 15 ? lv3.get(s, 0) : lv.get(s, r + 1);
                     int w = s == 0 ? lv4.get(15, r) : lv.get(s - 1, r);
                     int x = s == 15 ? lv5.get(0, r) : lv.get(s + 1, r);
                     int y = Math.max(Math.max(u, v), Math.max(w, x));

                     for(int z = q; z >= Math.max(p, t); --z) {
                        lv6.set(s, ChunkSectionPos.getLocalCoord(z), r, 15);
                        if (z == t || z < y) {
                           long aa = BlockPos.asLong(k + s, z, m + r);
                           this.method_51566(aa, ChunkLightProvider.class_8531.method_51578(z == t, z < u, z < v, z < w, z < x));
                        }
                     }

                     if (t < p) {
                        bl = true;
                     }
                  }
               }
            }

            if (!bl) {
               break;
            }
         }
      }

   }

   static {
      field_44744 = ChunkLightProvider.class_8531.packWithOneDirectionCleared(15, Direction.UP);
      field_44745 = ChunkLightProvider.class_8531.method_51574(15, false, Direction.UP);
   }
}
