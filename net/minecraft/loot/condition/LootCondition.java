package net.minecraft.loot.condition;

import java.util.function.Predicate;
import net.minecraft.loot.context.LootContextAware;

public interface LootCondition extends LootContextAware, Predicate {
   LootConditionType getType();

   @FunctionalInterface
   public interface Builder {
      LootCondition build();

      default Builder invert() {
         return InvertedLootCondition.builder(this);
      }

      default AnyOfLootCondition.Builder or(Builder condition) {
         return AnyOfLootCondition.builder(this, condition);
      }

      default AllOfLootCondition.Builder and(Builder condition) {
         return AllOfLootCondition.builder(this, condition);
      }
   }
}
