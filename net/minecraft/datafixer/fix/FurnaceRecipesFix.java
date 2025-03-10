package net.minecraft.datafixer.fix;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class FurnaceRecipesFix extends DataFix {
   public FurnaceRecipesFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   protected TypeRewriteRule makeRule() {
      return this.updateBlockEntities(this.getOutputSchema().getTypeRaw(TypeReferences.RECIPE));
   }

   private TypeRewriteRule updateBlockEntities(Type type) {
      Type type2 = DSL.and(DSL.optional(DSL.field("RecipesUsed", DSL.and(DSL.compoundList(type, DSL.intType()), DSL.remainderType()))), DSL.remainderType());
      OpticFinder opticFinder = DSL.namedChoice("minecraft:furnace", this.getInputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:furnace"));
      OpticFinder opticFinder2 = DSL.namedChoice("minecraft:blast_furnace", this.getInputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:blast_furnace"));
      OpticFinder opticFinder3 = DSL.namedChoice("minecraft:smoker", this.getInputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:smoker"));
      Type type3 = this.getOutputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:furnace");
      Type type4 = this.getOutputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:blast_furnace");
      Type type5 = this.getOutputSchema().getChoiceType(TypeReferences.BLOCK_ENTITY, "minecraft:smoker");
      Type type6 = this.getInputSchema().getType(TypeReferences.BLOCK_ENTITY);
      Type type7 = this.getOutputSchema().getType(TypeReferences.BLOCK_ENTITY);
      return this.fixTypeEverywhereTyped("FurnaceRecipesFix", type6, type7, (typed) -> {
         return typed.updateTyped(opticFinder, type3, (typedx) -> {
            return this.updateBlockEntityData(type, type2, typedx);
         }).updateTyped(opticFinder2, type4, (typedx) -> {
            return this.updateBlockEntityData(type, type2, typedx);
         }).updateTyped(opticFinder3, type5, (typedx) -> {
            return this.updateBlockEntityData(type, type2, typedx);
         });
      });
   }

   private Typed updateBlockEntityData(Type type, Type type2, Typed typed) {
      Dynamic dynamic = (Dynamic)typed.getOrCreate(DSL.remainderFinder());
      int i = dynamic.get("RecipesUsedSize").asInt(0);
      dynamic = dynamic.remove("RecipesUsedSize");
      List list = Lists.newArrayList();

      for(int j = 0; j < i; ++j) {
         String string = "RecipeLocation" + j;
         String string2 = "RecipeAmount" + j;
         Optional optional = dynamic.get(string).result();
         int k = dynamic.get(string2).asInt(0);
         if (k > 0) {
            optional.ifPresent((dynamicx) -> {
               Optional optional = type.read(dynamicx).result();
               optional.ifPresent((pair) -> {
                  list.add(Pair.of(pair.getFirst(), k));
               });
            });
         }

         dynamic = dynamic.remove(string).remove(string2);
      }

      return typed.set(DSL.remainderFinder(), type2, Pair.of(Either.left(Pair.of(list, dynamic.emptyMap())), dynamic));
   }
}
