package net.minecraft.datafixer.fix;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Identifier;

public class EntityPaintingMotiveFix extends ChoiceFix {
   private static final Map RENAMED_MOTIVES = (Map)DataFixUtils.make(Maps.newHashMap(), (map) -> {
      map.put("donkeykong", "donkey_kong");
      map.put("burningskull", "burning_skull");
      map.put("skullandroses", "skull_and_roses");
   });

   public EntityPaintingMotiveFix(Schema schema, boolean bl) {
      super(schema, bl, "EntityPaintingMotiveFix", TypeReferences.ENTITY, "minecraft:painting");
   }

   public Dynamic renameMotive(Dynamic dynamic) {
      Optional optional = dynamic.get("Motive").asString().result();
      if (optional.isPresent()) {
         String string = ((String)optional.get()).toLowerCase(Locale.ROOT);
         return dynamic.set("Motive", dynamic.createString((new Identifier((String)RENAMED_MOTIVES.getOrDefault(string, string))).toString()));
      } else {
         return dynamic;
      }
   }

   protected Typed transform(Typed inputType) {
      return inputType.update(DSL.remainderFinder(), this::renameMotive);
   }
}
