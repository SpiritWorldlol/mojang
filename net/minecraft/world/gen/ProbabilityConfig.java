package net.minecraft.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;

public class ProbabilityConfig implements FeatureConfig {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((arg) -> {
         return arg.probability;
      })).apply(instance, ProbabilityConfig::new);
   });
   public final float probability;

   public ProbabilityConfig(float probability) {
      this.probability = probability;
   }
}
