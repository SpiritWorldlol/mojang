package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SlimeBlock extends TransparentBlock {
   public SlimeBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
      if (entity.bypassesLandingEffects()) {
         super.onLandedUpon(world, state, pos, entity, fallDistance);
      } else {
         entity.handleFallDamage(fallDistance, 0.0F, world.getDamageSources().fall());
      }

   }

   public void onEntityLand(BlockView world, Entity entity) {
      if (entity.bypassesLandingEffects()) {
         super.onEntityLand(world, entity);
      } else {
         this.bounce(entity);
      }

   }

   private void bounce(Entity entity) {
      Vec3d lv = entity.getVelocity();
      if (lv.y < 0.0) {
         double d = entity instanceof LivingEntity ? 1.0 : 0.8;
         entity.setVelocity(lv.x, -lv.y * d, lv.z);
      }

   }

   public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
      double d = Math.abs(entity.getVelocity().y);
      if (d < 0.1 && !entity.bypassesSteppingEffects()) {
         double e = 0.4 + d * 0.2;
         entity.setVelocity(entity.getVelocity().multiply(e, 1.0, e));
      }

      super.onSteppedOn(world, pos, state, entity);
   }
}
