package net.minecraft.util.math.intprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class UniformIntProvider extends IntProvider {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.INT.fieldOf("min_inclusive").forGetter((provider) -> {
         return provider.min;
      }), Codec.INT.fieldOf("max_inclusive").forGetter((provider) -> {
         return provider.max;
      })).apply(instance, UniformIntProvider::new);
   }).comapFlatMap((provider) -> {
      return provider.max < provider.min ? DataResult.error(() -> {
         return "Max must be at least min, min_inclusive: " + provider.min + ", max_inclusive: " + provider.max;
      }) : DataResult.success(provider);
   }, Function.identity());
   private final int min;
   private final int max;

   private UniformIntProvider(int min, int max) {
      this.min = min;
      this.max = max;
   }

   public static UniformIntProvider create(int min, int max) {
      return new UniformIntProvider(min, max);
   }

   public int get(Random random) {
      return MathHelper.nextBetween(random, this.min, this.max);
   }

   public int getMin() {
      return this.min;
   }

   public int getMax() {
      return this.max;
   }

   public IntProviderType getType() {
      return IntProviderType.UNIFORM;
   }

   public String toString() {
      return "[" + this.min + "-" + this.max + "]";
   }
}
