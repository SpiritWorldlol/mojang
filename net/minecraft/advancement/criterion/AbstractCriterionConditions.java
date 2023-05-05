package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.util.Identifier;

public abstract class AbstractCriterionConditions implements CriterionConditions {
   private final Identifier id;
   private final EntityConditions playerPredicate;

   public AbstractCriterionConditions(Identifier id, EntityConditions entity) {
      this.id = id;
      this.playerPredicate = entity;
   }

   public Identifier getId() {
      return this.id;
   }

   protected EntityConditions getPlayerPredicate() {
      return this.playerPredicate;
   }

   public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("player", this.playerPredicate.toJson(predicateSerializer));
      return jsonObject;
   }

   public String toString() {
      return "AbstractCriterionInstance{criterion=" + this.id + "}";
   }
}
