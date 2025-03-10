package net.minecraft.resource.featuretoggle;

import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;

public class FeatureFlags {
   public static final FeatureFlag VANILLA;
   public static final FeatureFlag BUNDLE;
   public static final FeatureManager FEATURE_MANAGER;
   public static final Codec CODEC;
   public static final FeatureSet VANILLA_FEATURES;
   public static final FeatureSet DEFAULT_ENABLED_FEATURES;

   public static String printMissingFlags(FeatureSet featuresToCheck, FeatureSet features) {
      return printMissingFlags(FEATURE_MANAGER, featuresToCheck, features);
   }

   public static String printMissingFlags(FeatureManager featureManager, FeatureSet featuresToCheck, FeatureSet features) {
      Set set = featureManager.toId(features);
      Set set2 = featureManager.toId(featuresToCheck);
      return (String)set.stream().filter((id) -> {
         return !set2.contains(id);
      }).map(Identifier::toString).collect(Collectors.joining(", "));
   }

   public static boolean isNotVanilla(FeatureSet features) {
      return !features.isSubsetOf(VANILLA_FEATURES);
   }

   static {
      FeatureManager.Builder lv = new FeatureManager.Builder("main");
      VANILLA = lv.addVanillaFlag("vanilla");
      BUNDLE = lv.addVanillaFlag("bundle");
      FEATURE_MANAGER = lv.build();
      CODEC = FEATURE_MANAGER.getCodec();
      VANILLA_FEATURES = FeatureSet.of(VANILLA);
      DEFAULT_ENABLED_FEATURES = VANILLA_FEATURES;
   }
}
