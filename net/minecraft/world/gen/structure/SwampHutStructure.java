package net.minecraft.world.gen.structure;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.SwampHutGenerator;
import net.minecraft.world.Heightmap;

public class SwampHutStructure extends Structure {
   public static final Codec CODEC = createCodec(SwampHutStructure::new);

   public SwampHutStructure(Structure.Config arg) {
      super(arg);
   }

   public Optional getStructurePosition(Structure.Context context) {
      return getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, (collector) -> {
         addPieces(collector, context);
      });
   }

   private static void addPieces(StructurePiecesCollector collector, Structure.Context context) {
      collector.addPiece(new SwampHutGenerator(context.random(), context.chunkPos().getStartX(), context.chunkPos().getStartZ()));
   }

   public StructureType getType() {
      return StructureType.SWAMP_HUT;
   }
}
