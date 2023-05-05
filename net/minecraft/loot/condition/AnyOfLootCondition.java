package net.minecraft.loot.condition;

public class AnyOfLootCondition extends AlternativeLootCondition {
   AnyOfLootCondition(LootCondition[] terms) {
      super(terms, LootConditionTypes.matchingAny(terms));
   }

   public LootConditionType getType() {
      return LootConditionTypes.ANY_OF;
   }

   public static Builder builder(LootCondition.Builder... builders) {
      return new Builder(builders);
   }

   public static class Builder extends AlternativeLootCondition.Builder {
      public Builder(LootCondition.Builder... args) {
         super(args);
      }

      public Builder or(LootCondition.Builder condition) {
         this.add(condition);
         return this;
      }

      protected LootCondition build(LootCondition[] terms) {
         return new AnyOfLootCondition(terms);
      }
   }

   public static class Serializer extends AlternativeLootCondition.Serializer {
      protected AnyOfLootCondition fromTerms(LootCondition[] args) {
         return new AnyOfLootCondition(args);
      }

      // $FF: synthetic method
      protected AlternativeLootCondition fromTerms(LootCondition[] terms) {
         return this.fromTerms(terms);
      }
   }
}
