package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.LightningEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class LightningStrikeCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("lightning_strike");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      EntityConditions lv = EntityPredicate.toConditions(jsonObject, "lightning", arg2);
      EntityConditions lv2 = EntityPredicate.toConditions(jsonObject, "bystander", arg2);
      return new Conditions(arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, LightningEntity lightning, List bystanders) {
      List list2 = (List)bystanders.stream().map((bystander) -> {
         return EntityPredicate.createAdvancementEntityLootContext(player, bystander);
      }).collect(Collectors.toList());
      LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, lightning);
      this.trigger(player, (conditions) -> {
         return conditions.test(lv, list2);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final EntityConditions lightning;
      private final EntityConditions bystander;

      public Conditions(EntityConditions player, EntityConditions lightning, EntityConditions bystander) {
         super(LightningStrikeCriterion.ID, player);
         this.lightning = lightning;
         this.bystander = bystander;
      }

      public static Conditions create(EntityPredicate lightning, EntityPredicate bystander) {
         return new Conditions(EntityConditions.EMPTY, EntityPredicate.toConditions(lightning), EntityPredicate.toConditions(bystander));
      }

      public boolean test(LootContext lightning, List bystanders) {
         if (!this.lightning.test(lightning)) {
            return false;
         } else {
            if (this.bystander != EntityConditions.EMPTY) {
               Stream var10000 = bystanders.stream();
               EntityConditions var10001 = this.bystander;
               Objects.requireNonNull(var10001);
               if (var10000.noneMatch(var10001::test)) {
                  return false;
               }
            }

            return true;
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("lightning", this.lightning.toJson(predicateSerializer));
         jsonObject.add("bystander", this.bystander.toJson(predicateSerializer));
         return jsonObject;
      }
   }
}
