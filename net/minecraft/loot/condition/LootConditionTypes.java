package net.minecraft.loot.condition;

import java.util.function.Predicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;

public class LootConditionTypes {
   public static final LootConditionType INVERTED = register("inverted", new InvertedLootCondition.Serializer());
   public static final LootConditionType ANY_OF = register("any_of", new AnyOfLootCondition.Serializer());
   public static final LootConditionType ALL_OF = register("all_of", new AllOfLootCondition.Serializer());
   public static final LootConditionType RANDOM_CHANCE = register("random_chance", new RandomChanceLootCondition.Serializer());
   public static final LootConditionType RANDOM_CHANCE_WITH_LOOTING = register("random_chance_with_looting", new RandomChanceWithLootingLootCondition.Serializer());
   public static final LootConditionType ENTITY_PROPERTIES = register("entity_properties", new EntityPropertiesLootCondition.Serializer());
   public static final LootConditionType KILLED_BY_PLAYER = register("killed_by_player", new KilledByPlayerLootCondition.Serializer());
   public static final LootConditionType ENTITY_SCORES = register("entity_scores", new EntityScoresLootCondition.Serializer());
   public static final LootConditionType BLOCK_STATE_PROPERTY = register("block_state_property", new BlockStatePropertyLootCondition.Serializer());
   public static final LootConditionType MATCH_TOOL = register("match_tool", new MatchToolLootCondition.Serializer());
   public static final LootConditionType TABLE_BONUS = register("table_bonus", new TableBonusLootCondition.Serializer());
   public static final LootConditionType SURVIVES_EXPLOSION = register("survives_explosion", new SurvivesExplosionLootCondition.Serializer());
   public static final LootConditionType DAMAGE_SOURCE_PROPERTIES = register("damage_source_properties", new DamageSourcePropertiesLootCondition.Serializer());
   public static final LootConditionType LOCATION_CHECK = register("location_check", new LocationCheckLootCondition.Serializer());
   public static final LootConditionType WEATHER_CHECK = register("weather_check", new WeatherCheckLootCondition.Serializer());
   public static final LootConditionType REFERENCE = register("reference", new ReferenceLootCondition.Serializer());
   public static final LootConditionType TIME_CHECK = register("time_check", new TimeCheckLootCondition.Serializer());
   public static final LootConditionType VALUE_CHECK = register("value_check", new ValueCheckLootCondition.Serializer());

   private static LootConditionType register(String id, JsonSerializer serializer) {
      return (LootConditionType)Registry.register(Registries.LOOT_CONDITION_TYPE, (Identifier)(new Identifier(id)), new LootConditionType(serializer));
   }

   public static Object createGsonSerializer() {
      return JsonSerializing.createSerializerBuilder(Registries.LOOT_CONDITION_TYPE, "condition", "condition", LootCondition::getType).build();
   }

   public static Predicate matchingAll(Predicate[] predicates) {
      Predicate var10000;
      switch (predicates.length) {
         case 0:
            var10000 = (predicatesx) -> {
               return true;
            };
            break;
         case 1:
            var10000 = predicates[0];
            break;
         case 2:
            var10000 = predicates[0].and(predicates[1]);
            break;
         default:
            var10000 = (operand) -> {
               Predicate[] var2 = predicates;
               int var3 = predicates.length;

               for(int var4 = 0; var4 < var3; ++var4) {
                  Predicate predicate = var2[var4];
                  if (!predicate.test(operand)) {
                     return false;
                  }
               }

               return true;
            };
      }

      return var10000;
   }

   public static Predicate matchingAny(Predicate[] predicates) {
      Predicate var10000;
      switch (predicates.length) {
         case 0:
            var10000 = (predicatesx) -> {
               return false;
            };
            break;
         case 1:
            var10000 = predicates[0];
            break;
         case 2:
            var10000 = predicates[0].or(predicates[1]);
            break;
         default:
            var10000 = (operand) -> {
               Predicate[] var2 = predicates;
               int var3 = predicates.length;

               for(int var4 = 0; var4 < var3; ++var4) {
                  Predicate predicate = var2[var4];
                  if (predicate.test(operand)) {
                     return true;
                  }
               }

               return false;
            };
      }

      return var10000;
   }
}
