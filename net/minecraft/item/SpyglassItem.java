package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class SpyglassItem extends Item {
   public static final int MAX_USE_TIME = 1200;
   public static final float field_30922 = 0.1F;

   public SpyglassItem(Item.Settings arg) {
      super(arg);
   }

   public int getMaxUseTime(ItemStack stack) {
      return 1200;
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.SPYGLASS;
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      user.playSound(SoundEvents.ITEM_SPYGLASS_USE, 1.0F, 1.0F);
      user.incrementStat(Stats.USED.getOrCreateStat(this));
      return ItemUsage.consumeHeldItem(world, user, hand);
   }

   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
      this.playStopUsingSound(user);
      return stack;
   }

   public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
      this.playStopUsingSound(user);
   }

   private void playStopUsingSound(LivingEntity user) {
      user.playSound(SoundEvents.ITEM_SPYGLASS_STOP_USING, 1.0F, 1.0F);
   }
}
