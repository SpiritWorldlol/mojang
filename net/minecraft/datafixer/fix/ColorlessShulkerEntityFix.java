package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class ColorlessShulkerEntityFix extends ChoiceFix {
   public ColorlessShulkerEntityFix(Schema schema, boolean bl) {
      super(schema, bl, "Colorless shulker entity fix", TypeReferences.ENTITY, "minecraft:shulker");
   }

   protected Typed transform(Typed inputType) {
      return inputType.update(DSL.remainderFinder(), (dynamic) -> {
         return dynamic.get("Color").asInt(0) == 10 ? dynamic.set("Color", dynamic.createByte((byte)16)) : dynamic;
      });
   }
}
