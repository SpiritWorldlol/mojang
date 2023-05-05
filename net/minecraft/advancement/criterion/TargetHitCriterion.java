package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class TargetHitCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("target_hit");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      NumberRange.IntRange lv = NumberRange.IntRange.fromJson(jsonObject.get("signal_strength"));
      EntityConditions lv2 = EntityPredicate.toConditions(jsonObject, "projectile", arg2);
      return new Conditions(arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, Entity projectile, Vec3d hitPos, int signalStrength) {
      LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, projectile);
      this.trigger(player, (conditions) -> {
         return conditions.test(lv, hitPos, signalStrength);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final NumberRange.IntRange signalStrength;
      private final EntityConditions projectile;

      public Conditions(EntityConditions player, NumberRange.IntRange signalStrength, EntityConditions projectile) {
         super(TargetHitCriterion.ID, player);
         this.signalStrength = signalStrength;
         this.projectile = projectile;
      }

      public static Conditions create(NumberRange.IntRange signalStrength, EntityConditions projectile) {
         return new Conditions(EntityConditions.EMPTY, signalStrength, projectile);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("signal_strength", this.signalStrength.toJson());
         jsonObject.add("projectile", this.projectile.toJson(predicateSerializer));
         return jsonObject;
      }

      public boolean test(LootContext projectileContext, Vec3d hitPos, int signalStrength) {
         if (!this.signalStrength.test(signalStrength)) {
            return false;
         } else {
            return this.projectile.test(projectileContext);
         }
      }
   }
}
