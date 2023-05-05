package net.minecraft.advancement.criterion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class RecipeCraftedCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("recipe_crafted");

   public Identifier getId() {
      return ID;
   }

   protected Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "recipe_id"));
      ItemPredicate[] lvs = ItemPredicate.deserializeAll(jsonObject.get("ingredients"));
      return new Conditions(arg, lv, List.of(lvs));
   }

   public void trigger(ServerPlayerEntity player, Identifier recipeId, List ingredients) {
      this.trigger(player, (conditions) -> {
         return conditions.matches(recipeId, ingredients);
      });
   }

   // $FF: synthetic method
   protected AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final Identifier recipeId;
      private final List ingredients;

      public Conditions(EntityPredicate.Extended player, Identifier recipeId, List ingredients) {
         super(RecipeCraftedCriterion.ID, player);
         this.recipeId = recipeId;
         this.ingredients = ingredients;
      }

      public static Conditions create(Identifier recipeId, List ingredients) {
         return new Conditions(EntityPredicate.Extended.EMPTY, recipeId, ingredients);
      }

      public static Conditions create(Identifier recipeId) {
         return new Conditions(EntityPredicate.Extended.EMPTY, recipeId, List.of());
      }

      boolean matches(Identifier recipeId, List ingredients) {
         if (!recipeId.equals(this.recipeId)) {
            return false;
         } else {
            List list2 = new ArrayList(ingredients);
            Iterator var4 = this.ingredients.iterator();

            boolean bl;
            do {
               if (!var4.hasNext()) {
                  return true;
               }

               ItemPredicate lv = (ItemPredicate)var4.next();
               bl = false;
               Iterator iterator = list2.iterator();

               while(iterator.hasNext()) {
                  if (lv.test((ItemStack)iterator.next())) {
                     iterator.remove();
                     bl = true;
                     break;
                  }
               }
            } while(bl);

            return false;
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.addProperty("recipe_id", this.recipeId.toString());
         if (this.ingredients.size() > 0) {
            JsonArray jsonArray = new JsonArray();
            Iterator var4 = this.ingredients.iterator();

            while(var4.hasNext()) {
               ItemPredicate lv = (ItemPredicate)var4.next();
               jsonArray.add(lv.toJson());
            }

            jsonObject.add("ingredients", jsonArray);
         }

         return jsonObject;
      }
   }
}
