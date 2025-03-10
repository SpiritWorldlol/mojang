package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Objects;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class BlockNameFlatteningFix extends DataFix {
   public BlockNameFlatteningFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.BLOCK_NAME);
      Type type2 = this.getOutputSchema().getType(TypeReferences.BLOCK_NAME);
      Type type3 = DSL.named(TypeReferences.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), IdentifierNormalizingSchema.getIdentifierType()));
      Type type4 = DSL.named(TypeReferences.BLOCK_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType());
      if (Objects.equals(type, type3) && Objects.equals(type2, type4)) {
         return this.fixTypeEverywhere("BlockNameFlatteningFix", type3, type4, (dynamicOps) -> {
            return (pair) -> {
               return pair.mapSecond((either) -> {
                  return (String)either.map(BlockStateFlattening::lookupStateBlock, (string) -> {
                     return BlockStateFlattening.lookupBlock(IdentifierNormalizingSchema.normalize(string));
                  });
               });
            };
         });
      } else {
         throw new IllegalStateException("Expected and actual types don't match.");
      }
   }
}
