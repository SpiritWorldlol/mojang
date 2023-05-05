package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PlayerInteractedWithEntityCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("player_interacted_with_entity");

   public Identifier getId() {
      return ID;
   }

   protected Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      ItemPredicate lv = ItemPredicate.fromJson(jsonObject.get("item"));
      EntityConditions lv2 = EntityPredicate.toConditions(jsonObject, "entity", arg2);
      return new Conditions(arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, ItemStack stack, Entity entity) {
      LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, entity);
      this.trigger(player, (conditions) -> {
         return conditions.test(stack, lv);
      });
   }

   // $FF: synthetic method
   protected AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final ItemPredicate item;
      private final EntityConditions entity;

      public Conditions(EntityConditions player, ItemPredicate item, EntityConditions entity) {
         super(PlayerInteractedWithEntityCriterion.ID, player);
         this.item = item;
         this.entity = entity;
      }

      public static Conditions create(EntityConditions player, ItemPredicate.Builder itemBuilder, EntityConditions entity) {
         return new Conditions(player, itemBuilder.build(), entity);
      }

      public static Conditions create(ItemPredicate.Builder itemBuilder, EntityConditions entity) {
         return create(EntityConditions.EMPTY, itemBuilder, entity);
      }

      public boolean test(ItemStack stack, LootContext context) {
         return !this.item.test(stack) ? false : this.entity.test(context);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("item", this.item.toJson());
         jsonObject.add("entity", this.entity.toJson(predicateSerializer));
         return jsonObject;
      }
   }
}
