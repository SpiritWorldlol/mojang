package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class UsingItemCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("using_item");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      ItemPredicate lv = ItemPredicate.fromJson(jsonObject.get("item"));
      return new Conditions(arg, lv);
   }

   public void trigger(ServerPlayerEntity player, ItemStack stack) {
      this.trigger(player, (conditions) -> {
         return conditions.test(stack);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final ItemPredicate item;

      public Conditions(EntityConditions player, ItemPredicate item) {
         super(UsingItemCriterion.ID, player);
         this.item = item;
      }

      public static Conditions create(EntityPredicate.Builder player, ItemPredicate.Builder item) {
         return new Conditions(EntityPredicate.toConditions(player.build()), item.build());
      }

      public boolean test(ItemStack stack) {
         return this.item.test(stack);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("item", this.item.toJson());
         return jsonObject;
      }
   }
}
