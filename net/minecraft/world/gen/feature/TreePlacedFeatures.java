package net.minecraft.world.gen.feature;

import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.BlockFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountMultilayerPlacementModifier;
import net.minecraft.world.gen.placementmodifier.EnvironmentScanPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;

public class TreePlacedFeatures {
   public static final RegistryKey CRIMSON_FUNGI = PlacedFeatures.of("crimson_fungi");
   public static final RegistryKey WARPED_FUNGI = PlacedFeatures.of("warped_fungi");
   public static final RegistryKey OAK_CHECKED = PlacedFeatures.of("oak_checked");
   public static final RegistryKey DARK_OAK_CHECKED = PlacedFeatures.of("dark_oak_checked");
   public static final RegistryKey BIRCH_CHECKED = PlacedFeatures.of("birch_checked");
   public static final RegistryKey ACACIA_CHECKED = PlacedFeatures.of("acacia_checked");
   public static final RegistryKey SPRUCE_CHECKED = PlacedFeatures.of("spruce_checked");
   public static final RegistryKey MANGROVE_CHECKED = PlacedFeatures.of("mangrove_checked");
   public static final RegistryKey field_42963 = PlacedFeatures.of("cherry_checked");
   public static final RegistryKey PINE_ON_SNOW = PlacedFeatures.of("pine_on_snow");
   public static final RegistryKey SPRUCE_ON_SNOW = PlacedFeatures.of("spruce_on_snow");
   public static final RegistryKey PINE_CHECKED = PlacedFeatures.of("pine_checked");
   public static final RegistryKey JUNGLE_TREE = PlacedFeatures.of("jungle_tree");
   public static final RegistryKey FANCY_OAK_CHECKED = PlacedFeatures.of("fancy_oak_checked");
   public static final RegistryKey MEGA_JUNGLE_TREE_CHECKED = PlacedFeatures.of("mega_jungle_tree_checked");
   public static final RegistryKey MEGA_SPRUCE_CHECKED = PlacedFeatures.of("mega_spruce_checked");
   public static final RegistryKey MEGA_PINE_CHECKED = PlacedFeatures.of("mega_pine_checked");
   public static final RegistryKey TALL_MANGROVE_CHECKED = PlacedFeatures.of("tall_mangrove_checked");
   public static final RegistryKey JUNGLE_BUSH = PlacedFeatures.of("jungle_bush");
   public static final RegistryKey SUPER_BIRCH_BEES_0002 = PlacedFeatures.of("super_birch_bees_0002");
   public static final RegistryKey SUPER_BIRCH_BEES = PlacedFeatures.of("super_birch_bees");
   public static final RegistryKey OAK_BEES_0002 = PlacedFeatures.of("oak_bees_0002");
   public static final RegistryKey OAK_BEES_002 = PlacedFeatures.of("oak_bees_002");
   public static final RegistryKey BIRCH_BEES_0002 = PlacedFeatures.of("birch_bees_0002");
   public static final RegistryKey BIRCH_BEES_002 = PlacedFeatures.of("birch_bees_002");
   public static final RegistryKey FANCY_OAK_BEES_0002 = PlacedFeatures.of("fancy_oak_bees_0002");
   public static final RegistryKey FANCY_OAK_BEES_002 = PlacedFeatures.of("fancy_oak_bees_002");
   public static final RegistryKey FANCY_OAK_BEES = PlacedFeatures.of("fancy_oak_bees");
   public static final RegistryKey field_42962 = PlacedFeatures.of("cherry_bees_005");

