package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SmokerBlock extends AbstractFurnaceBlock {
   protected SmokerBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new SmokerBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return checkType(world, type, BlockEntityType.SMOKER);
   }

   protected void openScreen(World world, BlockPos pos, PlayerEntity player) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof SmokerBlockEntity) {
         player.openHandledScreen((NamedScreenHandlerFactory)lv);
         player.incrementStat(Stats.INTERACT_WITH_SMOKER);
      }

   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         double d = (double)pos.getX() + 0.5;
         double e = (double)pos.getY();
         double f = (double)pos.getZ() + 0.5;
         if (random.nextDouble() < 0.1) {
            world.playSound(d, e, f, SoundEvents.BLOCK_SMOKER_SMOKE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
         }

         world.addParticle(ParticleTypes.SMOKE, d, e + 1.1, f, 0.0, 0.0, 0.0);
      }
   }
}
