package net.minecraft.world.gen.feature;

import java.util.List;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.intprovider.BiasedToBottomIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountMultilayerPlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;

public class NetherPlacedFeatures {
   public static final RegistryKey DELTA = PlacedFeatures.of("delta");
   public static final RegistryKey SMALL_BASALT_COLUMNS = PlacedFeatures.of("small_basalt_columns");
   public static final RegistryKey LARGE_BASALT_COLUMNS = PlacedFeatures.of("large_basalt_columns");
   public static final RegistryKey BASALT_BLOBS = PlacedFeatures.of("basalt_blobs");
   public static final RegistryKey BLACKSTONE_BLOBS = PlacedFeatures.of("blackstone_blobs");
   public static final RegistryKey GLOWSTONE_EXTRA = PlacedFeatures.of("glowstone_extra");
   public static final RegistryKey GLOWSTONE = PlacedFeatures.of("glowstone");
   public static final RegistryKey CRIMSON_FOREST_VEGETATION = PlacedFeatures.of("crimson_forest_vegetation");
   public static final RegistryKey WARPED_FOREST_VEGETATION = PlacedFeatures.of("warped_forest_vegetation");
   public static final RegistryKey NETHER_SPROUTS = PlacedFeatures.of("nether_sprouts");
   public static final RegistryKey TWISTING_VINES = PlacedFeatures.of("twisting_vines");
   public static final RegistryKey WEEPING_VINES = PlacedFeatures.of("weeping_vines");
   public static final RegistryKey PATCH_CRIMSON_ROOTS = PlacedFeatures.of("patch_crimson_roots");
   public static final RegistryKey BASALT_PILLAR = PlacedFeatures.of("basalt_pillar");
   public static final RegistryKey SPRING_DELTA = PlacedFeatures.of("spring_delta");
   public static final RegistryKey SPRING_CLOSED = PlacedFeatures.of("spring_closed");
   public static final RegistryKey SPRING_CLOSED_DOUBLE = PlacedFeatures.of("spring_closed_double");
   public static final RegistryKey SPRING_OPEN = PlacedFeatures.of("spring_open");
   public static final RegistryKey PATCH_SOUL_FIRE = PlacedFeatures.of("patch_soul_fire");
   public static final RegistryKey PATCH_FIRE = PlacedFeatures.of("patch_fire");

   public static void bootstrap(Registerable featureRegisterable) {
      RegistryEntryLookup lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
      RegistryEntry lv2 = lv.getOrThrow(NetherConfiguredFeatures.DELTA);
      RegistryEntry lv3 = lv.getOrThrow(NetherConfiguredFeatures.SMALL_BASALT_COLUMNS);
      RegistryEntry lv4 = lv.getOrThrow(NetherConfiguredFeatures.SMALL_BASALT_COLUMNS_TEMP);
      RegistryEntry lv5 = lv.getOrThrow(NetherConfiguredFeatures.BASALT_BLOBS);
      RegistryEntry lv6 = lv.getOrThrow(NetherConfiguredFeatures.BLACKSTONE_BLOBS);
      RegistryEntry lv7 = lv.getOrThrow(NetherConfiguredFeatures.GLOWSTONE_EXTRA);
      RegistryEntry lv8 = lv.getOrThrow(NetherConfiguredFeatures.CRIMSON_FOREST_VEGETATION);
      RegistryEntry lv9 = lv.getOrThrow(NetherConfiguredFeatures.WARPED_FOREST_VEGETATION);
      RegistryEntry lv10 = lv.getOrThrow(NetherConfiguredFeatures.NETHER_SPROUTS);
      RegistryEntry lv11 = lv.getOrThrow(NetherConfiguredFeatures.TWISTING_VINES);
      RegistryEntry lv12 = lv.getOrThrow(NetherConfiguredFeatures.WEEPING_VINES);
      RegistryEntry lv13 = lv.getOrThrow(NetherConfiguredFeatures.PATCH_CRIMSON_ROOTS);
      RegistryEntry lv14 = lv.getOrThrow(NetherConfiguredFeatures.BASALT_PILLAR);
      RegistryEntry lv15 = lv.getOrThrow(NetherConfiguredFeatures.SPRING_LAVA_NETHER);
      RegistryEntry lv16 = lv.getOrThrow(NetherConfiguredFeatures.SPRING_NETHER_CLOSED);
      RegistryEntry lv17 = lv.getOrThrow(NetherConfiguredFeatures.SPRING_NETHER_OPEN);
      RegistryEntry lv18 = lv.getOrThrow(NetherConfiguredFeatures.PATCH_SOUL_FIRE);
      RegistryEntry lv19 = lv.getOrThrow(NetherConfiguredFeatures.PATCH_FIRE);
      PlacedFeatures.register(featureRegisterable, DELTA, lv2, (PlacementModifier[])(CountMultilayerPlacementModifier.of(40), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, SMALL_BASALT_COLUMNS, lv3, (PlacementModifier[])(CountMultilayerPlacementModifier.of(4), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, LARGE_BASALT_COLUMNS, lv4, (PlacementModifier[])(CountMultilayerPlacementModifier.of(2), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, BASALT_BLOBS, lv5, (PlacementModifier[])(CountPlacementModifier.of(75), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, BLACKSTONE_BLOBS, lv6, (PlacementModifier[])(CountPlacementModifier.of(25), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, GLOWSTONE_EXTRA, lv7, (PlacementModifier[])(CountPlacementModifier.of(BiasedToBottomIntProvider.create(0, 9)), SquarePlacementModifier.of(), PlacedFeatures.FOUR_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, GLOWSTONE, lv7, (PlacementModifier[])(CountPlacementModifier.of(10), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, CRIMSON_FOREST_VEGETATION, lv8, (PlacementModifier[])(CountMultilayerPlacementModifier.of(6), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, WARPED_FOREST_VEGETATION, lv9, (PlacementModifier[])(CountMultilayerPlacementModifier.of(5), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, NETHER_SPROUTS, lv10, (PlacementModifier[])(CountMultilayerPlacementModifier.of(4), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, TWISTING_VINES, lv11, (PlacementModifier[])(CountPlacementModifier.of(10), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, WEEPING_VINES, lv12, (PlacementModifier[])(CountPlacementModifier.of(10), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_CRIMSON_ROOTS, lv13, (PlacementModifier[])(PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, BASALT_PILLAR, lv14, (PlacementModifier[])(CountPlacementModifier.of(10), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, SPRING_DELTA, lv15, (PlacementModifier[])(CountPlacementModifier.of(16), SquarePlacementModifier.of(), PlacedFeatures.FOUR_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, SPRING_CLOSED, lv16, (PlacementModifier[])(CountPlacementModifier.of(16), SquarePlacementModifier.of(), PlacedFeatures.TEN_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, SPRING_CLOSED_DOUBLE, lv16, (PlacementModifier[])(CountPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.TEN_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, SPRING_OPEN, lv17, (PlacementModifier[])(CountPlacementModifier.of(8), SquarePlacementModifier.of(), PlacedFeatures.FOUR_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of()));
      List list = List.of(CountPlacementModifier.of(UniformIntProvider.create(0, 5)), SquarePlacementModifier.of(), PlacedFeatures.FOUR_ABOVE_AND_BELOW_RANGE, BiomePlacementModifier.of());
      PlacedFeatures.register(featureRegisterable, PATCH_SOUL_FIRE, lv18, (List)list);
      PlacedFeatures.register(featureRegisterable, PATCH_FIRE, lv19, (List)list);
   }
}
