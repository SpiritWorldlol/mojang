package net.minecraft.world.gen.blockpredicate;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

class AllOfBlockPredicate extends CombinedBlockPredicate {
   public static final Codec CODEC = buildCodec(AllOfBlockPredicate::new);

   public AllOfBlockPredicate(List list) {
      super(list);
   }

   public boolean test(StructureWorldAccess arg, BlockPos arg2) {
      Iterator var3 = this.predicates.iterator();

      BlockPredicate lv;
      do {
         if (!var3.hasNext()) {
            return true;
         }

         lv = (BlockPredicate)var3.next();
      } while(lv.test(arg, arg2));

      return false;
   }

   public BlockPredicateType getType() {
      return BlockPredicateType.ALL_OF;
   }

   // $FF: synthetic method
   public boolean test(Object world, Object pos) {
      return this.test((StructureWorldAccess)world, (BlockPos)pos);
   }
}
