package net.minecraft.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Function;
import net.minecraft.predicate.NumberRange;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class FloatRangeArgument {
   public static final FloatRangeArgument ANY = new FloatRangeArgument((Float)null, (Float)null);
   public static final SimpleCommandExceptionType ONLY_INTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.range.ints"));
   @Nullable
   private final Float min;
   @Nullable
   private final Float max;

   public FloatRangeArgument(@Nullable Float min, @Nullable Float max) {
      this.min = min;
      this.max = max;
   }

   public static FloatRangeArgument exactly(float value) {
      return new FloatRangeArgument(value, value);
   }

   public static FloatRangeArgument between(float min, float max) {
      return new FloatRangeArgument(min, max);
   }

   public static FloatRangeArgument atLeast(float value) {
      return new FloatRangeArgument(value, (Float)null);
   }

   public static FloatRangeArgument atMost(float value) {
      return new FloatRangeArgument((Float)null, value);
   }

   public boolean isInRange(float value) {
      if (this.min != null && this.max != null && this.min > this.max && this.min > value && this.max < value) {
         return false;
      } else if (this.min != null && this.min > value) {
         return false;
      } else {
         return this.max == null || !(this.max < value);
      }
   }

   public boolean isInSquaredRange(double value) {
      if (this.min != null && this.max != null && this.min > this.max && (double)(this.min * this.min) > value && (double)(this.max * this.max) < value) {
         return false;
      } else if (this.min != null && (double)(this.min * this.min) > value) {
         return false;
      } else {
         return this.max == null || !((double)(this.max * this.max) < value);
      }
   }

   @Nullable
   public Float getMin() {
      return this.min;
   }

   @Nullable
   public Float getMax() {
      return this.max;
   }

   public JsonElement toJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else if (this.min != null && this.max != null && this.min.equals(this.max)) {
         return new JsonPrimitive(this.min);
      } else {
         JsonObject jsonObject = new JsonObject();
         if (this.min != null) {
            jsonObject.addProperty("min", this.min);
         }

         if (this.max != null) {
            jsonObject.addProperty("max", this.min);
         }

         return jsonObject;
      }
   }

   public static FloatRangeArgument fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         if (JsonHelper.isNumber(json)) {
            float f = JsonHelper.asFloat(json, "value");
            return new FloatRangeArgument(f, f);
         } else {
            JsonObject jsonObject = JsonHelper.asObject(json, "value");
            Float float_ = jsonObject.has("min") ? JsonHelper.getFloat(jsonObject, "min") : null;
            Float float2 = jsonObject.has("max") ? JsonHelper.getFloat(jsonObject, "max") : null;
            return new FloatRangeArgument(float_, float2);
         }
      } else {
         return ANY;
      }
   }

   public static FloatRangeArgument parse(StringReader reader, boolean allowFloats) throws CommandSyntaxException {
      return parse(reader, allowFloats, (value) -> {
         return value;
      });
   }

   public static FloatRangeArgument parse(StringReader reader, boolean allowFloats, Function transform) throws CommandSyntaxException {
      if (!reader.canRead()) {
         throw NumberRange.EXCEPTION_EMPTY.createWithContext(reader);
      } else {
         int i = reader.getCursor();
         Float float_ = mapFloat(parseFloat(reader, allowFloats), transform);
         Float float2;
         if (reader.canRead(2) && reader.peek() == '.' && reader.peek(1) == '.') {
            reader.skip();
            reader.skip();
            float2 = mapFloat(parseFloat(reader, allowFloats), transform);
            if (float_ == null && float2 == null) {
               reader.setCursor(i);
               throw NumberRange.EXCEPTION_EMPTY.createWithContext(reader);
            }
         } else {
            if (!allowFloats && reader.canRead() && reader.peek() == '.') {
               reader.setCursor(i);
               throw ONLY_INTS_EXCEPTION.createWithContext(reader);
            }

            float2 = float_;
         }

         if (float_ == null && float2 == null) {
            reader.setCursor(i);
            throw NumberRange.EXCEPTION_EMPTY.createWithContext(reader);
         } else {
            return new FloatRangeArgument(float_, float2);
         }
      }
   }

   @Nullable
   private static Float parseFloat(StringReader reader, boolean allowFloats) throws CommandSyntaxException {
      int i = reader.getCursor();

      while(reader.canRead() && peekDigit(reader, allowFloats)) {
         reader.skip();
      }

      String string = reader.getString().substring(i, reader.getCursor());
      if (string.isEmpty()) {
         return null;
      } else {
         try {
            return Float.parseFloat(string);
         } catch (NumberFormatException var5) {
            if (allowFloats) {
               throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble().createWithContext(reader, string);
            } else {
               throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(reader, string);
            }
         }
      }
   }

   private static boolean peekDigit(StringReader reader, boolean allowFloats) {
      char c = reader.peek();
      if ((c < '0' || c > '9') && c != '-') {
         if (allowFloats && c == '.') {
            return !reader.canRead(2) || reader.peek(1) != '.';
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   @Nullable
   private static Float mapFloat(@Nullable Float value, Function function) {
      return value == null ? null : (Float)function.apply(value);
   }
}
