package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import net.minecraft.datafixer.TypeReferences;

public class Schema1928 extends IdentifierNormalizingSchema {
   public Schema1928(int i, Schema schema) {
      super(i, schema);
   }

   protected static TypeTemplate targetItems(Schema schema) {
      return DSL.optionalFields("ArmorItems", DSL.list(TypeReferences.ITEM_STACK.in(schema)), "HandItems", DSL.list(TypeReferences.ITEM_STACK.in(schema)));
   }

   protected static void targetEntityItems(Schema schema, Map map, String entityId) {
      schema.register(map, entityId, () -> {
         return targetItems(schema);
      });
   }

   public Map registerEntities(Schema schema) {
      Map map = super.registerEntities(schema);
      map.remove("minecraft:illager_beast");
      targetEntityItems(schema, map, "minecraft:ravager");
      return map;
   }
}
