package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.datafixer.TypeReferences;

public class OptionsKeyTranslationFix extends DataFix {
   public OptionsKeyTranslationFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("OptionsKeyTranslationFix", this.getInputSchema().getType(TypeReferences.OPTIONS), (typed) -> {
         return typed.update(DSL.remainderFinder(), (dynamic) -> {
            return (Dynamic)dynamic.getMapValues().map((map) -> {
               return dynamic.createMap((Map)map.entrySet().stream().map((entry) -> {
                  if (((Dynamic)entry.getKey()).asString("").startsWith("key_")) {
                     String string = ((Dynamic)entry.getValue()).asString("");
                     if (!string.startsWith("key.mouse") && !string.startsWith("scancode.")) {
                        return Pair.of((Dynamic)entry.getKey(), dynamic.createString("key.keyboard." + string.substring("key.".length())));
                     }
                  }

                  return Pair.of((Dynamic)entry.getKey(), (Dynamic)entry.getValue());
               }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
            }).result().orElse(dynamic);
         });
      });
   }
}
