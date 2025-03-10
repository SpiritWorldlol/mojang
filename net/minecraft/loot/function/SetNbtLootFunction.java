package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.JsonHelper;

public class SetNbtLootFunction extends ConditionalLootFunction {
   final NbtCompound nbt;

   SetNbtLootFunction(LootCondition[] conditions, NbtCompound nbt) {
      super(conditions);
      this.nbt = nbt;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_NBT;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      stack.getOrCreateNbt().copyFrom(this.nbt);
      return stack;
   }

   /** @deprecated */
   @Deprecated
   public static ConditionalLootFunction.Builder builder(NbtCompound nbt) {
      return builder((conditions) -> {
         return new SetNbtLootFunction(conditions, nbt);
      });
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetNbtLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.addProperty("tag", arg.nbt.toString());
      }

      public SetNbtLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         try {
            NbtCompound lv = StringNbtReader.parse(JsonHelper.getString(jsonObject, "tag"));
            return new SetNbtLootFunction(args, lv);
         } catch (CommandSyntaxException var5) {
            throw new JsonSyntaxException(var5.getMessage());
         }
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
