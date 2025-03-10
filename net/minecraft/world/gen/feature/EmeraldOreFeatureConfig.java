package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.structure.rule.BlockStateMatchRuleTest;

public class EmeraldOreFeatureConfig implements FeatureConfig {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.list(OreFeatureConfig.Target.CODEC).fieldOf("targets").forGetter((config) -> {
         return config.targets;
      })).apply(instance, EmeraldOreFeatureConfig::new);
   });
   public final List targets;

   public EmeraldOreFeatureConfig(BlockState target, BlockState state) {
      this(ImmutableList.of(OreFeatureConfig.createTarget(new BlockStateMatchRuleTest(target), state)));
   }

   public EmeraldOreFeatureConfig(List targets) {
      this.targets = targets;
   }
}
