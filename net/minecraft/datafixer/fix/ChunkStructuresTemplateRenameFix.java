package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class ChunkStructuresTemplateRenameFix extends DataFix {
   private static final ImmutableMap STRUCTURES = ImmutableMap.builder().put("EndCity", Pair.of("ECP", ImmutableMap.builder().put("second_floor", "second_floor_1").put("third_floor", "third_floor_1").put("third_floor_c", "third_floor_2").build())).put("Mansion", Pair.of("WMP", ImmutableMap.builder().put("carpet_south", "carpet_south_1").put("carpet_west", "carpet_west_1").put("indoors_door", "indoors_door_1").put("indoors_wall", "indoors_wall_1").build())).put("Igloo", Pair.of("Iglu", ImmutableMap.builder().put("minecraft:igloo/igloo_bottom", "minecraft:igloo/bottom").put("minecraft:igloo/igloo_middle", "minecraft:igloo/middle").put("minecraft:igloo/igloo_top", "minecraft:igloo/top").build())).put("Ocean_Ruin", Pair.of("ORP", ImmutableMap.builder().put("minecraft:ruin/big_ruin1_brick", "minecraft:underwater_ruin/big_brick_1").put("minecraft:ruin/big_ruin2_brick", "minecraft:underwater_ruin/big_brick_2").put("minecraft:ruin/big_ruin3_brick", "minecraft:underwater_ruin/big_brick_3").put("minecraft:ruin/big_ruin8_brick", "minecraft:underwater_ruin/big_brick_8").put("minecraft:ruin/big_ruin1_cracked", "minecraft:underwater_ruin/big_cracked_1").put("minecraft:ruin/big_ruin2_cracked", "minecraft:underwater_ruin/big_cracked_2").put("minecraft:ruin/big_ruin3_cracked", "minecraft:underwater_ruin/big_cracked_3").put("minecraft:ruin/big_ruin8_cracked", "minecraft:underwater_ruin/big_cracked_8").put("minecraft:ruin/big_ruin1_mossy", "minecraft:underwater_ruin/big_mossy_1").put("minecraft:ruin/big_ruin2_mossy", "minecraft:underwater_ruin/big_mossy_2").put("minecraft:ruin/big_ruin3_mossy", "minecraft:underwater_ruin/big_mossy_3").put("minecraft:ruin/big_ruin8_mossy", "minecraft:underwater_ruin/big_mossy_8").put("minecraft:ruin/big_ruin_warm4", "minecraft:underwater_ruin/big_warm_4").put("minecraft:ruin/big_ruin_warm5", "minecraft:underwater_ruin/big_warm_5").put("minecraft:ruin/big_ruin_warm6", "minecraft:underwater_ruin/big_warm_6").put("minecraft:ruin/big_ruin_warm7", "minecraft:underwater_ruin/big_warm_7").put("minecraft:ruin/ruin1_brick", "minecraft:underwater_ruin/brick_1").put("minecraft:ruin/ruin2_brick", "minecraft:underwater_ruin/brick_2").put("minecraft:ruin/ruin3_brick", "minecraft:underwater_ruin/brick_3").put("minecraft:ruin/ruin4_brick", "minecraft:underwater_ruin/brick_4").put("minecraft:ruin/ruin5_brick", "minecraft:underwater_ruin/brick_5").put("minecraft:ruin/ruin6_brick", "minecraft:underwater_ruin/brick_6").put("minecraft:ruin/ruin7_brick", "minecraft:underwater_ruin/brick_7").put("minecraft:ruin/ruin8_brick", "minecraft:underwater_ruin/brick_8").put("minecraft:ruin/ruin1_cracked", "minecraft:underwater_ruin/cracked_1").put("minecraft:ruin/ruin2_cracked", "minecraft:underwater_ruin/cracked_2").put("minecraft:ruin/ruin3_cracked", "minecraft:underwater_ruin/cracked_3").put("minecraft:ruin/ruin4_cracked", "minecraft:underwater_ruin/cracked_4").put("minecraft:ruin/ruin5_cracked", "minecraft:underwater_ruin/cracked_5").put("minecraft:ruin/ruin6_cracked", "minecraft:underwater_ruin/cracked_6").put("minecraft:ruin/ruin7_cracked", "minecraft:underwater_ruin/cracked_7").put("minecraft:ruin/ruin8_cracked", "minecraft:underwater_ruin/cracked_8").put("minecraft:ruin/ruin1_mossy", "minecraft:underwater_ruin/mossy_1").put("minecraft:ruin/ruin2_mossy", "minecraft:underwater_ruin/mossy_2").put("minecraft:ruin/ruin3_mossy", "minecraft:underwater_ruin/mossy_3").put("minecraft:ruin/ruin4_mossy", "minecraft:underwater_ruin/mossy_4").put("minecraft:ruin/ruin5_mossy", "minecraft:underwater_ruin/mossy_5").put("minecraft:ruin/ruin6_mossy", "minecraft:underwater_ruin/mossy_6").put("minecraft:ruin/ruin7_mossy", "minecraft:underwater_ruin/mossy_7").put("minecraft:ruin/ruin8_mossy", "minecraft:underwater_ruin/mossy_8").put("minecraft:ruin/ruin_warm1", "minecraft:underwater_ruin/warm_1").put("minecraft:ruin/ruin_warm2", "minecraft:underwater_ruin/warm_2").put("minecraft:ruin/ruin_warm3", "minecraft:underwater_ruin/warm_3").put("minecraft:ruin/ruin_warm4", "minecraft:underwater_ruin/warm_4").put("minecraft:ruin/ruin_warm5", "minecraft:underwater_ruin/warm_5").put("minecraft:ruin/ruin_warm6", "minecraft:underwater_ruin/warm_6").put("minecraft:ruin/ruin_warm7", "minecraft:underwater_ruin/warm_7").put("minecraft:ruin/ruin_warm8", "minecraft:underwater_ruin/warm_8").put("minecraft:ruin/big_brick_1", "minecraft:underwater_ruin/big_brick_1").put("minecraft:ruin/big_brick_2", "minecraft:underwater_ruin/big_brick_2").put("minecraft:ruin/big_brick_3", "minecraft:underwater_ruin/big_brick_3").put("minecraft:ruin/big_brick_8", "minecraft:underwater_ruin/big_brick_8").put("minecraft:ruin/big_mossy_1", "minecraft:underwater_ruin/big_mossy_1").put("minecraft:ruin/big_mossy_2", "minecraft:underwater_ruin/big_mossy_2").put("minecraft:ruin/big_mossy_3", "minecraft:underwater_ruin/big_mossy_3").put("minecraft:ruin/big_mossy_8", "minecraft:underwater_ruin/big_mossy_8").put("minecraft:ruin/big_cracked_1", "minecraft:underwater_ruin/big_cracked_1").put("minecraft:ruin/big_cracked_2", "minecraft:underwater_ruin/big_cracked_2").put("minecraft:ruin/big_cracked_3", "minecraft:underwater_ruin/big_cracked_3").put("minecraft:ruin/big_cracked_8", "minecraft:underwater_ruin/big_cracked_8").put("minecraft:ruin/big_warm_4", "minecraft:underwater_ruin/big_warm_4").put("minecraft:ruin/big_warm_5", "minecraft:underwater_ruin/big_warm_5").put("minecraft:ruin/big_warm_6", "minecraft:underwater_ruin/big_warm_6").put("minecraft:ruin/big_warm_7", "minecraft:underwater_ruin/big_warm_7").build())).build();

   public ChunkStructuresTemplateRenameFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   public TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
      return this.fixTypeEverywhereTyped("ChunkStructuresTemplateRenameFix", type, (typed) -> {
         return typed.update(DSL.remainderFinder(), this::method_49459);
      });
   }

   private Dynamic method_49459(Dynamic dynamic) {
      return dynamic.update("Children", (dynamic2) -> {
         return dynamic.createList(dynamic2.asStream().map((dynamic2x) -> {
            return this.fix(dynamic, dynamic2x);
         }));
      });
   }

   private Dynamic fix(Dynamic dynamic, Dynamic dynamic2) {
      String string = dynamic.get("id").asString("");
      if (STRUCTURES.containsKey(string)) {
         Pair pair = (Pair)STRUCTURES.get(string);
         if (((String)pair.getFirst()).equals(dynamic2.get("id").asString(""))) {
            String string2 = dynamic2.get("Template").asString("");
            dynamic2 = dynamic2.set("Template", dynamic2.createString((String)((ImmutableMap)pair.getSecond()).getOrDefault(string2, string2)));
         }
      }

      return dynamic2;
   }
}
