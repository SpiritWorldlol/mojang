package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class BeeNestDestroyedCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("bee_nest_destroyed");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      Block lv = getBlock(jsonObject);
      ItemPredicate lv2 = ItemPredicate.fromJson(jsonObject.get("item"));
      NumberRange.IntRange lv3 = NumberRange.IntRange.fromJson(jsonObject.get("num_bees_inside"));
      return new Conditions(arg, lv, lv2, lv3);
   }

   @Nullable
   private static Block getBlock(JsonObject root) {
      if (root.has("block")) {
         Identifier lv = new Identifier(JsonHelper.getString(root, "block"));
         return (Block)Registries.BLOCK.getOrEmpty(lv).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block type '" + lv + "'");
         });
      } else {
         return null;
      }
   }

   public void trigger(ServerPlayerEntity player, BlockState state, ItemStack stack, int beeCount) {
      this.trigger(player, (conditions) -> {
         return conditions.test(state, stack, beeCount);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      @Nullable
      private final Block block;
      private final ItemPredicate item;
      private final NumberRange.IntRange beeCount;

      public Conditions(EntityConditions player, @Nullable Block block, ItemPredicate item, NumberRange.IntRange beeCount) {
         super(BeeNestDestroyedCriterion.ID, player);
         this.block = block;
         this.item = item;
         this.beeCount = beeCount;
      }

      public static Conditions create(Block block, ItemPredicate.Builder itemPredicateBuilder, NumberRange.IntRange beeCountRange) {
         return new Conditions(EntityConditions.EMPTY, block, itemPredicateBuilder.build(), beeCountRange);
      }

      public boolean test(BlockState state, ItemStack stack, int count) {
         if (this.block != null && !state.isOf(this.block)) {
            return false;
         } else {
            return !this.item.test(stack) ? false : this.beeCount.test(count);
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         if (this.block != null) {
            jsonObject.addProperty("block", Registries.BLOCK.getId(this.block).toString());
         }

         jsonObject.add("item", this.item.toJson());
         jsonObject.add("num_bees_inside", this.beeCount.toJson());
         return jsonObject;
      }
   }
}
