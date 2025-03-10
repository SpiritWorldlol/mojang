package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class RandomChanceWithLootingLootCondition implements LootCondition {
   final float chance;
   final float lootingMultiplier;

   RandomChanceWithLootingLootCondition(float chance, float lootingMultiplier) {
      this.chance = chance;
      this.lootingMultiplier = lootingMultiplier;
   }

   public LootConditionType getType() {
      return LootConditionTypes.RANDOM_CHANCE_WITH_LOOTING;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.KILLER_ENTITY);
   }

   public boolean test(LootContext arg) {
      Entity lv = (Entity)arg.get(LootContextParameters.KILLER_ENTITY);
      int i = 0;
      if (lv instanceof LivingEntity) {
         i = EnchantmentHelper.getLooting((LivingEntity)lv);
      }

      return arg.getRandom().nextFloat() < this.chance + (float)i * this.lootingMultiplier;
   }

   public static LootCondition.Builder builder(float chance, float lootingMultiplier) {
      return () -> {
         return new RandomChanceWithLootingLootCondition(chance, lootingMultiplier);
      };
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, RandomChanceWithLootingLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("chance", arg.chance);
         jsonObject.addProperty("looting_multiplier", arg.lootingMultiplier);
      }

      public RandomChanceWithLootingLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         return new RandomChanceWithLootingLootCondition(JsonHelper.getFloat(jsonObject, "chance"), JsonHelper.getFloat(jsonObject, "looting_multiplier"));
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
