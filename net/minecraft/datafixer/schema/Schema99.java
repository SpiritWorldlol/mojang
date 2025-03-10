package net.minecraft.datafixer.schema;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import net.minecraft.datafixer.TypeReferences;
import org.slf4j.Logger;

public class Schema99 extends Schema {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final Map field_5748 = (Map)DataFixUtils.make(Maps.newHashMap(), (map) -> {
      map.put("minecraft:furnace", "Furnace");
      map.put("minecraft:lit_furnace", "Furnace");
      map.put("minecraft:chest", "Chest");
      map.put("minecraft:trapped_chest", "Chest");
      map.put("minecraft:ender_chest", "EnderChest");
      map.put("minecraft:jukebox", "RecordPlayer");
      map.put("minecraft:dispenser", "Trap");
      map.put("minecraft:dropper", "Dropper");
      map.put("minecraft:sign", "Sign");
      map.put("minecraft:mob_spawner", "MobSpawner");
      map.put("minecraft:noteblock", "Music");
      map.put("minecraft:brewing_stand", "Cauldron");
      map.put("minecraft:enhanting_table", "EnchantTable");
      map.put("minecraft:command_block", "CommandBlock");
      map.put("minecraft:beacon", "Beacon");
      map.put("minecraft:skull", "Skull");
      map.put("minecraft:daylight_detector", "DLDetector");
      map.put("minecraft:hopper", "Hopper");
      map.put("minecraft:banner", "Banner");
      map.put("minecraft:flower_pot", "FlowerPot");
      map.put("minecraft:repeating_command_block", "CommandBlock");
      map.put("minecraft:chain_command_block", "CommandBlock");
      map.put("minecraft:standing_sign", "Sign");
      map.put("minecraft:wall_sign", "Sign");
      map.put("minecraft:piston_head", "Piston");
      map.put("minecraft:daylight_detector_inverted", "DLDetector");
      map.put("minecraft:unpowered_comparator", "Comparator");
      map.put("minecraft:powered_comparator", "Comparator");
      map.put("minecraft:wall_banner", "Banner");
      map.put("minecraft:standing_banner", "Banner");
      map.put("minecraft:structure_block", "Structure");
      map.put("minecraft:end_portal", "Airportal");
      map.put("minecraft:end_gateway", "EndGateway");
      map.put("minecraft:shield", "Banner");
   });
   protected static final Hook.HookFunction field_5747 = new Hook.HookFunction() {
      public Object apply(DynamicOps ops, Object value) {
         return Schema99.method_5359(new Dynamic(ops, value), Schema99.field_5748, "ArmorStand");
      }
   };

   public Schema99(int versionKey, Schema parent) {
      super(versionKey, parent);
   }

