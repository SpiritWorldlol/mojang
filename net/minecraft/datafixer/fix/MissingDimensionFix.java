package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class MissingDimensionFix extends DataFix {
   public MissingDimensionFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   protected static Type method_29913(String string, Type type) {
      return DSL.and(DSL.field(string, type), DSL.remainderType());
   }

   protected static Type method_29915(String string, Type type) {
      return DSL.and(DSL.optional(DSL.field(string, type)), DSL.remainderType());
   }

   protected static Type method_29914(String string, Type type, String string2, Type type2) {
      return DSL.and(DSL.optional(DSL.field(string, type)), DSL.optional(DSL.field(string2, type2)), DSL.remainderType());
   }

   protected TypeRewriteRule makeRule() {
      Schema schema = this.getInputSchema();
      Type type = DSL.taggedChoiceType("type", DSL.string(), ImmutableMap.of("minecraft:debug", DSL.remainderType(), "minecraft:flat", method_38820(schema), "minecraft:noise", method_29914("biome_source", DSL.taggedChoiceType("type", DSL.string(), ImmutableMap.of("minecraft:fixed", method_29913("biome", schema.getType(TypeReferences.BIOME)), "minecraft:multi_noise", DSL.list(method_29913("biome", schema.getType(TypeReferences.BIOME))), "minecraft:checkerboard", method_29913("biomes", DSL.list(schema.getType(TypeReferences.BIOME))), "minecraft:vanilla_layered", DSL.remainderType(), "minecraft:the_end", DSL.remainderType())), "settings", DSL.or(DSL.string(), method_29914("default_block", schema.getType(TypeReferences.BLOCK_NAME), "default_fluid", schema.getType(TypeReferences.BLOCK_NAME))))));
      CompoundList.CompoundListType compoundListType = DSL.compoundList(IdentifierNormalizingSchema.getIdentifierType(), method_29913("generator", type));
      Type type2 = DSL.and(compoundListType, DSL.remainderType());
      Type type3 = schema.getType(TypeReferences.WORLD_GEN_SETTINGS);
      FieldFinder fieldFinder = new FieldFinder("dimensions", type2);
      if (!type3.findFieldType("dimensions").equals(type2)) {
         throw new IllegalStateException();
      } else {
         OpticFinder opticFinder = compoundListType.finder();
         return this.fixTypeEverywhereTyped("MissingDimensionFix", type3, (typed) -> {
            return typed.updateTyped(fieldFinder, (typed2) -> {
               return typed2.updateTyped(opticFinder, (typed2x) -> {
                  if (!(typed2x.getValue() instanceof List)) {
                     throw new IllegalStateException("List exptected");
                  } else if (((List)typed2x.getValue()).isEmpty()) {
                     Dynamic dynamic = (Dynamic)typed.get(DSL.remainderFinder());
                     Dynamic dynamic2 = this.method_29912(dynamic);
                     return (Typed)DataFixUtils.orElse(compoundListType.readTyped(dynamic2).result().map(Pair::getFirst), typed2x);
                  } else {
                     return typed2x;
                  }
               });
            });
         });
      }
   }

   protected static Type method_38820(Schema schema) {
      return method_29915("settings", method_29914("biome", schema.getType(TypeReferences.BIOME), "layers", DSL.list(method_29915("block", schema.getType(TypeReferences.BLOCK_NAME)))));
   }

   private Dynamic method_29912(Dynamic dynamic) {
      long l = dynamic.get("seed").asLong(0L);
      return new Dynamic(dynamic.getOps(), StructureSeparationDataFix.method_29917(dynamic, l, StructureSeparationDataFix.method_29916(dynamic, l), false));
   }
}
