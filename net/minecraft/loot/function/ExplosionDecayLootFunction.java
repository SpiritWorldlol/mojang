package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.random.Random;

public class ExplosionDecayLootFunction extends ConditionalLootFunction {
   ExplosionDecayLootFunction(LootCondition[] args) {
      super(args);
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.EXPLOSION_DECAY;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      Float float_ = (Float)context.get(LootContextParameters.EXPLOSION_RADIUS);
      if (float_ != null) {
         Random lv = context.getRandom();
         float f = 1.0F / float_;
         int i = stack.getCount();
         int j = 0;

         for(int k = 0; k < i; ++k) {
            if (lv.nextFloat() <= f) {
               ++j;
            }
         }

         stack.setCount(j);
      }

      return stack;
   }

   public static ConditionalLootFunction.Builder builder() {
      return builder(ExplosionDecayLootFunction::new);
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public ExplosionDecayLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         return new ExplosionDecayLootFunction(args);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
