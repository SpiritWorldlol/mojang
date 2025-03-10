package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class OptionsLowerCaseLanguageFix extends DataFix {
   public OptionsLowerCaseLanguageFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("OptionsLowerCaseLanguageFix", this.getInputSchema().getType(TypeReferences.OPTIONS), (typed) -> {
         return typed.update(DSL.remainderFinder(), (dynamic) -> {
            Optional optional = dynamic.get("lang").asString().result();
            return optional.isPresent() ? dynamic.set("lang", dynamic.createString(((String)optional.get()).toLowerCase(Locale.ROOT))) : dynamic;
         });
      });
   }
}
