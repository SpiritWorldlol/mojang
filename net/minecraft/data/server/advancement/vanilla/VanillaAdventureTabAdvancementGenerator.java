package net.minecraft.data.server.advancement.vanilla;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.ChanneledLightningCriterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.ItemCriterion;
import net.minecraft.advancement.criterion.KilledByCrossbowCriterion;
import net.minecraft.advancement.criterion.LightningStrikeCriterion;
import net.minecraft.advancement.criterion.OnKilledCriterion;
import net.minecraft.advancement.criterion.PlayerGeneratesContainerLootCriterion;
import net.minecraft.advancement.criterion.PlayerHurtEntityCriterion;
import net.minecraft.advancement.criterion.RecipeCraftedCriterion;
import net.minecraft.advancement.criterion.ShotCrossbowCriterion;
import net.minecraft.advancement.criterion.SlideDownBlockCriterion;
import net.minecraft.advancement.criterion.SummonedEntityCriterion;
import net.minecraft.advancement.criterion.TargetHitCriterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.advancement.criterion.TravelCriterion;
import net.minecraft.advancement.criterion.UsedTotemCriterion;
import net.minecraft.advancement.criterion.UsingItemCriterion;
import net.minecraft.advancement.criterion.VillagerTradeCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.data.server.advancement.AdvancementTabGenerator;
import net.minecraft.data.server.recipe.VanillaRecipeProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.DamagePredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.TagPredicate;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LightningBoltPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.entity.PlayerPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.CraftingDecoratedPotRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;

public class VanillaAdventureTabAdvancementGenerator implements AdvancementTabGenerator {
   private static final int OVERWORLD_HEIGHT = 384;
   private static final int OVERWORLD_MAX_Y = 320;
   private static final int OVERWORLD_MIN_Y = -64;
   private static final int OVERWORLD_BEDROCK_LAYER_HEIGHT = 5;
   private static final EntityType[] MONSTERS;

   private static LightningStrikeCriterion.Conditions createLightningStrike(NumberRange.IntRange range, EntityPredicate entity) {
      return LightningStrikeCriterion.Conditions.create(EntityPredicate.Builder.create().distance(DistancePredicate.absolute(NumberRange.FloatRange.atMost(30.0))).typeSpecific(LightningBoltPredicate.of(range)).build(), entity);
   }

   private static UsingItemCriterion.Conditions createLookingAtEntityUsing(EntityType entity, Item item) {
      return UsingItemCriterion.Conditions.create(EntityPredicate.Builder.create().typeSpecific(PlayerPredicate.Builder.create().lookingAt(EntityPredicate.Builder.create().type(entity).build()).build()), ItemPredicate.Builder.create().items(item));
   }

