package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class BlockEntityShulkerBoxColorFix extends ChoiceFix {
   public BlockEntityShulkerBoxColorFix(Schema schema, boolean bl) {
      super(schema, bl, "BlockEntityShulkerBoxColorFix", TypeReferences.BLOCK_ENTITY, "minecraft:shulker_box");
   }

   protected Typed transform(Typed inputType) {
      return inputType.update(DSL.remainderFinder(), (dynamic) -> {
         return dynamic.remove("Color");
      });
   }
}
