package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ConduitBlockEntity extends BlockEntity {
   private static final int field_31333 = 2;
   private static final int field_31334 = 13;
   private static final float field_31335 = -0.0375F;
   private static final int field_31336 = 16;
   private static final int MIN_BLOCKS_TO_ACTIVATE = 42;
   private static final int field_31338 = 8;
   private static final Block[] ACTIVATING_BLOCKS;
   public int ticks;
   private float ticksActive;
   private boolean active;
   private boolean eyeOpen;
   private final List activatingBlocks = Lists.newArrayList();
   @Nullable
   private LivingEntity targetEntity;
   @Nullable
   private UUID targetUuid;
   private long nextAmbientSoundTime;

   public ConduitBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.CONDUIT, pos, state);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.containsUuid("Target")) {
         this.targetUuid = nbt.getUuid("Target");
      } else {
         this.targetUuid = null;
      }

   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (this.targetEntity != null) {
         nbt.putUuid("Target", this.targetEntity.getUuid());
      }

   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.createNbt();
   }

   public static void clientTick(World world, BlockPos pos, BlockState state, ConduitBlockEntity blockEntity) {
      ++blockEntity.ticks;
      long l = world.getTime();
      List list = blockEntity.activatingBlocks;
      if (l % 40L == 0L) {
         blockEntity.active = updateActivatingBlocks(world, pos, list);
         openEye(blockEntity, list);
      }

      updateTargetEntity(world, pos, blockEntity);
      spawnNautilusParticles(world, pos, list, blockEntity.targetEntity, blockEntity.ticks);
      if (blockEntity.isActive()) {
         ++blockEntity.ticksActive;
      }

   }

   public static void serverTick(World world, BlockPos pos, BlockState state, ConduitBlockEntity blockEntity) {
      ++blockEntity.ticks;
      long l = world.getTime();
      List list = blockEntity.activatingBlocks;
      if (l % 40L == 0L) {
         boolean bl = updateActivatingBlocks(world, pos, list);
         if (bl != blockEntity.active) {
            SoundEvent lv = bl ? SoundEvents.BLOCK_CONDUIT_ACTIVATE : SoundEvents.BLOCK_CONDUIT_DEACTIVATE;
            world.playSound((PlayerEntity)null, pos, lv, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }

         blockEntity.active = bl;
         openEye(blockEntity, list);
         if (bl) {
            givePlayersEffects(world, pos, list);
            attackHostileEntity(world, pos, state, list, blockEntity);
         }
      }

      if (blockEntity.isActive()) {
         if (l % 80L == 0L) {
            world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_CONDUIT_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }

         if (l > blockEntity.nextAmbientSoundTime) {
            blockEntity.nextAmbientSoundTime = l + 60L + (long)world.getRandom().nextInt(40);
            world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_CONDUIT_AMBIENT_SHORT, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }
      }

   }

   private static void openEye(ConduitBlockEntity blockEntity, List activatingBlocks) {
      blockEntity.setEyeOpen(activatingBlocks.size() >= 42);
   }

   private static boolean updateActivatingBlocks(World world, BlockPos pos, List activatingBlocks) {
      activatingBlocks.clear();

      int i;
      int j;
      int k;
      for(i = -1; i <= 1; ++i) {
         for(j = -1; j <= 1; ++j) {
            for(k = -1; k <= 1; ++k) {
               BlockPos lv = pos.add(i, j, k);
               if (!world.isWater(lv)) {
                  return false;
               }
            }
         }
      }

      for(i = -2; i <= 2; ++i) {
         for(j = -2; j <= 2; ++j) {
            for(k = -2; k <= 2; ++k) {
               int l = Math.abs(i);
               int m = Math.abs(j);
               int n = Math.abs(k);
               if ((l > 1 || m > 1 || n > 1) && (i == 0 && (m == 2 || n == 2) || j == 0 && (l == 2 || n == 2) || k == 0 && (l == 2 || m == 2))) {
                  BlockPos lv2 = pos.add(i, j, k);
                  BlockState lv3 = world.getBlockState(lv2);
                  Block[] var11 = ACTIVATING_BLOCKS;
                  int var12 = var11.length;

                  for(int var13 = 0; var13 < var12; ++var13) {
                     Block lv4 = var11[var13];
                     if (lv3.isOf(lv4)) {
                        activatingBlocks.add(lv2);
                     }
                  }
               }
            }
         }
      }

      return activatingBlocks.size() >= 16;
   }

   private static void givePlayersEffects(World world, BlockPos pos, List activatingBlocks) {
      int i = activatingBlocks.size();
      int j = i / 7 * 16;
      int k = pos.getX();
      int l = pos.getY();
      int m = pos.getZ();
      Box lv = (new Box((double)k, (double)l, (double)m, (double)(k + 1), (double)(l + 1), (double)(m + 1))).expand((double)j).stretch(0.0, (double)world.getHeight(), 0.0);
      List list2 = world.getNonSpectatingEntities(PlayerEntity.class, lv);
      if (!list2.isEmpty()) {
         Iterator var10 = list2.iterator();

         while(var10.hasNext()) {
            PlayerEntity lv2 = (PlayerEntity)var10.next();
            if (pos.isWithinDistance(lv2.getBlockPos(), (double)j) && lv2.isTouchingWaterOrRain()) {
               lv2.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, 260, 0, true, true));
            }
         }

      }
   }

   private static void attackHostileEntity(World world, BlockPos pos, BlockState state, List activatingBlocks, ConduitBlockEntity blockEntity) {
      LivingEntity lv = blockEntity.targetEntity;
      int i = activatingBlocks.size();
      if (i < 42) {
         blockEntity.targetEntity = null;
      } else if (blockEntity.targetEntity == null && blockEntity.targetUuid != null) {
         blockEntity.targetEntity = findTargetEntity(world, pos, blockEntity.targetUuid);
         blockEntity.targetUuid = null;
      } else if (blockEntity.targetEntity == null) {
         List list2 = world.getEntitiesByClass(LivingEntity.class, getAttackZone(pos), (entity) -> {
            return entity instanceof Monster && entity.isTouchingWaterOrRain();
         });
         if (!list2.isEmpty()) {
            blockEntity.targetEntity = (LivingEntity)list2.get(world.random.nextInt(list2.size()));
         }
      } else if (!blockEntity.targetEntity.isAlive() || !pos.isWithinDistance(blockEntity.targetEntity.getBlockPos(), 8.0)) {
         blockEntity.targetEntity = null;
      }

      if (blockEntity.targetEntity != null) {
         world.playSound((PlayerEntity)null, blockEntity.targetEntity.getX(), blockEntity.targetEntity.getY(), blockEntity.targetEntity.getZ(), SoundEvents.BLOCK_CONDUIT_ATTACK_TARGET, SoundCategory.BLOCKS, 1.0F, 1.0F);
         blockEntity.targetEntity.damage(world.getDamageSources().magic(), 4.0F);
      }

      if (lv != blockEntity.targetEntity) {
         world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
      }

   }

   private static void updateTargetEntity(World world, BlockPos pos, ConduitBlockEntity blockEntity) {
      if (blockEntity.targetUuid == null) {
         blockEntity.targetEntity = null;
      } else if (blockEntity.targetEntity == null || !blockEntity.targetEntity.getUuid().equals(blockEntity.targetUuid)) {
         blockEntity.targetEntity = findTargetEntity(world, pos, blockEntity.targetUuid);
         if (blockEntity.targetEntity == null) {
            blockEntity.targetUuid = null;
         }
      }

   }

   private static Box getAttackZone(BlockPos pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      return (new Box((double)i, (double)j, (double)k, (double)(i + 1), (double)(j + 1), (double)(k + 1))).expand(8.0);
   }

   @Nullable
   private static LivingEntity findTargetEntity(World world, BlockPos pos, UUID uuid) {
      List list = world.getEntitiesByClass(LivingEntity.class, getAttackZone(pos), (entity) -> {
         return entity.getUuid().equals(uuid);
      });
      return list.size() == 1 ? (LivingEntity)list.get(0) : null;
   }

   private static void spawnNautilusParticles(World world, BlockPos pos, List activatingBlocks, @Nullable Entity entity, int ticks) {
      Random lv = world.random;
      double d = (double)(MathHelper.sin((float)(ticks + 35) * 0.1F) / 2.0F + 0.5F);
      d = (d * d + d) * 0.30000001192092896;
      Vec3d lv2 = new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 1.5 + d, (double)pos.getZ() + 0.5);
      Iterator var9 = activatingBlocks.iterator();

      float f;
      while(var9.hasNext()) {
         BlockPos lv3 = (BlockPos)var9.next();
         if (lv.nextInt(50) == 0) {
            BlockPos lv4 = lv3.subtract(pos);
            f = -0.5F + lv.nextFloat() + (float)lv4.getX();
            float g = -2.0F + lv.nextFloat() + (float)lv4.getY();
            float h = -0.5F + lv.nextFloat() + (float)lv4.getZ();
            world.addParticle(ParticleTypes.NAUTILUS, lv2.x, lv2.y, lv2.z, (double)f, (double)g, (double)h);
         }
      }

      if (entity != null) {
         Vec3d lv5 = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
         float j = (-0.5F + lv.nextFloat()) * (3.0F + entity.getWidth());
         float k = -1.0F + lv.nextFloat() * entity.getHeight();
         f = (-0.5F + lv.nextFloat()) * (3.0F + entity.getWidth());
         Vec3d lv6 = new Vec3d((double)j, (double)k, (double)f);
         world.addParticle(ParticleTypes.NAUTILUS, lv5.x, lv5.y, lv5.z, lv6.x, lv6.y, lv6.z);
      }

   }

   public boolean isActive() {
      return this.active;
   }

   public boolean isEyeOpen() {
      return this.eyeOpen;
   }

   private void setEyeOpen(boolean eyeOpen) {
      this.eyeOpen = eyeOpen;
   }

   public float getRotation(float tickDelta) {
      return (this.ticksActive + tickDelta) * -0.0375F;
   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }

   static {
      ACTIVATING_BLOCKS = new Block[]{Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE};
   }
}
