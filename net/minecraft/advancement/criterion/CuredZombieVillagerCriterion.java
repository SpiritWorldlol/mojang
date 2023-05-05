package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CuredZombieVillagerCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("cured_zombie_villager");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      EntityConditions lv = EntityPredicate.toConditions(jsonObject, "zombie", arg2);
      EntityConditions lv2 = EntityPredicate.toConditions(jsonObject, "villager", arg2);
      return new Conditions(arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, ZombieEntity zombie, VillagerEntity villager) {
      LootContext lv = EntityPredicate.createAdvancementEntityLootContext(player, zombie);
      LootContext lv2 = EntityPredicate.createAdvancementEntityLootContext(player, villager);
      this.trigger(player, (conditions) -> {
         return conditions.matches(lv, lv2);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final EntityConditions zombie;
      private final EntityConditions villager;

      public Conditions(EntityConditions player, EntityConditions zombie, EntityConditions villager) {
         super(CuredZombieVillagerCriterion.ID, player);
         this.zombie = zombie;
         this.villager = villager;
      }

      public static Conditions any() {
         return new Conditions(EntityConditions.EMPTY, EntityConditions.EMPTY, EntityConditions.EMPTY);
      }

      public boolean matches(LootContext zombieContext, LootContext villagerContext) {
         if (!this.zombie.test(zombieContext)) {
            return false;
         } else {
            return this.villager.test(villagerContext);
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("zombie", this.zombie.toJson(predicateSerializer));
         jsonObject.add("villager", this.villager.toJson(predicateSerializer));
         return jsonObject;
      }
   }
}
