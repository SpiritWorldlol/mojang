package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class WeaponsmithChestLootTableFix extends ChoiceFix {
   public WeaponsmithChestLootTableFix(Schema schema, boolean bl) {
      super(schema, bl, "WeaponSmithChestLootTableFix", TypeReferences.BLOCK_ENTITY, "minecraft:chest");
   }

   protected Typed transform(Typed inputType) {
      return inputType.update(DSL.remainderFinder(), (dynamic) -> {
         String string = dynamic.get("LootTable").asString("");
         return string.equals("minecraft:chests/village_blacksmith") ? dynamic.set("LootTable", dynamic.createString("minecraft:chests/village/village_weaponsmith")) : dynamic;
      });
   }
}
