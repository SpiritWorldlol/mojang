package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PowderSnowBucketItem extends BlockItem implements FluidModificationItem {
   private final SoundEvent placeSound;

   public PowderSnowBucketItem(Block block, SoundEvent placeSound, Item.Settings settings) {
      super(block, settings);
      this.placeSound = placeSound;
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      ActionResult lv = super.useOnBlock(context);
      PlayerEntity lv2 = context.getPlayer();
      if (lv.isAccepted() && lv2 != null && !lv2.isCreative()) {
         Hand lv3 = context.getHand();
         lv2.setStackInHand(lv3, Items.BUCKET.getDefaultStack());
      }

      return lv;
   }

   public String getTranslationKey() {
      return this.getOrCreateTranslationKey();
   }

   protected SoundEvent getPlaceSound(BlockState state) {
      return this.placeSound;
   }

   public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult) {
      if (world.isInBuildLimit(pos) && world.isAir(pos)) {
         if (!world.isClient) {
            world.setBlockState(pos, this.getBlock().getDefaultState(), Block.NOTIFY_ALL);
         }

         world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
         world.playSound(player, pos, this.placeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }
}
