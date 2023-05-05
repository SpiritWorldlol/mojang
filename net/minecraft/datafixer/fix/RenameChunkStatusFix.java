package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.TypeReferences;

public class RenameChunkStatusFix extends DataFix {
   private final String name;
   private final UnaryOperator mapper;

   public RenameChunkStatusFix(Schema schema, String name, UnaryOperator mapper) {
      super(schema, false);
      this.name = name;
      this.mapper = mapper;
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(TypeReferences.CHUNK), (typed) -> {
         return typed.update(DSL.remainderFinder(), (chunk) -> {
            return chunk.update("Status", this::updateStatus).update("below_zero_retrogen", (dynamic) -> {
               return dynamic.update("target_status", this::updateStatus);
            });
         });
      });
   }

   private Dynamic updateStatus(Dynamic status) {
      Optional var10000 = status.asString().result().map(this.mapper);
      Objects.requireNonNull(status);
      return (Dynamic)DataFixUtils.orElse(var10000.map(status::createString), status);
   }
}
