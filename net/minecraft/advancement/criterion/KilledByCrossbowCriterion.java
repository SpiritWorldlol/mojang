package net.minecraft.advancement.criterion;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityConditions;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class KilledByCrossbowCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("killed_by_crossbow");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityConditions arg, AdvancementEntityPredicateDeserializer arg2) {
      EntityConditions[] lvs = EntityPredicate.toConditonsArray(jsonObject, "victims", arg2);
      NumberRange.IntRange lv = NumberRange.IntRange.fromJson(jsonObject.get("unique_entity_types"));
      return new Conditions(arg, lvs, lv);
   }

   public void trigger(ServerPlayerEntity player, Collection piercingKilledEntities) {
      List list = Lists.newArrayList();
      Set set = Sets.newHashSet();
      Iterator var5 = piercingKilledEntities.iterator();

      while(var5.hasNext()) {
         Entity lv = (Entity)var5.next();
         set.add(lv.getType());
         list.add(EntityPredicate.createAdvancementEntityLootContext(player, lv));
      }

      this.trigger(player, (conditions) -> {
         return conditions.matches(list, set.size());
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityConditions playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final EntityConditions[] victims;
      private final NumberRange.IntRange uniqueEntityTypes;

      public Conditions(EntityConditions player, EntityConditions[] victims, NumberRange.IntRange uniqueEntityTypes) {
         super(KilledByCrossbowCriterion.ID, player);
         this.victims = victims;
         this.uniqueEntityTypes = uniqueEntityTypes;
      }

      public static Conditions create(EntityPredicate.Builder... victimPredicates) {
         EntityConditions[] lvs = new EntityConditions[victimPredicates.length];

         for(int i = 0; i < victimPredicates.length; ++i) {
            EntityPredicate.Builder lv = victimPredicates[i];
            lvs[i] = EntityPredicate.toConditions(lv.build());
         }

         return new Conditions(EntityConditions.EMPTY, lvs, NumberRange.IntRange.ANY);
      }

      public static Conditions create(NumberRange.IntRange uniqueEntityTypes) {
         EntityConditions[] lvs = new EntityConditions[0];
         return new Conditions(EntityConditions.EMPTY, lvs, uniqueEntityTypes);
      }

      public boolean matches(Collection victimContexts, int uniqueEntityTypeCount) {
         if (this.victims.length > 0) {
            List list = Lists.newArrayList(victimContexts);
            EntityConditions[] var4 = this.victims;
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               EntityConditions lv = var4[var6];
               boolean bl = false;
               Iterator iterator = list.iterator();

               while(iterator.hasNext()) {
                  LootContext lv2 = (LootContext)iterator.next();
                  if (lv.test(lv2)) {
                     iterator.remove();
                     bl = true;
                     break;
                  }
               }

               if (!bl) {
                  return false;
               }
            }
         }

         return this.uniqueEntityTypes.test(uniqueEntityTypeCount);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.add("victims", EntityConditions.toPredicatesJsonArray(this.victims, predicateSerializer));
         jsonObject.add("unique_entity_types", this.uniqueEntityTypes.toJson());
         return jsonObject;
      }
   }
}