   public static void bootstrap(Registerable featureRegisterable) {
      RegistryEntryLookup lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
      RegistryEntry lv2 = lv.getOrThrow(TreeConfiguredFeatures.CRIMSON_FUNGUS);
      RegistryEntry lv3 = lv.getOrThrow(TreeConfiguredFeatures.WARPED_FUNGUS);
      RegistryEntry lv4 = lv.getOrThrow(TreeConfiguredFeatures.OAK);
      RegistryEntry lv5 = lv.getOrThrow(TreeConfiguredFeatures.DARK_OAK);
      RegistryEntry lv6 = lv.getOrThrow(TreeConfiguredFeatures.BIRCH);
      RegistryEntry lv7 = lv.getOrThrow(TreeConfiguredFeatures.ACACIA);
      RegistryEntry lv8 = lv.getOrThrow(TreeConfiguredFeatures.SPRUCE);
      RegistryEntry lv9 = lv.getOrThrow(TreeConfiguredFeatures.MANGROVE);
      RegistryEntry lv10 = lv.getOrThrow(TreeConfiguredFeatures.CHERRY);
      RegistryEntry lv11 = lv.getOrThrow(TreeConfiguredFeatures.PINE);
      RegistryEntry lv12 = lv.getOrThrow(TreeConfiguredFeatures.JUNGLE_TREE);
      RegistryEntry lv13 = lv.getOrThrow(TreeConfiguredFeatures.FANCY_OAK);
      RegistryEntry lv14 = lv.getOrThrow(TreeConfiguredFeatures.MEGA_JUNGLE_TREE);
      RegistryEntry lv15 = lv.getOrThrow(TreeConfiguredFeatures.MEGA_SPRUCE);
      RegistryEntry lv16 = lv.getOrThrow(TreeConfiguredFeatures.MEGA_PINE);
      RegistryEntry lv17 = lv.getOrThrow(TreeConfiguredFeatures.TALL_MANGROVE);
      RegistryEntry lv18 = lv.getOrThrow(TreeConfiguredFeatures.JUNGLE_BUSH);
      RegistryEntry lv19 = lv.getOrThrow(TreeConfiguredFeatures.SUPER_BIRCH_BEES_0002);
      RegistryEntry lv20 = lv.getOrThrow(TreeConfiguredFeatures.SUPER_BIRCH_BEES);
      RegistryEntry lv21 = lv.getOrThrow(TreeConfiguredFeatures.OAK_BEES_0002);
      RegistryEntry lv22 = lv.getOrThrow(TreeConfiguredFeatures.OAK_BEES_002);
      RegistryEntry lv23 = lv.getOrThrow(TreeConfiguredFeatures.BIRCH_BEES_0002);
      RegistryEntry lv24 = lv.getOrThrow(TreeConfiguredFeatures.BIRCH_BEES_002);
      RegistryEntry lv25 = lv.getOrThrow(TreeConfiguredFeatures.FANCY_OAK_BEES_0002);
      RegistryEntry lv26 = lv.getOrThrow(TreeConfiguredFeatures.FANCY_OAK_BEES_002);
      RegistryEntry lv27 = lv.getOrThrow(TreeConfiguredFeatures.FANCY_OAK_BEES);
      RegistryEntry lv28 = lv.getOrThrow(TreeConfiguredFeatures.CHERRY_BEES_005);
      PlacedFeatures.register(featureRegisterable, CRIMSON_FUNGI, lv2, (PlacementModifier[])(CountMultilayerPlacementModifier.of(8), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, WARPED_FUNGI, lv3, (PlacementModifier[])(CountMultilayerPlacementModifier.of(8), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, OAK_CHECKED, lv4, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING)));
      PlacedFeatures.register(featureRegisterable, DARK_OAK_CHECKED, lv5, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.DARK_OAK_SAPLING)));
      PlacedFeatures.register(featureRegisterable, BIRCH_CHECKED, lv6, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.BIRCH_SAPLING)));
      PlacedFeatures.register(featureRegisterable, ACACIA_CHECKED, lv7, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.ACACIA_SAPLING)));
      PlacedFeatures.register(featureRegisterable, SPRUCE_CHECKED, lv8, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING)));
      PlacedFeatures.register(featureRegisterable, MANGROVE_CHECKED, lv9, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.MANGROVE_PROPAGULE)));
      PlacedFeatures.register(featureRegisterable, field_42963, lv10, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.CHERRY_SAPLING)));
      BlockPredicate lv29 = BlockPredicate.matchingBlocks(Direction.DOWN.getVector(), Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW);
      List list = List.of(EnvironmentScanPlacementModifier.of(Direction.UP, BlockPredicate.not(BlockPredicate.matchingBlocks(Blocks.POWDER_SNOW)), 8), BlockFilterPlacementModifier.of(lv29));
      PlacedFeatures.register(featureRegisterable, PINE_ON_SNOW, lv11, (List)list);
      PlacedFeatures.register(featureRegisterable, SPRUCE_ON_SNOW, lv8, (List)list);
      PlacedFeatures.register(featureRegisterable, PINE_CHECKED, lv11, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING)));
      PlacedFeatures.register(featureRegisterable, JUNGLE_TREE, lv12, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.JUNGLE_SAPLING)));
      PlacedFeatures.register(featureRegisterable, FANCY_OAK_CHECKED, lv13, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING)));
      PlacedFeatures.register(featureRegisterable, MEGA_JUNGLE_TREE_CHECKED, lv14, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.JUNGLE_SAPLING)));
      PlacedFeatures.register(featureRegisterable, MEGA_SPRUCE_CHECKED, lv15, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING)));
      PlacedFeatures.register(featureRegisterable, MEGA_PINE_CHECKED, lv16, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING)));
      PlacedFeatures.register(featureRegisterable, TALL_MANGROVE_CHECKED, lv17, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.MANGROVE_PROPAGULE)));
      PlacedFeatures.register(featureRegisterable, JUNGLE_BUSH, lv18, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING)));
      PlacedFeatures.register(featureRegisterable, SUPER_BIRCH_BEES_0002, lv19, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.BIRCH_SAPLING)));
      PlacedFeatures.register(featureRegisterable, SUPER_BIRCH_BEES, lv20, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.BIRCH_SAPLING)));
      PlacedFeatures.register(featureRegisterable, OAK_BEES_0002, lv21, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING)));
      PlacedFeatures.register(featureRegisterable, OAK_BEES_002, lv22, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING)));
      PlacedFeatures.register(featureRegisterable, BIRCH_BEES_0002, lv23, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.BIRCH_SAPLING)));
      PlacedFeatures.register(featureRegisterable, BIRCH_BEES_002, lv24, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.BIRCH_SAPLING)));
      PlacedFeatures.register(featureRegisterable, FANCY_OAK_BEES_0002, lv25, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING)));
      PlacedFeatures.register(featureRegisterable, FANCY_OAK_BEES_002, lv26, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING)));
      PlacedFeatures.register(featureRegisterable, FANCY_OAK_BEES, lv27, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING)));
      PlacedFeatures.register(featureRegisterable, field_42962, lv28, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.CHERRY_SAPLING)));
   }
}
