package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import net.minecraft.datafixer.TypeReferences;

public class Schema1451v1 extends IdentifierNormalizingSchema {
   public Schema1451v1(int i, Schema schema) {
      super(i, schema);
   }

   public void registerTypes(Schema schema, Map entityTypes, Map blockEntityTypes) {
      super.registerTypes(schema, entityTypes, blockEntityTypes);
      schema.registerType(false, TypeReferences.CHUNK, () -> {
         return DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(TypeReferences.ENTITY_TREE.in(schema)), "TileEntities", DSL.list(DSL.or(TypeReferences.BLOCK_ENTITY.in(schema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", TypeReferences.BLOCK_NAME.in(schema))), "Sections", DSL.list(DSL.optionalFields("Palette", DSL.list(TypeReferences.BLOCK_STATE.in(schema))))));
      });
   }
}