   public void accept(RegistryWrapper.WrapperLookup lookup, Consumer exporter) {
      Advancement lv = Advancement.Builder.create().display((ItemConvertible)Items.MAP, Text.translatable("advancements.adventure.root.title"), Text.translatable("advancements.adventure.root.description"), new Identifier("textures/gui/advancements/backgrounds/adventure.png"), AdvancementFrame.TASK, false, false, false).criteriaMerger(CriterionMerger.OR).criterion("killed_something", (CriterionConditions)OnKilledCriterion.Conditions.createPlayerKilledEntity()).criterion("killed_by_something", (CriterionConditions)OnKilledCriterion.Conditions.createEntityKilledPlayer()).build(exporter, "adventure/root");
      Advancement lv2 = Advancement.Builder.create().parent(lv).display((ItemConvertible)Blocks.RED_BED, Text.translatable("advancements.adventure.sleep_in_bed.title"), Text.translatable("advancements.adventure.sleep_in_bed.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("slept_in_bed", (CriterionConditions)TickCriterion.Conditions.createSleptInBed()).build(exporter, "adventure/sleep_in_bed");
      buildAdventuringTime(exporter, lv2, MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD);
      Advancement lv3 = Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.EMERALD, Text.translatable("advancements.adventure.trade.title"), Text.translatable("advancements.adventure.trade.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("traded", (CriterionConditions)VillagerTradeCriterion.Conditions.any()).build(exporter, "adventure/trade");
      Advancement.Builder.create().parent(lv3).display((ItemConvertible)Items.EMERALD, Text.translatable("advancements.adventure.trade_at_world_height.title"), Text.translatable("advancements.adventure.trade_at_world_height.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("trade_at_world_height", (CriterionConditions)VillagerTradeCriterion.Conditions.create(EntityPredicate.Builder.create().location(LocationPredicate.y(NumberRange.FloatRange.atLeast(319.0))))).build(exporter, "adventure/trade_at_world_height");
      Advancement lv4 = requireListedMobsKilled(Advancement.Builder.create()).parent(lv).display((ItemConvertible)Items.IRON_SWORD, Text.translatable("advancements.adventure.kill_a_mob.title"), Text.translatable("advancements.adventure.kill_a_mob.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criteriaMerger(CriterionMerger.OR).build(exporter, "adventure/kill_a_mob");
      requireListedMobsKilled(Advancement.Builder.create()).parent(lv4).display((ItemConvertible)Items.DIAMOND_SWORD, Text.translatable("advancements.adventure.kill_all_mobs.title"), Text.translatable("advancements.adventure.kill_all_mobs.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).build(exporter, "adventure/kill_all_mobs");
      Advancement lv5 = Advancement.Builder.create().parent(lv4).display((ItemConvertible)Items.BOW, Text.translatable("advancements.adventure.shoot_arrow.title"), Text.translatable("advancements.adventure.shoot_arrow.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("shot_arrow", (CriterionConditions)PlayerHurtEntityCriterion.Conditions.create(DamagePredicate.Builder.create().type(DamageSourcePredicate.Builder.create().tag(TagPredicate.expected(DamageTypeTags.IS_PROJECTILE)).directEntity(EntityPredicate.Builder.create().type(EntityTypeTags.ARROWS))))).build(exporter, "adventure/shoot_arrow");
      Advancement lv6 = Advancement.Builder.create().parent(lv4).display((ItemConvertible)Items.TRIDENT, Text.translatable("advancements.adventure.throw_trident.title"), Text.translatable("advancements.adventure.throw_trident.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("shot_trident", (CriterionConditions)PlayerHurtEntityCriterion.Conditions.create(DamagePredicate.Builder.create().type(DamageSourcePredicate.Builder.create().tag(TagPredicate.expected(DamageTypeTags.IS_PROJECTILE)).directEntity(EntityPredicate.Builder.create().type(EntityType.TRIDENT))))).build(exporter, "adventure/throw_trident");
      Advancement.Builder.create().parent(lv6).display((ItemConvertible)Items.TRIDENT, Text.translatable("advancements.adventure.very_very_frightening.title"), Text.translatable("advancements.adventure.very_very_frightening.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("struck_villager", (CriterionConditions)ChanneledLightningCriterion.Conditions.create(EntityPredicate.Builder.create().type(EntityType.VILLAGER).build())).build(exporter, "adventure/very_very_frightening");
      Advancement.Builder.create().parent(lv3).display((ItemConvertible)Blocks.CARVED_PUMPKIN, Text.translatable("advancements.adventure.summon_iron_golem.title"), Text.translatable("advancements.adventure.summon_iron_golem.description"), (Identifier)null, AdvancementFrame.GOAL, true, true, false).criterion("summoned_golem", (CriterionConditions)SummonedEntityCriterion.Conditions.create(EntityPredicate.Builder.create().type(EntityType.IRON_GOLEM))).build(exporter, "adventure/summon_iron_golem");
      Advancement.Builder.create().parent(lv5).display((ItemConvertible)Items.ARROW, Text.translatable("advancements.adventure.sniper_duel.title"), Text.translatable("advancements.adventure.sniper_duel.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).criterion("killed_skeleton", (CriterionConditions)OnKilledCriterion.Conditions.createPlayerKilledEntity(EntityPredicate.Builder.create().type(EntityType.SKELETON).distance(DistancePredicate.horizontal(NumberRange.FloatRange.atLeast(50.0))), DamageSourcePredicate.Builder.create().tag(TagPredicate.expected(DamageTypeTags.IS_PROJECTILE)))).build(exporter, "adventure/sniper_duel");
      Advancement.Builder.create().parent(lv4).display((ItemConvertible)Items.TOTEM_OF_UNDYING, Text.translatable("advancements.adventure.totem_of_undying.title"), Text.translatable("advancements.adventure.totem_of_undying.description"), (Identifier)null, AdvancementFrame.GOAL, true, true, false).criterion("used_totem", (CriterionConditions)UsedTotemCriterion.Conditions.create((ItemConvertible)Items.TOTEM_OF_UNDYING)).build(exporter, "adventure/totem_of_undying");
      Advancement lv7 = Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.CROSSBOW, Text.translatable("advancements.adventure.ol_betsy.title"), Text.translatable("advancements.adventure.ol_betsy.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("shot_crossbow", (CriterionConditions)ShotCrossbowCriterion.Conditions.create((ItemConvertible)Items.CROSSBOW)).build(exporter, "adventure/ol_betsy");
      Advancement.Builder.create().parent(lv7).display((ItemConvertible)Items.CROSSBOW, Text.translatable("advancements.adventure.whos_the_pillager_now.title"), Text.translatable("advancements.adventure.whos_the_pillager_now.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("kill_pillager", (CriterionConditions)KilledByCrossbowCriterion.Conditions.create(EntityPredicate.Builder.create().type(EntityType.PILLAGER))).build(exporter, "adventure/whos_the_pillager_now");
      Advancement.Builder.create().parent(lv7).display((ItemConvertible)Items.CROSSBOW, Text.translatable("advancements.adventure.two_birds_one_arrow.title"), Text.translatable("advancements.adventure.two_birds_one_arrow.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(65)).criterion("two_birds", (CriterionConditions)KilledByCrossbowCriterion.Conditions.create(EntityPredicate.Builder.create().type(EntityType.PHANTOM), EntityPredicate.Builder.create().type(EntityType.PHANTOM))).build(exporter, "adventure/two_birds_one_arrow");
      Advancement.Builder.create().parent(lv7).display((ItemConvertible)Items.CROSSBOW, Text.translatable("advancements.adventure.arbalistic.title"), Text.translatable("advancements.adventure.arbalistic.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, true).rewards(AdvancementRewards.Builder.experience(85)).criterion("arbalistic", (CriterionConditions)KilledByCrossbowCriterion.Conditions.create(NumberRange.IntRange.exactly(5))).build(exporter, "adventure/arbalistic");
      Advancement lv8 = Advancement.Builder.create().parent(lv).display((ItemStack)Raid.getOminousBanner(), Text.translatable("advancements.adventure.voluntary_exile.title"), Text.translatable("advancements.adventure.voluntary_exile.description"), (Identifier)null, AdvancementFrame.TASK, true, true, true).criterion("voluntary_exile", (CriterionConditions)OnKilledCriterion.Conditions.createPlayerKilledEntity(EntityPredicate.Builder.create().type(EntityTypeTags.RAIDERS).equipment(EntityEquipmentPredicate.OMINOUS_BANNER_ON_HEAD))).build(exporter, "adventure/voluntary_exile");
      Advancement.Builder.create().parent(lv8).display((ItemStack)Raid.getOminousBanner(), Text.translatable("advancements.adventure.hero_of_the_village.title"), Text.translatable("advancements.adventure.hero_of_the_village.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, true).rewards(AdvancementRewards.Builder.experience(100)).criterion("hero_of_the_village", (CriterionConditions)TickCriterion.Conditions.createHeroOfTheVillage()).build(exporter, "adventure/hero_of_the_village");
      Advancement.Builder.create().parent(lv).display((ItemConvertible)Blocks.HONEY_BLOCK.asItem(), Text.translatable("advancements.adventure.honey_block_slide.title"), Text.translatable("advancements.adventure.honey_block_slide.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("honey_block_slide", (CriterionConditions)SlideDownBlockCriterion.Conditions.create(Blocks.HONEY_BLOCK)).build(exporter, "adventure/honey_block_slide");
      Advancement.Builder.create().parent(lv5).display((ItemConvertible)Blocks.TARGET.asItem(), Text.translatable("advancements.adventure.bullseye.title"), Text.translatable("advancements.adventure.bullseye.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).criterion("bullseye", (CriterionConditions)TargetHitCriterion.Conditions.create(NumberRange.IntRange.exactly(15), EntityPredicate.Extended.ofLegacy(EntityPredicate.Builder.create().distance(DistancePredicate.horizontal(NumberRange.FloatRange.atLeast(30.0))).build()))).build(exporter, "adventure/bullseye");
      Advancement.Builder.create().parent(lv2).display((ItemConvertible)Items.LEATHER_BOOTS, Text.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.title"), Text.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("walk_on_powder_snow_with_leather_boots", (CriterionConditions)TickCriterion.Conditions.createLocation(Blocks.POWDER_SNOW, Items.LEATHER_BOOTS)).build(exporter, "adventure/walk_on_powder_snow_with_leather_boots");
      Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.LIGHTNING_ROD, Text.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.title"), Text.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("lightning_rod_with_villager_no_fire", (CriterionConditions)createLightningStrike(NumberRange.IntRange.exactly(0), EntityPredicate.Builder.create().type(EntityType.VILLAGER).build())).build(exporter, "adventure/lightning_rod_with_villager_no_fire");
      Advancement lv9 = Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.SPYGLASS, Text.translatable("advancements.adventure.spyglass_at_parrot.title"), Text.translatable("advancements.adventure.spyglass_at_parrot.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("spyglass_at_parrot", (CriterionConditions)createLookingAtEntityUsing(EntityType.PARROT, Items.SPYGLASS)).build(exporter, "adventure/spyglass_at_parrot");
      Advancement lv10 = Advancement.Builder.create().parent(lv9).display((ItemConvertible)Items.SPYGLASS, Text.translatable("advancements.adventure.spyglass_at_ghast.title"), Text.translatable("advancements.adventure.spyglass_at_ghast.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("spyglass_at_ghast", (CriterionConditions)createLookingAtEntityUsing(EntityType.GHAST, Items.SPYGLASS)).build(exporter, "adventure/spyglass_at_ghast");
      Advancement.Builder.create().parent(lv2).display((ItemConvertible)Items.JUKEBOX, Text.translatable("advancements.adventure.play_jukebox_in_meadows.title"), Text.translatable("advancements.adventure.play_jukebox_in_meadows.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("play_jukebox_in_meadows", (CriterionConditions)ItemCriterion.Conditions.create(LocationPredicate.Builder.create().biome(BiomeKeys.MEADOW).block(BlockPredicate.Builder.create().blocks(Blocks.JUKEBOX).build()), ItemPredicate.Builder.create().tag(ItemTags.MUSIC_DISCS))).build(exporter, "adventure/play_jukebox_in_meadows");
      Advancement.Builder.create().parent(lv10).display((ItemConvertible)Items.SPYGLASS, Text.translatable("advancements.adventure.spyglass_at_dragon.title"), Text.translatable("advancements.adventure.spyglass_at_dragon.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("spyglass_at_dragon", (CriterionConditions)createLookingAtEntityUsing(EntityType.ENDER_DRAGON, Items.SPYGLASS)).build(exporter, "adventure/spyglass_at_dragon");
      Advancement.Builder.create().parent(lv).display((ItemConvertible)Items.WATER_BUCKET, Text.translatable("advancements.adventure.fall_from_world_height.title"), Text.translatable("advancements.adventure.fall_from_world_height.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("fall_from_world_height", (CriterionConditions)TravelCriterion.Conditions.fallFromHeight(EntityPredicate.Builder.create().location(LocationPredicate.y(NumberRange.FloatRange.atMost(-59.0))), DistancePredicate.y(NumberRange.FloatRange.atLeast(379.0)), LocationPredicate.y(NumberRange.FloatRange.atLeast(319.0)))).build(exporter, "adventure/fall_from_world_height");
      Advancement.Builder.create().parent(lv4).display((ItemConvertible)Blocks.SCULK_CATALYST, Text.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.title"), Text.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).criterion("kill_mob_near_sculk_catalyst", (CriterionConditions)OnKilledCriterion.Conditions.createKillMobNearSculkCatalyst()).build(exporter, "adventure/kill_mob_near_sculk_catalyst");
      Advancement.Builder.create().parent(lv).display((ItemConvertible)Blocks.SCULK_SENSOR, Text.translatable("advancements.adventure.avoid_vibration.title"), Text.translatable("advancements.adventure.avoid_vibration.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("avoid_vibration", (CriterionConditions)TickCriterion.Conditions.createAvoidVibration()).build(exporter, "adventure/avoid_vibration");
      Advancement lv11 = requireSalvagedSherd(Advancement.Builder.create()).parent(lv).display((ItemConvertible)Items.BRUSH, Text.translatable("advancements.adventure.salvage_sherd.title"), Text.translatable("advancements.adventure.salvage_sherd.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(exporter, "adventure/salvage_sherd");
      Advancement.Builder.create().parent(lv11).display((ItemStack)CraftingDecoratedPotRecipe.getPotStackWith(new DecoratedPotBlockEntity.Sherds(Items.BRICK, Items.HEART_POTTERY_SHERD, Items.BRICK, Items.EXPLORER_POTTERY_SHERD)), Text.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.title"), Text.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("pot_crafted_using_only_sherds", (CriterionConditions)RecipeCraftedCriterion.Conditions.create(new Identifier("minecraft:decorated_pot"), List.of(ItemPredicate.Builder.create().tag(ItemTags.DECORATED_POT_SHERDS).build(), ItemPredicate.Builder.create().tag(ItemTags.DECORATED_POT_SHERDS).build(), ItemPredicate.Builder.create().tag(ItemTags.DECORATED_POT_SHERDS).build(), ItemPredicate.Builder.create().tag(ItemTags.DECORATED_POT_SHERDS).build()))).build(exporter, "adventure/craft_decorated_pot_using_only_sherds");
      Advancement lv12 = requireTrimmedArmor(Advancement.Builder.create()).parent(lv).display((ItemStack)(new ItemStack(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE)), Text.translatable("advancements.adventure.trim_with_any_armor_pattern.title"), Text.translatable("advancements.adventure.trim_with_any_armor_pattern.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(exporter, "adventure/trim_with_any_armor_pattern");
      requireAllExclusiveTrimmedArmor(Advancement.Builder.create()).parent(lv12).display((ItemStack)(new ItemStack(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE)), Text.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.title"), Text.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.description"), (Identifier)null, AdvancementFrame.TASK, true, true, false).build(exporter, "adventure/trim_with_all_exclusive_armor_patterns");
   }

   private static Advancement.Builder requireAllExclusiveTrimmedArmor(Advancement.Builder builder) {
      builder.criteriaMerger(CriterionMerger.AND);
      Map map = VanillaRecipeProvider.getTrimSmithingTemplateMap();
      Stream.of(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE).forEach((template) -> {
         Identifier lv = (Identifier)map.get(template);
         builder.criterion("armor_trimmed_" + lv, (CriterionConditions)RecipeCraftedCriterion.Conditions.create(lv));
      });
      return builder;
   }

   private static Advancement.Builder requireTrimmedArmor(Advancement.Builder builder) {
      builder.criteriaMerger(CriterionMerger.OR);
      Iterator var1 = VanillaRecipeProvider.getTrimSmithingTemplateMap().values().iterator();

      while(var1.hasNext()) {
         Identifier lv = (Identifier)var1.next();
         builder.criterion("armor_trimmed_" + lv, (CriterionConditions)RecipeCraftedCriterion.Conditions.create(lv));
      }

      return builder;
   }

   private static Advancement.Builder requireSalvagedSherd(Advancement.Builder builder) {
      builder.criterion("desert_pyramid", (CriterionConditions)PlayerGeneratesContainerLootCriterion.Conditions.create(LootTables.DESERT_PYRAMID_ARCHAEOLOGY));
      builder.criterion("desert_well", (CriterionConditions)PlayerGeneratesContainerLootCriterion.Conditions.create(LootTables.DESERT_WELL_ARCHAEOLOGY));
      builder.criterion("ocean_ruin_cold", (CriterionConditions)PlayerGeneratesContainerLootCriterion.Conditions.create(LootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY));
      builder.criterion("ocean_ruin_warm", (CriterionConditions)PlayerGeneratesContainerLootCriterion.Conditions.create(LootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY));
      builder.criterion("trail_ruins_rare", (CriterionConditions)PlayerGeneratesContainerLootCriterion.Conditions.create(LootTables.TRAIL_RUINS_RARE_ARCHAEOLOGY));
      builder.criterion("trail_ruins_common", (CriterionConditions)PlayerGeneratesContainerLootCriterion.Conditions.create(LootTables.TRAIL_RUINS_COMMON_ARCHAEOLOGY));
      String[] strings = (String[])builder.getCriteria().keySet().toArray((i) -> {
         return new String[i];
      });
      String string = "has_sherd";
      builder.criterion("has_sherd", (CriterionConditions)InventoryChangedCriterion.Conditions.items(ItemPredicate.Builder.create().tag(ItemTags.DECORATED_POT_SHERDS).build()));
      builder.requirements(new String[][]{strings, {"has_sherd"}});
      return builder;
   }

   protected static void buildAdventuringTime(Consumer exporter, Advancement parent, MultiNoiseBiomeSourceParameterList.Preset preset) {
      requireListedBiomesVisited(Advancement.Builder.create(), preset.biomeStream().toList()).parent(parent).display((ItemConvertible)Items.DIAMOND_BOOTS, Text.translatable("advancements.adventure.adventuring_time.title"), Text.translatable("advancements.adventure.adventuring_time.description"), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(500)).build(exporter, "adventure/adventuring_time");
   }

   private static Advancement.Builder requireListedMobsKilled(Advancement.Builder builder) {
      EntityType[] var1 = MONSTERS;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         EntityType lv = var1[var3];
         builder.criterion(Registries.ENTITY_TYPE.getId(lv).toString(), (CriterionConditions)OnKilledCriterion.Conditions.createPlayerKilledEntity(EntityPredicate.Builder.create().type(lv)));
      }

      return builder;
   }

   protected static Advancement.Builder requireListedBiomesVisited(Advancement.Builder builder, List biomes) {
      Iterator var2 = biomes.iterator();

      while(var2.hasNext()) {
         RegistryKey lv = (RegistryKey)var2.next();
         builder.criterion(lv.getValue().toString(), (CriterionConditions)TickCriterion.Conditions.createLocation(LocationPredicate.biome(lv)));
      }

      return builder;
   }

   static {
      MONSTERS = new EntityType[]{EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.DROWNED, EntityType.ELDER_GUARDIAN, EntityType.ENDER_DRAGON, EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.EVOKER, EntityType.GHAST, EntityType.GUARDIAN, EntityType.HOGLIN, EntityType.HUSK, EntityType.MAGMA_CUBE, EntityType.PHANTOM, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.STRAY, EntityType.VEX, EntityType.VINDICATOR, EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.WITHER, EntityType.ZOGLIN, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIE, EntityType.ZOMBIFIED_PIGLIN};
   }
}