   protected static TypeTemplate targetEquipment(Schema schema) {
      return DSL.optionalFields("Equipment", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
   }

   protected static void targetEquipment(Schema schema, Map map, String entityId) {
      schema.register(map, entityId, () -> {
         return targetEquipment(schema);
      });
   }

   protected static void targetInTile(Schema schema, Map map, String entityId) {
      schema.register(map, entityId, () -> {
         return DSL.optionalFields("inTile", TypeReferences.BLOCK_NAME.in(schema));
      });
   }

   protected static void targetDisplayTile(Schema schema, Map map, String entityId) {
      schema.register(map, entityId, () -> {
         return DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema));
      });
   }

   protected static void targetItems(Schema schema, Map map, String entityId) {
      schema.register(map, entityId, () -> {
         return DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
      });
   }

   public Map registerEntities(Schema schema) {
      Map map = Maps.newHashMap();
      schema.register(map, "Item", (name) -> {
         return DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema));
      });
      schema.registerSimple(map, "XPOrb");
      targetInTile(schema, map, "ThrownEgg");
      schema.registerSimple(map, "LeashKnot");
      schema.registerSimple(map, "Painting");
      schema.register(map, "Arrow", (name) -> {
         return DSL.optionalFields("inTile", TypeReferences.BLOCK_NAME.in(schema));
      });
      schema.register(map, "TippedArrow", (name) -> {
         return DSL.optionalFields("inTile", TypeReferences.BLOCK_NAME.in(schema));
      });
      schema.register(map, "SpectralArrow", (name) -> {
         return DSL.optionalFields("inTile", TypeReferences.BLOCK_NAME.in(schema));
      });
      targetInTile(schema, map, "Snowball");
      targetInTile(schema, map, "Fireball");
      targetInTile(schema, map, "SmallFireball");
      targetInTile(schema, map, "ThrownEnderpearl");
      schema.registerSimple(map, "EyeOfEnderSignal");
      schema.register(map, "ThrownPotion", (name) -> {
         return DSL.optionalFields("inTile", TypeReferences.BLOCK_NAME.in(schema), "Potion", TypeReferences.ITEM_STACK.in(schema));
      });
      targetInTile(schema, map, "ThrownExpBottle");
      schema.register(map, "ItemFrame", (name) -> {
         return DSL.optionalFields("Item", TypeReferences.ITEM_STACK.in(schema));
      });
      targetInTile(schema, map, "WitherSkull");
      schema.registerSimple(map, "PrimedTnt");
      schema.register(map, "FallingSand", (name) -> {
         return DSL.optionalFields("Block", TypeReferences.BLOCK_NAME.in(schema), "TileEntityData", TypeReferences.BLOCK_ENTITY.in(schema));
      });
      schema.register(map, "FireworksRocketEntity", (name) -> {
         return DSL.optionalFields("FireworksItem", TypeReferences.ITEM_STACK.in(schema));
      });
      schema.registerSimple(map, "Boat");
      schema.register(map, "Minecart", () -> {
         return DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
      });
      targetDisplayTile(schema, map, "MinecartRideable");
      schema.register(map, "MinecartChest", (name) -> {
         return DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
      });
      targetDisplayTile(schema, map, "MinecartFurnace");
      targetDisplayTile(schema, map, "MinecartTNT");
      schema.register(map, "MinecartSpawner", () -> {
         return DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema), TypeReferences.UNTAGGED_SPAWNER.in(schema));
      });
      schema.register(map, "MinecartHopper", (name) -> {
         return DSL.optionalFields("DisplayTile", TypeReferences.BLOCK_NAME.in(schema), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
      });
      targetDisplayTile(schema, map, "MinecartCommandBlock");
      targetEquipment(schema, map, "ArmorStand");
      targetEquipment(schema, map, "Creeper");
      targetEquipment(schema, map, "Skeleton");
      targetEquipment(schema, map, "Spider");
      targetEquipment(schema, map, "Giant");
      targetEquipment(schema, map, "Zombie");
      targetEquipment(schema, map, "Slime");
      targetEquipment(schema, map, "Ghast");
      targetEquipment(schema, map, "PigZombie");
      schema.register(map, "Enderman", (name) -> {
         return DSL.optionalFields("carried", TypeReferences.BLOCK_NAME.in(schema), targetEquipment(schema));
      });
      targetEquipment(schema, map, "CaveSpider");
      targetEquipment(schema, map, "Silverfish");
      targetEquipment(schema, map, "Blaze");
      targetEquipment(schema, map, "LavaSlime");
      targetEquipment(schema, map, "EnderDragon");
      targetEquipment(schema, map, "WitherBoss");
      targetEquipment(schema, map, "Bat");
      targetEquipment(schema, map, "Witch");
      targetEquipment(schema, map, "Endermite");
      targetEquipment(schema, map, "Guardian");
      targetEquipment(schema, map, "Pig");
      targetEquipment(schema, map, "Sheep");
      targetEquipment(schema, map, "Cow");
      targetEquipment(schema, map, "Chicken");
      targetEquipment(schema, map, "Squid");
      targetEquipment(schema, map, "Wolf");
      targetEquipment(schema, map, "MushroomCow");
      targetEquipment(schema, map, "SnowMan");
      targetEquipment(schema, map, "Ozelot");
      targetEquipment(schema, map, "VillagerGolem");
      schema.register(map, "EntityHorse", (name) -> {
         return DSL.optionalFields("Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "ArmorItem", TypeReferences.ITEM_STACK.in(schema), "SaddleItem", TypeReferences.ITEM_STACK.in(schema), targetEquipment(schema));
      });
      targetEquipment(schema, map, "Rabbit");
      schema.register(map, "Villager", (name) -> {
         return DSL.optionalFields("Inventory", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", TypeReferences.ITEM_STACK.in(schema), "buyB", TypeReferences.ITEM_STACK.in(schema), "sell", TypeReferences.ITEM_STACK.in(schema)))), targetEquipment(schema));
      });
      schema.registerSimple(map, "EnderCrystal");
      schema.registerSimple(map, "AreaEffectCloud");
      schema.registerSimple(map, "ShulkerBullet");
      targetEquipment(schema, map, "Shulker");
      return map;
   }

   public Map registerBlockEntities(Schema schema) {
      Map map = Maps.newHashMap();
      targetItems(schema, map, "Furnace");
      targetItems(schema, map, "Chest");
      schema.registerSimple(map, "EnderChest");
      schema.register(map, "RecordPlayer", (name) -> {
         return DSL.optionalFields("RecordItem", TypeReferences.ITEM_STACK.in(schema));
      });
      targetItems(schema, map, "Trap");
      targetItems(schema, map, "Dropper");
      schema.registerSimple(map, "Sign");
      schema.register(map, "MobSpawner", (name) -> {
         return TypeReferences.UNTAGGED_SPAWNER.in(schema);
      });
      schema.registerSimple(map, "Music");
      schema.registerSimple(map, "Piston");
      targetItems(schema, map, "Cauldron");
      schema.registerSimple(map, "EnchantTable");
      schema.registerSimple(map, "Airportal");
      schema.registerSimple(map, "Control");
      schema.registerSimple(map, "Beacon");
      schema.registerSimple(map, "Skull");
      schema.registerSimple(map, "DLDetector");
      targetItems(schema, map, "Hopper");
      schema.registerSimple(map, "Comparator");
      schema.register(map, "FlowerPot", (name) -> {
         return DSL.optionalFields("Item", DSL.or(DSL.constType(DSL.intType()), TypeReferences.ITEM_NAME.in(schema)));
      });
      schema.registerSimple(map, "Banner");
      schema.registerSimple(map, "Structure");
      schema.registerSimple(map, "EndGateway");
      return map;
   }

   public void registerTypes(Schema schema, Map entityTypes, Map blockEntityTypes) {
      schema.registerType(false, TypeReferences.LEVEL, DSL::remainder);
      schema.registerType(false, TypeReferences.PLAYER, () -> {
         return DSL.optionalFields("Inventory", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "EnderItems", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
      });
      schema.registerType(false, TypeReferences.CHUNK, () -> {
         return DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(TypeReferences.ENTITY_TREE.in(schema)), "TileEntities", DSL.list(DSL.or(TypeReferences.BLOCK_ENTITY.in(schema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", TypeReferences.BLOCK_NAME.in(schema)))));
      });
      schema.registerType(true, TypeReferences.BLOCK_ENTITY, () -> {
         return DSL.taggedChoiceLazy("id", DSL.string(), blockEntityTypes);
      });
      schema.registerType(true, TypeReferences.ENTITY_TREE, () -> {
         return DSL.optionalFields("Riding", TypeReferences.ENTITY_TREE.in(schema), TypeReferences.ENTITY.in(schema));
      });
      schema.registerType(false, TypeReferences.ENTITY_NAME, () -> {
         return DSL.constType(IdentifierNormalizingSchema.getIdentifierType());
      });
      schema.registerType(true, TypeReferences.ENTITY, () -> {
         return DSL.taggedChoiceLazy("id", DSL.string(), entityTypes);
      });
      schema.registerType(true, TypeReferences.ITEM_STACK, () -> {
         return DSL.hook(DSL.optionalFields("id", DSL.or(DSL.constType(DSL.intType()), TypeReferences.ITEM_NAME.in(schema)), "tag", DSL.optionalFields("EntityTag", TypeReferences.ENTITY_TREE.in(schema), "BlockEntityTag", TypeReferences.BLOCK_ENTITY.in(schema), "CanDestroy", DSL.list(TypeReferences.BLOCK_NAME.in(schema)), "CanPlaceOn", DSL.list(TypeReferences.BLOCK_NAME.in(schema)), "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)))), field_5747, HookFunction.IDENTITY);
      });
      schema.registerType(false, TypeReferences.OPTIONS, DSL::remainder);
      schema.registerType(false, TypeReferences.BLOCK_NAME, () -> {
         return DSL.or(DSL.constType(DSL.intType()), DSL.constType(IdentifierNormalizingSchema.getIdentifierType()));
      });
      schema.registerType(false, TypeReferences.ITEM_NAME, () -> {
         return DSL.constType(IdentifierNormalizingSchema.getIdentifierType());
      });
      schema.registerType(false, TypeReferences.STATS, DSL::remainder);
      schema.registerType(false, TypeReferences.SAVED_DATA, () -> {
         return DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(TypeReferences.STRUCTURE_FEATURE.in(schema)), "Objectives", DSL.list(TypeReferences.OBJECTIVE.in(schema)), "Teams", DSL.list(TypeReferences.TEAM.in(schema))));
      });
      schema.registerType(false, TypeReferences.STRUCTURE_FEATURE, DSL::remainder);
      schema.registerType(false, TypeReferences.OBJECTIVE, DSL::remainder);
      schema.registerType(false, TypeReferences.TEAM, DSL::remainder);
      schema.registerType(true, TypeReferences.UNTAGGED_SPAWNER, DSL::remainder);
      schema.registerType(false, TypeReferences.POI_CHUNK, DSL::remainder);
      schema.registerType(false, TypeReferences.WORLD_GEN_SETTINGS, DSL::remainder);
      schema.registerType(false, TypeReferences.ENTITY_CHUNK, () -> {
         return DSL.optionalFields("Entities", DSL.list(TypeReferences.ENTITY_TREE.in(schema)));
      });
   }

   protected static Object method_5359(Dynamic stack, Map renames, String newArmorStandId) {
      return stack.update("tag", (tag) -> {
         return tag.update("BlockEntityTag", (blockEntityTag) -> {
            String string = (String)stack.get("id").asString().result().map(IdentifierNormalizingSchema::normalize).orElse("minecraft:air");
            if (!"minecraft:air".equals(string)) {
               String string2 = (String)renames.get(string);
               if (string2 != null) {
                  return blockEntityTag.set("id", stack.createString(string2));
               }

               LOGGER.warn("Unable to resolve BlockEntity for ItemStack: {}", string);
            }

            return blockEntityTag;
         }).update("EntityTag", (entityTag) -> {
            String string2 = stack.get("id").asString("");
            return "minecraft:armor_stand".equals(IdentifierNormalizingSchema.normalize(string2)) ? entityTag.set("id", stack.createString(newArmorStandId)) : entityTag;
         });
      }).getValue();
   }
}
