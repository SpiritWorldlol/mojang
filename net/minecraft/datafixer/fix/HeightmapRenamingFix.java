package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class HeightmapRenamingFix extends DataFix {
   public HeightmapRenamingFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   protected TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.CHUNK);
      OpticFinder opticFinder = type.findField("Level");
      return this.fixTypeEverywhereTyped("HeightmapRenamingFix", type, (typed) -> {
         return typed.updateTyped(opticFinder, (typedx) -> {
            return typedx.update(DSL.remainderFinder(), this::renameHeightmapTags);
         });
      });
   }

   private Dynamic renameHeightmapTags(Dynamic dynamic) {
      Optional optional = dynamic.get("Heightmaps").result();
      if (!optional.isPresent()) {
         return dynamic;
      } else {
         Dynamic dynamic2 = (Dynamic)optional.get();
         Optional optional2 = dynamic2.get("LIQUID").result();
         if (optional2.isPresent()) {
            dynamic2 = dynamic2.remove("LIQUID");
            dynamic2 = dynamic2.set("WORLD_SURFACE_WG", (Dynamic)optional2.get());
         }

         Optional optional3 = dynamic2.get("SOLID").result();
         if (optional3.isPresent()) {
            dynamic2 = dynamic2.remove("SOLID");
            dynamic2 = dynamic2.set("OCEAN_FLOOR_WG", (Dynamic)optional3.get());
            dynamic2 = dynamic2.set("OCEAN_FLOOR", (Dynamic)optional3.get());
         }

         Optional optional4 = dynamic2.get("LIGHT").result();
         if (optional4.isPresent()) {
            dynamic2 = dynamic2.remove("LIGHT");
            dynamic2 = dynamic2.set("LIGHT_BLOCKING", (Dynamic)optional4.get());
         }

         Optional optional5 = dynamic2.get("RAIN").result();
         if (optional5.isPresent()) {
            dynamic2 = dynamic2.remove("RAIN");
            dynamic2 = dynamic2.set("MOTION_BLOCKING", (Dynamic)optional5.get());
            dynamic2 = dynamic2.set("MOTION_BLOCKING_NO_LEAVES", (Dynamic)optional5.get());
         }

         return dynamic.set("Heightmaps", dynamic2);
      }
   }
}
