package net.minecraft.datafixer.schema;

import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class Schema1801 extends IdentifierNormalizingSchema {
   public Schema1801(int i, Schema schema) {
      super(i, schema);
   }

   public Map registerEntities(Schema schema) {
      Map map = super.registerEntities(schema);
      schema.register(map, "minecraft:illager_beast", () -> {
         return Schema100.targetItems(schema);
      });
      return map;
   }
}
