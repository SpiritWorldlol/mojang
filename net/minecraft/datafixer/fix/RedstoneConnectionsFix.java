package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class RedstoneConnectionsFix extends DataFix {
   public RedstoneConnectionsFix(Schema outputSchema) {
      super(outputSchema, false);
   }

   protected TypeRewriteRule makeRule() {
      Schema schema = this.getInputSchema();
      return this.fixTypeEverywhereTyped("RedstoneConnectionsFix", schema.getType(TypeReferences.BLOCK_STATE), (typed) -> {
         return typed.update(DSL.remainderFinder(), this::updateBlockState);
      });
   }

   private Dynamic updateBlockState(Dynamic dynamic) {
      boolean bl = dynamic.get("Name").asString().result().filter("minecraft:redstone_wire"::equals).isPresent();
      return !bl ? dynamic : dynamic.update("Properties", (dynamicx) -> {
         String string = dynamicx.get("east").asString("none");
         String string2 = dynamicx.get("west").asString("none");
         String string3 = dynamicx.get("north").asString("none");
         String string4 = dynamicx.get("south").asString("none");
         boolean bl = hasObsoleteValue(string) || hasObsoleteValue(string2);
         boolean bl2 = hasObsoleteValue(string3) || hasObsoleteValue(string4);
         String string5 = !hasObsoleteValue(string) && !bl2 ? "side" : string;
         String string6 = !hasObsoleteValue(string2) && !bl2 ? "side" : string2;
         String string7 = !hasObsoleteValue(string3) && !bl ? "side" : string3;
         String string8 = !hasObsoleteValue(string4) && !bl ? "side" : string4;
         return dynamicx.update("east", (dynamic) -> {
            return dynamic.createString(string5);
         }).update("west", (dynamic) -> {
            return dynamic.createString(string6);
         }).update("north", (dynamic) -> {
            return dynamic.createString(string7);
         }).update("south", (dynamic) -> {
            return dynamic.createString(string8);
         });
      });
   }

   private static boolean hasObsoleteValue(String string) {
      return !"none".equals(string);
   }
}
