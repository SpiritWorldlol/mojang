package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class StructureReferenceFix extends DataFix {
   public StructureReferenceFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   protected TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
      return this.fixTypeEverywhereTyped("Structure Reference Fix", type, (typed) -> {
         return typed.update(DSL.remainderFinder(), StructureReferenceFix::updateReferences);
      });
   }

   private static Dynamic updateReferences(Dynamic dynamic) {
      return dynamic.update("references", (dynamicx) -> {
         return dynamicx.createInt((Integer)dynamicx.asNumber().map(Number::intValue).result().filter((integer) -> {
            return integer > 0;
         }).orElse(1));
      });
   }
}
