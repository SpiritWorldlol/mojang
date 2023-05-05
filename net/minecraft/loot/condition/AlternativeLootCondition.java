package net.minecraft.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public abstract class AlternativeLootCondition implements LootCondition {
   final LootCondition[] terms;
   private final Predicate predicate;

   protected AlternativeLootCondition(LootCondition[] terms, Predicate predicate) {
      this.terms = terms;
      this.predicate = predicate;
   }

   public final boolean test(LootContext arg) {
      return this.predicate.test(arg);
   }

   public void validate(LootTableReporter reporter) {
      LootCondition.super.validate(reporter);

      for(int i = 0; i < this.terms.length; ++i) {
         this.terms[i].validate(reporter.makeChild(".term[" + i + "]"));
      }

   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public abstract static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, AlternativeLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.add("terms", jsonSerializationContext.serialize(arg.terms));
      }

      public AlternativeLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         LootCondition[] lvs = (LootCondition[])JsonHelper.deserialize(jsonObject, "terms", jsonDeserializationContext, LootCondition[].class);
         return this.fromTerms(lvs);
      }

      protected abstract AlternativeLootCondition fromTerms(LootCondition[] terms);

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }

   public abstract static class Builder implements LootCondition.Builder {
      private final List terms = new ArrayList();

      public Builder(LootCondition.Builder... terms) {
         LootCondition.Builder[] var2 = terms;
         int var3 = terms.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            LootCondition.Builder lv = var2[var4];
            this.terms.add(lv.build());
         }

      }

      public void add(LootCondition.Builder builder) {
         this.terms.add(builder.build());
      }

      public LootCondition build() {
         LootCondition[] lvs = (LootCondition[])this.terms.toArray((i) -> {
            return new LootCondition[i];
         });
         return this.build(lvs);
      }

      protected abstract LootCondition build(LootCondition[] terms);
   }
}
