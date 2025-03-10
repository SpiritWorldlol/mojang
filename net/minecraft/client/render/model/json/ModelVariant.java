package net.minecraft.client.render.model.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.AffineTransformation;

@Environment(EnvType.CLIENT)
public class ModelVariant implements ModelBakeSettings {
   private final Identifier location;
   private final AffineTransformation rotation;
   private final boolean uvLock;
   private final int weight;

   public ModelVariant(Identifier location, AffineTransformation rotation, boolean uvLock, int weight) {
      this.location = location;
      this.rotation = rotation;
      this.uvLock = uvLock;
      this.weight = weight;
   }

   public Identifier getLocation() {
      return this.location;
   }

   public AffineTransformation getRotation() {
      return this.rotation;
   }

   public boolean isUvLocked() {
      return this.uvLock;
   }

   public int getWeight() {
      return this.weight;
   }

   public String toString() {
      return "Variant{modelLocation=" + this.location + ", rotation=" + this.rotation + ", uvLock=" + this.uvLock + ", weight=" + this.weight + "}";
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof ModelVariant)) {
         return false;
      } else {
         ModelVariant lv = (ModelVariant)o;
         return this.location.equals(lv.location) && Objects.equals(this.rotation, lv.rotation) && this.uvLock == lv.uvLock && this.weight == lv.weight;
      }
   }

   public int hashCode() {
      int i = this.location.hashCode();
      i = 31 * i + this.rotation.hashCode();
      i = 31 * i + Boolean.valueOf(this.uvLock).hashCode();
      i = 31 * i + this.weight;
      return i;
   }

   @Environment(EnvType.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      @VisibleForTesting
      static final boolean DEFAULT_UV_LOCK = false;
      @VisibleForTesting
      static final int DEFAULT_WEIGHT = 1;
      @VisibleForTesting
      static final int DEFAULT_X_ROTATION = 0;
      @VisibleForTesting
      static final int DEFAULT_Y_ROTATION = 0;

      public ModelVariant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         Identifier lv = this.deserializeModel(jsonObject);
         net.minecraft.client.render.model.ModelRotation lv2 = this.deserializeRotation(jsonObject);
         boolean bl = this.deserializeUvLock(jsonObject);
         int i = this.deserializeWeight(jsonObject);
         return new ModelVariant(lv, lv2.getRotation(), bl, i);
      }

      private boolean deserializeUvLock(JsonObject object) {
         return JsonHelper.getBoolean(object, "uvlock", false);
      }

      protected net.minecraft.client.render.model.ModelRotation deserializeRotation(JsonObject object) {
         int i = JsonHelper.getInt(object, "x", 0);
         int j = JsonHelper.getInt(object, "y", 0);
         net.minecraft.client.render.model.ModelRotation lv = net.minecraft.client.render.model.ModelRotation.get(i, j);
         if (lv == null) {
            throw new JsonParseException("Invalid BlockModelRotation x: " + i + ", y: " + j);
         } else {
            return lv;
         }
      }

      protected Identifier deserializeModel(JsonObject object) {
         return new Identifier(JsonHelper.getString(object, "model"));
      }

      protected int deserializeWeight(JsonObject object) {
         int i = JsonHelper.getInt(object, "weight", 1);
         if (i < 1) {
            throw new JsonParseException("Invalid weight " + i + " found, expected integer >= 1");
         } else {
            return i;
         }
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement functionJson, Type unused, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(functionJson, unused, context);
      }
   }
}
