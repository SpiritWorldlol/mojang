package net.minecraft.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SwordItem extends ToolItem implements Vanishable {
   private final float attackDamage;
   private final Multimap attributeModifiers;

   public SwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Item.Settings settings) {
      super(toolMaterial, settings);
      this.attackDamage = (float)attackDamage + toolMaterial.getAttackDamage();
      ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
      builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", (double)this.attackDamage, EntityAttributeModifier.Operation.ADDITION));
      builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", (double)attackSpeed, EntityAttributeModifier.Operation.ADDITION));
      this.attributeModifiers = builder.build();
   }

   public float getAttackDamage() {
      return this.attackDamage;
   }

   public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
      return !miner.isCreative();
   }

   public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
      if (state.isOf(Blocks.COBWEB)) {
         return 15.0F;
      } else {
         return state.isIn(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
      }
   }

   public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.damage(1, (LivingEntity)attacker, (Consumer)((e) -> {
         e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
      }));
      return true;
   }

   public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
      if (state.getHardness(world, pos) != 0.0F) {
         stack.damage(2, (LivingEntity)miner, (Consumer)((e) -> {
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
         }));
      }

      return true;
   }

   public boolean isSuitableFor(BlockState state) {
      return state.isOf(Blocks.COBWEB);
   }

   public Multimap getAttributeModifiers(EquipmentSlot slot) {
      return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
   }
}
