package net.minecraft.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class DamagePredicate {
   public static final DamagePredicate ANY = DamagePredicate.Builder.create().build();
   private final NumberRange.FloatRange dealt;
   private final NumberRange.FloatRange taken;
   private final EntityPredicate sourceEntity;
   @Nullable
   private final Boolean blocked;
   private final DamageSourcePredicate type;

   public DamagePredicate() {
      this.dealt = NumberRange.FloatRange.ANY;
      this.taken = NumberRange.FloatRange.ANY;
      this.sourceEntity = EntityPredicate.ANY;
      this.blocked = null;
      this.type = DamageSourcePredicate.EMPTY;
   }

   public DamagePredicate(NumberRange.FloatRange dealt, NumberRange.FloatRange taken, EntityPredicate sourceEntity, @Nullable Boolean blocked, DamageSourcePredicate type) {
      this.dealt = dealt;
      this.taken = taken;
      this.sourceEntity = sourceEntity;
      this.blocked = blocked;
      this.type = type;
   }

   public boolean test(ServerPlayerEntity player, DamageSource source, float dealt, float taken, boolean blocked) {
      if (this == ANY) {
         return true;
      } else if (!this.dealt.test((double)dealt)) {
         return false;
      } else if (!this.taken.test((double)taken)) {
         return false;
      } else if (!this.sourceEntity.test(player, source.getAttacker())) {
         return false;
      } else if (this.blocked != null && this.blocked != blocked) {
         return false;
      } else {
         return this.type.test(player, source);
      }
   }

   public static DamagePredicate fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(json, "damage");
         NumberRange.FloatRange lv = NumberRange.FloatRange.fromJson(jsonObject.get("dealt"));
         NumberRange.FloatRange lv2 = NumberRange.FloatRange.fromJson(jsonObject.get("taken"));
         Boolean boolean_ = jsonObject.has("blocked") ? JsonHelper.getBoolean(jsonObject, "blocked") : null;
         EntityPredicate lv3 = EntityPredicate.fromJson(jsonObject.get("source_entity"));
         DamageSourcePredicate lv4 = DamageSourcePredicate.fromJson(jsonObject.get("type"));
         return new DamagePredicate(lv, lv2, lv3, boolean_, lv4);
      } else {
         return ANY;
      }
   }

   public JsonElement toJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("dealt", this.dealt.toJson());
         jsonObject.add("taken", this.taken.toJson());
         jsonObject.add("source_entity", this.sourceEntity.toJson());
         jsonObject.add("type", this.type.toJson());
         if (this.blocked != null) {
            jsonObject.addProperty("blocked", this.blocked);
         }

         return jsonObject;
      }
   }

   public static class Builder {
      private NumberRange.FloatRange dealt;
      private NumberRange.FloatRange taken;
      private EntityPredicate sourceEntity;
      @Nullable
      private Boolean blocked;
      private DamageSourcePredicate type;

      public Builder() {
         this.dealt = NumberRange.FloatRange.ANY;
         this.taken = NumberRange.FloatRange.ANY;
         this.sourceEntity = EntityPredicate.ANY;
         this.type = DamageSourcePredicate.EMPTY;
      }

      public static Builder create() {
         return new Builder();
      }

      public Builder dealt(NumberRange.FloatRange dealt) {
         this.dealt = dealt;
         return this;
      }

      public Builder taken(NumberRange.FloatRange taken) {
         this.taken = taken;
         return this;
      }

      public Builder sourceEntity(EntityPredicate sourceEntity) {
         this.sourceEntity = sourceEntity;
         return this;
      }

      public Builder blocked(Boolean blocked) {
         this.blocked = blocked;
         return this;
      }

      public Builder type(DamageSourcePredicate type) {
         this.type = type;
         return this;
      }

      public Builder type(DamageSourcePredicate.Builder builder) {
         this.type = builder.build();
         return this;
      }

      public DamagePredicate build() {
         return new DamagePredicate(this.dealt, this.taken, this.sourceEntity, this.blocked, this.type);
      }
   }
}
