package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class PlayerGeneratesContainerLootCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("player_generates_container_loot");

   public Identifier getId() {
      return ID;
   }

   protected Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "loot_table"));
      return new Conditions(arg, lv);
   }

   public void trigger(ServerPlayerEntity player, Identifier id) {
      this.trigger(player, (conditions) -> {
         return conditions.test(id);
      });
   }

   // $FF: synthetic method
   protected AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final Identifier lootTable;

      public Conditions(EntityConditions entity, Identifier lootTable) {
         super(PlayerGeneratesContainerLootCriterion.ID, entity);
         this.lootTable = lootTable;
      }

      public static Conditions create(Identifier lootTable) {
         return new Conditions(EntityConditions.EMPTY, lootTable);
      }

      public boolean test(Identifier lootTable) {
         return this.lootTable.equals(lootTable);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.addProperty("loot_table", this.lootTable.toString());
         return jsonObject;
      }
   }
}
