package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BredAnimalsCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("bred_animals");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      EntityConditions lv = EntityPredicate.toConditions(jsonObject, "parent", arg2);
      EntityConditions lv2 = EntityPredicate.toConditions(jsonObject, "partner", arg2);
      EntityConditions lv3 = EntityPredicate.toConditions(jsonObject, "child", arg2);
      return new Conditions(arg, lv, lv2, lv3);
   }

   public void trigger(ServerPlayerEntity player, AnimalEntity parent, AnimalEntity partner, @Nullable PassiveEntity child) {
      LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, parent);
      LootContext lv2 = EntityPredicate.createAdvancementEntityLootContext(player, partner);
      LootContext lv3 = child != null ? EntityPredicate.createAdvancementEntityLootContext(player, child) : null;
      this.trigger(player, (conditions) -> {
         return conditions.matches(lv, lv2, lv3);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final EntityConditions parent;
      private final EntityConditions partner;
      private final EntityConditions child;

      public Conditions(EntityConditions player, EntityConditions parent, EntityConditions partner, EntityConditions child) {
         super(BredAnimalsCriterion.ID, player);
         this.parent = parent;
         this.partner = partner;
         this.child = child;
      }

      public static Conditions any() {
         return new Conditions(EntityConditions.EMPTY, EntityConditions.EMPTY, EntityConditions.EMPTY, EntityConditions.EMPTY);
      }

      public static Conditions create(EntityPredicate.Builder child) {
         return new Conditions(EntityConditions.EMPTY, EntityConditions.EMPTY, EntityConditions.EMPTY, EntityPredicate.toConditions(child.build()));
      }

      public static Conditions create(EntityPredicate parent, EntityPredicate partner, EntityPredicate child) {
         return new Conditions(EntityConditions.EMPTY, EntityPredicate.toConditions(parent), EntityPredicate.toConditions(partner), EntityPredicate.toConditions(child));
      }

      public boolean matches(LootContext parentContext, LootContext partnerContext, @Nullable LootContext childContext) {
         if (this.child == EntityConditions.EMPTY || childContext != null && this.child.test(childContext)) {
            return this.parent.test(parentContext) && this.partner.test(partnerContext) || this.parent.test(partnerContext) && this.partner.test(parentContext);
         } else {
            return false;
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("parent", this.parent.toJson(predicateSerializer));
         jsonObject.add("partner", this.partner.toJson(predicateSerializer));
         jsonObject.add("child", this.child.toJson(predicateSerializer));
         return jsonObject;
      }
   }
}
