package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ItemCriterion extends AbstractCriterion {
   final Identifier id;

   public ItemCriterion(Identifier id) {
      this.id = id;
   }

   public Identifier getId() {
      return this.id;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      EntityConditions lv = EntityConditions.fromJson("location", arg2, jsonObject.get("location"), LootContextTypes.ADVANCEMENT_LOCATION);
      if (lv == null) {
         throw new JsonParseException("Failed to parse 'location' field");
      } else {
         return new Conditions(this.id, arg, lv);
      }
   }

   public void trigger(ServerPlayerEntity player, BlockPos pos, ItemStack stack) {
      ServerWorld lv = player.getServerWorld();
      BlockState lv2 = lv.getBlockState(pos);
      LootContext lv3 = (new LootContext.Builder(lv)).parameter(LootContextParameters.ORIGIN, pos.toCenterPos()).parameter(LootContextParameters.THIS_ENTITY, player).parameter(LootContextParameters.BLOCK_STATE, lv2).parameter(LootContextParameters.TOOL, stack).build(LootContextTypes.ADVANCEMENT_LOCATION);
      this.trigger(player, (conditions) -> {
         return conditions.testLocation(lv3);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final EntityConditions location;

      public Conditions(Identifier id, EntityConditions entity, EntityConditions location) {
         super(id, entity);
         this.location = location;
      }

      public static Conditions createPlacedBlock(Block block) {
         EntityConditions lv = EntityConditions.create(BlockStatePropertyLootCondition.builder(block).build());
         return new Conditions(Criteria.PLACED_BLOCK.id, EntityConditions.EMPTY, lv);
      }

      public static Conditions createPlacedBlock(LootCondition.Builder... location) {
         EntityConditions lv = EntityConditions.create((LootCondition[])Arrays.stream(location).map(LootCondition.Builder::build).toArray((i) -> {
            return new LootCondition[i];
         }));
         return new Conditions(Criteria.PLACED_BLOCK.id, EntityConditions.EMPTY, lv);
      }

      private static Conditions create(LocationPredicate.Builder location, ItemPredicate.Builder tool, Identifier id) {
         EntityConditions lv = EntityConditions.create(LocationCheckLootCondition.builder(location).build(), MatchToolLootCondition.builder(tool).build());
         return new Conditions(id, EntityConditions.EMPTY, lv);
      }

      public static Conditions create(LocationPredicate.Builder location, ItemPredicate.Builder item) {
         return create(location, item, Criteria.ITEM_USED_ON_BLOCK.id);
      }

      public static Conditions createAllayDropItemOnBlock(LocationPredicate.Builder location, ItemPredicate.Builder item) {
         return create(location, item, Criteria.ALLAY_DROP_ITEM_ON_BLOCK.id);
      }

      public boolean testLocation(LootContext context) {
         return this.location.test(context);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("location", this.location.toJson(predicateSerializer));
         return jsonObject;
      }
   }
}
