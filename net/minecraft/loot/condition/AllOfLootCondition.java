package net.minecraft.loot.condition;

public class AllOfLootCondition extends AlternativeLootCondition {
   AllOfLootCondition(LootCondition[] terms) {
      super(terms, LootConditionTypes.matchingAll(terms));
   }

   public LootConditionType getType() {
      return LootConditionTypes.ALL_OF;
   }

   public static Builder builder(LootCondition.Builder... builders) {
      return new Builder(builders);
   }

   public static class Builder extends AlternativeLootCondition.Builder {
      public Builder(LootCondition.Builder... args) {
         super(args);
      }

      public Builder and(LootCondition.Builder arg) {
         this.add(arg);
         return this;
      }

      protected LootCondition build(LootCondition[] terms) {
         return new AllOfLootCondition(terms);
      }
   }

   public static class Serializer extends AlternativeLootCondition.Serializer {
      protected AllOfLootCondition fromTerms(LootCondition[] args) {
         return new AllOfLootCondition(args);
      }

      // $FF: synthetic method
      protected AlternativeLootCondition fromTerms(LootCondition[] terms) {
         return this.fromTerms(terms);
      }
   }
}
