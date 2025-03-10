package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class BedItemColorFix extends DataFix {
   public BedItemColorFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      OpticFinder opticFinder = DSL.fieldFinder("id", DSL.named(TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
      return this.fixTypeEverywhereTyped("BedItemColorFix", this.getInputSchema().getType(TypeReferences.ITEM_STACK), (typed) -> {
         Optional optional = typed.getOptional(opticFinder);
         if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:bed")) {
            Dynamic dynamic = (Dynamic)typed.get(DSL.remainderFinder());
            if (dynamic.get("Damage").asInt(0) == 0) {
               return typed.set(DSL.remainderFinder(), dynamic.set("Damage", dynamic.createShort((short)14)));
            }
         }

         return typed;
      });
   }
}
