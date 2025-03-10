package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import net.minecraft.datafixer.TypeReferences;

public class Schema2842 extends IdentifierNormalizingSchema {
   public Schema2842(int i, Schema schema) {
      super(i, schema);
   }

   public void registerTypes(Schema schema, Map entityTypes, Map blockEntityTypes) {
      super.registerTypes(schema, entityTypes, blockEntityTypes);
      schema.registerType(false, TypeReferences.CHUNK, () -> {
         return DSL.optionalFields("entities", DSL.list(TypeReferences.ENTITY_TREE.in(schema)), "block_entities", DSL.list(DSL.or(TypeReferences.BLOCK_ENTITY.in(schema), DSL.remainder())), "block_ticks", DSL.list(DSL.fields("i", TypeReferences.BLOCK_NAME.in(schema))), "sections", DSL.list(DSL.optionalFields("biomes", DSL.optionalFields("palette", DSL.list(TypeReferences.BIOME.in(schema))), "block_states", DSL.optionalFields("palette", DSL.list(TypeReferences.BLOCK_STATE.in(schema))))), "structures", DSL.optionalFields("starts", DSL.compoundList(TypeReferences.STRUCTURE_FEATURE.in(schema))));
      });
   }
}
