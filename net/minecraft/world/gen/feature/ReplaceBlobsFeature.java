package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.jetbrains.annotations.Nullable;

public class ReplaceBlobsFeature extends Feature {
   public ReplaceBlobsFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      ReplaceBlobsFeatureConfig lv = (ReplaceBlobsFeatureConfig)context.getConfig();
      StructureWorldAccess lv2 = context.getWorld();
      Random lv3 = context.getRandom();
      Block lv4 = lv.target.getBlock();
      BlockPos lv5 = moveDownToTarget(lv2, context.getOrigin().mutableCopy().clamp(Direction.Axis.Y, lv2.getBottomY() + 1, lv2.getTopY() - 1), lv4);
      if (lv5 == null) {
         return false;
      } else {
         int i = lv.getRadius().get(lv3);
         int j = lv.getRadius().get(lv3);
         int k = lv.getRadius().get(lv3);
         int l = Math.max(i, Math.max(j, k));
         boolean bl = false;
         Iterator var12 = BlockPos.iterateOutwards(lv5, i, j, k).iterator();

         while(var12.hasNext()) {
            BlockPos lv6 = (BlockPos)var12.next();
            if (lv6.getManhattanDistance(lv5) > l) {
               break;
            }

            BlockState lv7 = lv2.getBlockState(lv6);
            if (lv7.isOf(lv4)) {
               this.setBlockState(lv2, lv6, lv.state);
               bl = true;
            }
         }

         return bl;
      }
   }

   @Nullable
   private static BlockPos moveDownToTarget(WorldAccess world, BlockPos.Mutable mutablePos, Block target) {
      while(mutablePos.getY() > world.getBottomY() + 1) {
         BlockState lv = world.getBlockState(mutablePos);
         if (lv.isOf(target)) {
            return mutablePos;
         }

         mutablePos.move(Direction.DOWN);
      }

      return null;
   }
}
