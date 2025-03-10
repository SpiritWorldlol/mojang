package net.minecraft.advancement.criterion;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ConsumeItemCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("consume_item");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      return new Conditions(arg, ItemPredicate.fromJson(jsonObject.get("item")));
   }

   public void trigger(ServerPlayerEntity player, ItemStack stack) {
      this.trigger(player, (conditions) -> {
         return conditions.matches(stack);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final ItemPredicate item;

      public Conditions(EntityConditions player, ItemPredicate item) {
         super(ConsumeItemCriterion.ID, player);
         this.item = item;
      }

      public static Conditions any() {
         return new Conditions(EntityConditions.EMPTY, ItemPredicate.ANY);
      }

      public static Conditions predicate(ItemPredicate predicate) {
         return new Conditions(EntityConditions.EMPTY, predicate);
      }

      public static Conditions item(ItemConvertible item) {
         return new Conditions(EntityConditions.EMPTY, new ItemPredicate((TagKey)null, ImmutableSet.of(item.asItem()), NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, EnchantmentPredicate.ARRAY_OF_ANY, EnchantmentPredicate.ARRAY_OF_ANY, (Potion)null, NbtPredicate.ANY));
      }

      public boolean matches(ItemStack stack) {
         return this.item.test(stack);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("item", this.item.toJson());
         return jsonObject;
      }
   }
}
