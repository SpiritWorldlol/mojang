package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class OptionsAddTextBackgroundFix extends DataFix {
   public OptionsAddTextBackgroundFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("OptionsAddTextBackgroundFix", this.getInputSchema().getType(TypeReferences.OPTIONS), (typed) -> {
         return typed.update(DSL.remainderFinder(), (dynamic) -> {
            return (Dynamic)DataFixUtils.orElse(dynamic.get("chatOpacity").asString().map((string) -> {
               return dynamic.set("textBackgroundOpacity", dynamic.createDouble(this.convertToTextBackgroundOpacity(string)));
            }).result(), dynamic);
         });
      });
   }

   private double convertToTextBackgroundOpacity(String chatOpacity) {
      try {
         double d = 0.9 * Double.parseDouble(chatOpacity) + 0.1;
         return d / 2.0;
      } catch (NumberFormatException var4) {
         return 0.5;
      }
   }
}
