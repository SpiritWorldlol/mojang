package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.text.Text;

public class TeamDisplayNameFix extends DataFix {
   public TeamDisplayNameFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   protected TypeRewriteRule makeRule() {
      Type type = DSL.named(TypeReferences.TEAM.typeName(), DSL.remainderType());
      if (!Objects.equals(type, this.getInputSchema().getType(TypeReferences.TEAM))) {
         throw new IllegalStateException("Team type is not what was expected.");
      } else {
         return this.fixTypeEverywhere("TeamDisplayNameFix", type, (dynamicOps) -> {
            return (pair) -> {
               return pair.mapSecond((dynamic) -> {
                  return dynamic.update("DisplayName", (dynamic2) -> {
                     DataResult var10000 = dynamic2.asString().map((string) -> {
                        return Text.Serializer.toJson(Text.literal(string));
                     });
                     Objects.requireNonNull(dynamic);
                     return (Dynamic)DataFixUtils.orElse(var10000.map(dynamic::createString).result(), dynamic2);
                  });
               });
            };
         });
      }
   }
}
