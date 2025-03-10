package net.minecraft.world.gen.structure;

import java.util.Optional;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.Heightmap;

public abstract class BasicTempleStructure extends Structure {
   private final Constructor constructor;
   private final int width;
   private final int height;

   protected BasicTempleStructure(Constructor constructor, int width, int height, Structure.Config config) {
      super(config);
      this.constructor = constructor;
      this.width = width;
      this.height = height;
   }

   public Optional getStructurePosition(Structure.Context context) {
      return getMinCornerHeight(context, this.width, this.height) < context.chunkGenerator().getSeaLevel() ? Optional.empty() : getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, (collector) -> {
         this.addPieces(collector, context);
      });
   }

   private void addPieces(StructurePiecesCollector collector, Structure.Context context) {
      ChunkPos lv = context.chunkPos();
      collector.addPiece(this.constructor.construct(context.random(), lv.getStartX(), lv.getStartZ()));
   }

   @FunctionalInterface
   protected interface Constructor {
      StructurePiece construct(ChunkRandom random, int startX, int startZ);
   }
}
