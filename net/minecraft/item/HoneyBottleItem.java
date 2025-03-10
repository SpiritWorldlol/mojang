package net.minecraft.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class HoneyBottleItem extends Item {
   private static final int MAX_USE_TIME = 40;

   public HoneyBottleItem(Item.Settings arg) {
      super(arg);
   }

   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
      super.finishUsing(stack, world, user);
      if (user instanceof ServerPlayerEntity lv) {
         Criteria.CONSUME_ITEM.trigger(lv, stack);
         lv.incrementStat(Stats.USED.getOrCreateStat(this));
      }

      if (!world.isClient) {
         user.removeStatusEffect(StatusEffects.POISON);
      }

      if (stack.isEmpty()) {
         return new ItemStack(Items.GLASS_BOTTLE);
      } else {
         if (user instanceof PlayerEntity && !((PlayerEntity)user).getAbilities().creativeMode) {
            ItemStack lv2 = new ItemStack(Items.GLASS_BOTTLE);
            PlayerEntity lv3 = (PlayerEntity)user;
            if (!lv3.getInventory().insertStack(lv2)) {
               lv3.dropItem(lv2, false);
            }
         }

         return stack;
      }
   }

   public int getMaxUseTime(ItemStack stack) {
      return 40;
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.DRINK;
   }

   public SoundEvent getDrinkSound() {
      return SoundEvents.ITEM_HONEY_BOTTLE_DRINK;
   }

   public SoundEvent getEatSound() {
      return SoundEvents.ITEM_HONEY_BOTTLE_DRINK;
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      return ItemUsage.consumeHeldItem(world, user, hand);
   }
}
