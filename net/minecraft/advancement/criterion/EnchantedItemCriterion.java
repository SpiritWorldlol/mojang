package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EnchantedItemCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("enchanted_item");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      ItemPredicate lv = ItemPredicate.fromJson(jsonObject.get("item"));
      NumberRange.IntRange lv2 = NumberRange.IntRange.fromJson(jsonObject.get("levels"));
      return new Conditions(arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, ItemStack stack, int levels) {
      this.trigger(player, (conditions) -> {
         return conditions.matches(stack, levels);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final ItemPredicate item;
      private final NumberRange.IntRange levels;

      public Conditions(EntityConditions player, ItemPredicate item, NumberRange.IntRange levels) {
         super(EnchantedItemCriterion.ID, player);
         this.item = item;
         this.levels = levels;
      }

      public static Conditions any() {
         return new Conditions(EntityConditions.EMPTY, ItemPredicate.ANY, NumberRange.IntRange.ANY);
      }

      public boolean matches(ItemStack stack, int levels) {
         if (!this.item.test(stack)) {
            return false;
         } else {
            return this.levels.test(levels);
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("item", this.item.toJson());
         jsonObject.add("levels", this.levels.toJson());
         return jsonObject;
      }
   }
}
