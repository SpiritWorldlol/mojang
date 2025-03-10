package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class TicksInWrongChunkFix extends DataFix {
   public TicksInWrongChunkFix(Schema schema) {
      super(schema, false);
   }

   protected TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.CHUNK);
      OpticFinder opticFinder = type.findField("block_ticks");
      return this.fixTypeEverywhereTyped("Handle ticks saved in the wrong chunk", type, (typed) -> {
         Optional optional = typed.getOptionalTyped(opticFinder);
         Optional optional2 = optional.isPresent() ? ((Typed)optional.get()).write().result() : Optional.empty();
         return typed.update(DSL.remainderFinder(), (dynamic) -> {
            int i = dynamic.get("xPos").asInt(0);
            int j = dynamic.get("zPos").asInt(0);
            Optional optional2x = dynamic.get("fluid_ticks").get().result();
            dynamic = method_40439(dynamic, i, j, optional2, "neighbor_block_ticks");
            dynamic = method_40439(dynamic, i, j, optional2x, "neighbor_fluid_ticks");
            return dynamic;
         });
      });
   }

   private static Dynamic method_40439(Dynamic dynamic, int i, int j, Optional optional, String string) {
      if (optional.isPresent()) {
         List list = ((Dynamic)optional.get()).asStream().filter((dynamicx) -> {
            int k = dynamicx.get("x").asInt(0);
            int l = dynamicx.get("z").asInt(0);
            int m = Math.abs(i - (k >> 4));
            int n = Math.abs(j - (l >> 4));
            return (m != 0 || n != 0) && m <= 1 && n <= 1;
         }).toList();
         if (!list.isEmpty()) {
            dynamic = dynamic.set("UpgradeData", dynamic.get("UpgradeData").orElseEmptyMap().set(string, dynamic.createList(list.stream())));
         }
      }

      return dynamic;
   }
}
