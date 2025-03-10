package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public abstract class ItemNameFix extends DataFix {
   private final String name;

   public ItemNameFix(Schema outputSchema, String name) {
      super(outputSchema, false);
      this.name = name;
   }

   public TypeRewriteRule makeRule() {
      Type type = DSL.named(TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType());
      if (!Objects.equals(this.getInputSchema().getType(TypeReferences.ITEM_NAME), type)) {
         throw new IllegalStateException("item name type is not what was expected.");
      } else {
         return this.fixTypeEverywhere(this.name, type, (dynamicOps) -> {
            return (pair) -> {
               return pair.mapSecond(this::rename);
            };
         });
      }
   }

   protected abstract String rename(String input);

   public static DataFix create(Schema outputSchema, String name, final Function rename) {
      return new ItemNameFix(outputSchema, name) {
         protected String rename(String input) {
            return (String)rename.apply(input);
         }
      };
   }
}
