package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class OptionFix extends DataFix {
   private final String name;
   private final String oldName;
   private final String newName;

   public OptionFix(Schema outputSchema, boolean changesType, String name, String oldName, String newName) {
      super(outputSchema, changesType);
      this.name = name;
      this.oldName = oldName;
      this.newName = newName;
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(TypeReferences.OPTIONS), (typed) -> {
         return typed.update(DSL.remainderFinder(), (dynamic) -> {
            return (Dynamic)DataFixUtils.orElse(dynamic.get(this.oldName).result().map((dynamic2) -> {
               return dynamic.set(this.newName, dynamic2).remove(this.oldName);
            }), dynamic);
         });
      });
   }
}
