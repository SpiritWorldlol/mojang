package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class EntityShulkerColorFix extends ChoiceFix {
   public EntityShulkerColorFix(Schema schema, boolean bl) {
      super(schema, bl, "EntityShulkerColorFix", TypeReferences.ENTITY, "minecraft:shulker");
   }

   public Dynamic fixShulkerColor(Dynamic dynamic) {
      return !dynamic.get("Color").map(Dynamic::asNumber).result().isPresent() ? dynamic.set("Color", dynamic.createByte((byte)10)) : dynamic;
   }

   protected Typed transform(Typed inputType) {
      return inputType.update(DSL.remainderFinder(), this::fixShulkerColor);
   }
}
