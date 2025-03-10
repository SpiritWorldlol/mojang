package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.text.Text;

public class EntityCustomNameToTextFix extends DataFix {
   public EntityCustomNameToTextFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      OpticFinder opticFinder = DSL.fieldFinder("id", IdentifierNormalizingSchema.getIdentifierType());
      return this.fixTypeEverywhereTyped("EntityCustomNameToComponentFix", this.getInputSchema().getType(TypeReferences.ENTITY), (typed) -> {
         return typed.update(DSL.remainderFinder(), (dynamic) -> {
            Optional optional = typed.getOptional(opticFinder);
            return optional.isPresent() && Objects.equals(optional.get(), "minecraft:commandblock_minecart") ? dynamic : fixCustomName(dynamic);
         });
      });
   }

   public static Dynamic fixCustomName(Dynamic dynamic) {
      String string = dynamic.get("CustomName").asString("");
      return string.isEmpty() ? dynamic.remove("CustomName") : dynamic.set("CustomName", dynamic.createString(Text.Serializer.toJson(Text.literal(string))));
   }
}
