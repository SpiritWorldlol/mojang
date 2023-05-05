package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ChanneledLightningCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("channeled_lightning");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      EntityConditions[] lvs = EntityPredicate.toConditonsArray(jsonObject, "victims", arg2);
      return new Conditions(arg, lvs);
   }

   public void trigger(ServerPlayerEntity player, Collection victims) {
      List list = (List)victims.stream().map((entity) -> {
         return EntityPredicate.createAdvancementEntityLootContext(player, entity);
      }).collect(Collectors.toList());
      this.trigger(player, (conditions) -> {
         return conditions.matches(list);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final EntityConditions[] victims;

      public Conditions(EntityConditions player, EntityConditions[] victims) {
         super(ChanneledLightningCriterion.ID, player);
         this.victims = victims;
      }

      public static Conditions create(EntityPredicate... victims) {
         return new Conditions(EntityConditions.EMPTY, (EntityConditions[])Stream.of(victims).map(EntityPredicate::toConditions).toArray((i) -> {
            return new EntityConditions[i];
         }));
      }

      public boolean matches(Collection victims) {
         EntityConditions[] var2 = this.victims;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            EntityConditions lv = var2[var4];
            boolean bl = false;
            Iterator var7 = victims.iterator();

            while(var7.hasNext()) {
               LootContext lv2 = (LootContext)var7.next();
               if (lv.test(lv2)) {
                  bl = true;
                  break;
               }
            }

            if (!bl) {
               return false;
            }
         }

         return true;
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("victims", EntityConditions.toPredicatesJsonArray(this.victims, predicateSerializer));
         return jsonObject;
      }
   }
}
