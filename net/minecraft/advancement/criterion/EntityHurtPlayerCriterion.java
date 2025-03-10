package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.predicate.DamagePredicate;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EntityHurtPlayerCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("entity_hurt_player");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      DamagePredicate lv = DamagePredicate.fromJson(jsonObject.get("damage"));
      return new Conditions(arg, lv);
   }

   public void trigger(ServerPlayerEntity player, DamageSource source, float dealt, float taken, boolean blocked) {
      this.trigger(player, (conditions) -> {
         return conditions.matches(player, source, dealt, taken, blocked);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final DamagePredicate damage;

      public Conditions(EntityConditions player, DamagePredicate damage) {
         super(EntityHurtPlayerCriterion.ID, player);
         this.damage = damage;
      }

      public static Conditions create() {
         return new Conditions(EntityConditions.EMPTY, DamagePredicate.ANY);
      }

      public static Conditions create(DamagePredicate predicate) {
         return new Conditions(EntityConditions.EMPTY, predicate);
      }

      public static Conditions create(DamagePredicate.Builder damageBuilder) {
         return new Conditions(EntityConditions.EMPTY, damageBuilder.build());
      }

      public boolean matches(ServerPlayerEntity player, DamageSource source, float dealt, float taken, boolean blocked) {
         return this.damage.test(player, source, dealt, taken, blocked);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("damage", this.damage.toJson());
         return jsonObject;
      }
   }
}
