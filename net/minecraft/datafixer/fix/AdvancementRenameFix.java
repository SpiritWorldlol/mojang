package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import net.minecraft.datafixer.TypeReferences;

public class AdvancementRenameFix extends DataFix {
   private final String name;
   private final Function renamer;

   public AdvancementRenameFix(Schema outputSchema, boolean changesType, String name, Function renamer) {
      super(outputSchema, changesType);
      this.name = name;
      this.renamer = renamer;
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(TypeReferences.ADVANCEMENTS), (typed) -> {
         return typed.update(DSL.remainderFinder(), (dynamic) -> {
            return dynamic.updateMapValues((pair) -> {
               String string = ((Dynamic)pair.getFirst()).asString("");
               return pair.mapFirst((dynamic2) -> {
                  return dynamic.createString((String)this.renamer.apply(string));
               });
            });
         });
      });
   }
}
