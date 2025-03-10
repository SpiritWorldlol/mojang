package net.minecraft.block;

import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

public class DragonEggBlock extends FallingBlock {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

   public DragonEggBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      this.teleport(state, world, pos);
      return ActionResult.success(world.isClient);
   }

   public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
      this.teleport(state, world, pos);
   }

   private void teleport(BlockState state, World world, BlockPos pos) {
      WorldBorder lv = world.getWorldBorder();

      for(int i = 0; i < 1000; ++i) {
         BlockPos lv2 = pos.add(world.random.nextInt(16) - world.random.nextInt(16), world.random.nextInt(8) - world.random.nextInt(8), world.random.nextInt(16) - world.random.nextInt(16));
         if (world.getBlockState(lv2).isAir() && lv.contains(lv2)) {
            if (world.isClient) {
               for(int j = 0; j < 128; ++j) {
                  double d = world.random.nextDouble();
                  float f = (world.random.nextFloat() - 0.5F) * 0.2F;
                  float g = (world.random.nextFloat() - 0.5F) * 0.2F;
                  float h = (world.random.nextFloat() - 0.5F) * 0.2F;
                  double e = MathHelper.lerp(d, (double)lv2.getX(), (double)pos.getX()) + (world.random.nextDouble() - 0.5) + 0.5;
                  double k = MathHelper.lerp(d, (double)lv2.getY(), (double)pos.getY()) + world.random.nextDouble() - 0.5;
                  double l = MathHelper.lerp(d, (double)lv2.getZ(), (double)pos.getZ()) + (world.random.nextDouble() - 0.5) + 0.5;
                  world.addParticle(ParticleTypes.PORTAL, e, k, l, (double)f, (double)g, (double)h);
               }
            } else {
               world.setBlockState(lv2, state, Block.NOTIFY_LISTENERS);
               world.removeBlock(pos, false);
            }

            return;
         }
      }

   }

   protected int getFallDelay() {
      return 5;
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }
}
