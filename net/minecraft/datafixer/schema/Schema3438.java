package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import java.util.function.Supplier;

public class Schema3438 extends IdentifierNormalizingSchema {
   public Schema3438(int i, Schema schema) {
      super(i, schema);
   }

   public Map registerBlockEntities(Schema schema) {
      Map map = super.registerBlockEntities(schema);
      map.put("minecraft:brushable_block", (Supplier)map.remove("minecraft:suspicious_sand"));
      schema.registerSimple(map, "minecraft:calibrated_sculk_sensor");
      return map;
   }
}
