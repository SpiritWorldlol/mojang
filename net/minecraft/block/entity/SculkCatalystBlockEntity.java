package net.minecraft.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SculkCatalystBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener.Holder {
   private final Listener eventListener;

   public SculkCatalystBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.SCULK_CATALYST, pos, state);
      this.eventListener = new Listener(state, new BlockPositionSource(pos));
   }

   public static void tick(World world, BlockPos pos, BlockState state, SculkCatalystBlockEntity blockEntity) {
      blockEntity.eventListener.getSpreadManager().tick(world, pos, world.getRandom(), true);
   }

   public void readNbt(NbtCompound nbt) {
      this.eventListener.spreadManager.readNbt(nbt);
   }

   protected void writeNbt(NbtCompound nbt) {
      this.eventListener.spreadManager.writeNbt(nbt);
      super.writeNbt(nbt);
   }

   public Listener getEventListener() {
      return this.eventListener;
   }

   // $FF: synthetic method
   public GameEventListener getEventListener() {
      return this.getEventListener();
   }

   public static class Listener implements GameEventListener {
      public static final int RANGE = 8;
      final SculkSpreadManager spreadManager;
      private final BlockState state;
      private final PositionSource positionSource;

      public Listener(BlockState state, PositionSource positionSource) {
         this.state = state;
         this.positionSource = positionSource;
         this.spreadManager = SculkSpreadManager.create();
      }

      public PositionSource getPositionSource() {
         return this.positionSource;
      }

      public int getRange() {
         return 8;
      }

      public GameEventListener.TriggerOrder getTriggerOrder() {
         return GameEventListener.TriggerOrder.BY_DISTANCE;
      }

      public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
         if (event == GameEvent.ENTITY_DIE) {
            Entity var6 = emitter.sourceEntity();
            if (var6 instanceof LivingEntity) {
               LivingEntity lv = (LivingEntity)var6;
               if (!lv.isExperienceDroppingDisabled()) {
                  int i = lv.getXpToDrop();
                  if (lv.shouldDropXp() && i > 0) {
                     this.spreadManager.spread(BlockPos.ofFloored(emitterPos.offset(Direction.UP, 0.5)), i);
                     this.triggerCriteria(world, lv);
                  }

                  lv.disableExperienceDropping();
                  this.positionSource.getPos(world).ifPresent((pos) -> {
                     this.bloom(world, BlockPos.ofFloored(pos), this.state, world.getRandom());
                  });
               }

               return true;
            }
         }

         return false;
      }

      @VisibleForTesting
      public SculkSpreadManager getSpreadManager() {
         return this.spreadManager;
      }

      private void bloom(ServerWorld world, BlockPos pos, BlockState state, Random random) {
         world.setBlockState(pos, (BlockState)state.with(SculkCatalystBlock.BLOOM, true), Block.NOTIFY_ALL);
         world.scheduleBlockTick(pos, state.getBlock(), 8);
         world.spawnParticles(ParticleTypes.SCULK_SOUL, (double)pos.getX() + 0.5, (double)pos.getY() + 1.15, (double)pos.getZ() + 0.5, 2, 0.2, 0.0, 0.2, 0.0);
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_SCULK_CATALYST_BLOOM, SoundCategory.BLOCKS, 2.0F, 0.6F + random.nextFloat() * 0.4F);
      }

      private void triggerCriteria(World world, LivingEntity deadEntity) {
         LivingEntity lv = deadEntity.getAttacker();
         if (lv instanceof ServerPlayerEntity lv2) {
            DamageSource lv3 = deadEntity.getRecentDamageSource() == null ? world.getDamageSources().playerAttack(lv2) : deadEntity.getRecentDamageSource();
            Criteria.KILL_MOB_NEAR_SCULK_CATALYST.trigger(lv2, deadEntity, lv3);
         }

      }
   }
}
