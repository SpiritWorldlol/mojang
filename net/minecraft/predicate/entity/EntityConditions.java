package net.minecraft.predicate.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.function.Predicate;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextType;
import org.jetbrains.annotations.Nullable;

public class EntityConditions {
   public static final EntityConditions EMPTY = new EntityConditions(new LootCondition[0]);
   private final LootCondition[] conditions;
   private final Predicate combinedCondition;

   EntityConditions(LootCondition[] conditions) {
      this.conditions = conditions;
      this.combinedCondition = LootConditionTypes.matchingAll(conditions);
   }

   public static EntityConditions create(LootCondition... conditions) {
      return new EntityConditions(conditions);
   }

   @Nullable
   public static EntityConditions fromJson(String key, AdvancementEntityPredicateDeserializer predicateDeserializer, @Nullable JsonElement json, LootContextType contextType) {
      if (json != null && json.isJsonArray()) {
         LootCondition[] lvs = predicateDeserializer.loadConditions(json.getAsJsonArray(), predicateDeserializer.getAdvancementId() + "/" + key, contextType);
         return new EntityConditions(lvs);
      } else {
         return null;
      }
   }

   public boolean test(LootContext context) {
      return this.combinedCondition.test(context);
   }

   public JsonElement toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
      return (JsonElement)(this.conditions.length == 0 ? JsonNull.INSTANCE : predicateSerializer.conditionsToJson(this.conditions));
   }

   public static JsonElement toPredicatesJsonArray(EntityConditions[] predicates, AdvancementEntityPredicateSerializer predicateSerializer) {
      if (predicates.length == 0) {
         return JsonNull.INSTANCE;
      } else {
         JsonArray jsonArray = new JsonArray();
         EntityConditions[] var3 = predicates;
         int var4 = predicates.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            EntityConditions lv = var3[var5];
            jsonArray.add(lv.toJson(predicateSerializer));
         }

         return jsonArray;
      }
   }
}
