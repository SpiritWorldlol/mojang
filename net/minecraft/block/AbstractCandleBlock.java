package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCandleBlock extends Block {
   public static final int field_30987 = 3;
   public static final BooleanProperty LIT;

   protected AbstractCandleBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   protected abstract Iterable getParticleOffsets(BlockState state);

   public static boolean isLitCandle(BlockState state) {
      return state.contains(LIT) && (state.isIn(BlockTags.CANDLES) || state.isIn(BlockTags.CANDLE_CAKES)) && (Boolean)state.get(LIT);
   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
      if (!world.isClient && projectile.isOnFire() && this.isNotLit(state)) {
         setLit(world, state, hit.getBlockPos(), true);
      }

   }

   protected boolean isNotLit(BlockState state) {
      return !(Boolean)state.get(LIT);
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         this.getParticleOffsets(state).forEach((offset) -> {
            spawnCandleParticles(world, offset.add((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), random);
         });
      }
   }

   private static void spawnCandleParticles(World world, Vec3d vec3d, Random random) {
      float f = random.nextFloat();
      if (f < 0.3F) {
         world.addParticle(ParticleTypes.SMOKE, vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 0.0);
         if (f < 0.17F) {
            world.playSound(vec3d.x + 0.5, vec3d.y + 0.5, vec3d.z + 0.5, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
         }
      }

      world.addParticle(ParticleTypes.SMALL_FLAME, vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 0.0);
   }

   public static void extinguish(@Nullable PlayerEntity player, BlockState state, WorldAccess world, BlockPos pos) {
      setLit(world, state, pos, false);
      if (state.getBlock() instanceof AbstractCandleBlock) {
         ((AbstractCandleBlock)state.getBlock()).getParticleOffsets(state).forEach((offset) -> {
            world.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + offset.getX(), (double)pos.getY() + offset.getY(), (double)pos.getZ() + offset.getZ(), 0.0, 0.10000000149011612, 0.0);
         });
      }

      world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
      world.emitGameEvent((Entity)player, (GameEvent)GameEvent.BLOCK_CHANGE, (BlockPos)pos);
   }

   private static void setLit(WorldAccess world, BlockState state, BlockPos pos, boolean lit) {
      world.setBlockState(pos, (BlockState)state.with(LIT, lit), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
   }

   static {
      LIT = Properties.LIT;
   }
}
