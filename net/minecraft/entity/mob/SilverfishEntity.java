package net.minecraft.entity.mob;

import java.util.EnumSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.InfestedBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.PowderSnowJumpGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class SilverfishEntity extends HostileEntity {
   @Nullable
   private CallForHelpGoal callForHelpGoal;

   public SilverfishEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   protected void initGoals() {
      this.callForHelpGoal = new CallForHelpGoal(this);
      this.goalSelector.add(1, new SwimGoal(this));
      this.goalSelector.add(1, new PowderSnowJumpGoal(this, this.getWorld()));
      this.goalSelector.add(3, this.callForHelpGoal);
      this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0, false));
      this.goalSelector.add(5, new WanderAndInfestGoal(this));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[0])).setGroupRevenge());
      this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, true));
   }

   public double getHeightOffset() {
      return 0.1;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.13F;
   }

   public static DefaultAttributeContainer.Builder createSilverfishAttributes() {
      return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0);
   }

   protected Entity.MoveEffect getMoveEffect() {
      return Entity.MoveEffect.EVENTS;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SILVERFISH_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_SILVERFISH_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SILVERFISH_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_SILVERFISH_STEP, 0.15F, 1.0F);
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         if ((source.getAttacker() != null || source.isIn(DamageTypeTags.ALWAYS_TRIGGERS_SILVERFISH)) && this.callForHelpGoal != null) {
            this.callForHelpGoal.onHurt();
         }

         return super.damage(source, amount);
      }
   }

   public void tick() {
      this.bodyYaw = this.getYaw();
      super.tick();
   }

   public void setBodyYaw(float bodyYaw) {
      this.setYaw(bodyYaw);
      super.setBodyYaw(bodyYaw);
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      return InfestedBlock.isInfestable(world.getBlockState(pos.down())) ? 10.0F : super.getPathfindingFavor(pos, world);
   }

   public static boolean canSpawn(EntityType type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      if (canSpawnIgnoreLightLevel(type, world, spawnReason, pos, random)) {
         PlayerEntity lv = world.getClosestPlayer((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 5.0, true);
         return lv == null;
      } else {
         return false;
      }
   }

   public EntityGroup getGroup() {
      return EntityGroup.ARTHROPOD;
   }

   private static class CallForHelpGoal extends Goal {
      private final SilverfishEntity silverfish;
      private int delay;

      public CallForHelpGoal(SilverfishEntity silverfish) {
         this.silverfish = silverfish;
      }

      public void onHurt() {
         if (this.delay == 0) {
            this.delay = this.getTickCount(20);
         }

      }

      public boolean canStart() {
         return this.delay > 0;
      }

      public void tick() {
         --this.delay;
         if (this.delay <= 0) {
            World lv = this.silverfish.getWorld();
            Random lv2 = this.silverfish.getRandom();
            BlockPos lv3 = this.silverfish.getBlockPos();

            for(int i = 0; i <= 5 && i >= -5; i = (i <= 0 ? 1 : 0) - i) {
               for(int j = 0; j <= 10 && j >= -10; j = (j <= 0 ? 1 : 0) - j) {
                  for(int k = 0; k <= 10 && k >= -10; k = (k <= 0 ? 1 : 0) - k) {
                     BlockPos lv4 = lv3.add(j, i, k);
                     BlockState lv5 = lv.getBlockState(lv4);
                     Block lv6 = lv5.getBlock();
                     if (lv6 instanceof InfestedBlock) {
                        if (lv.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                           lv.breakBlock(lv4, true, this.silverfish);
                        } else {
                           lv.setBlockState(lv4, ((InfestedBlock)lv6).toRegularState(lv.getBlockState(lv4)), Block.NOTIFY_ALL);
                        }

                        if (lv2.nextBoolean()) {
                           return;
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private static class WanderAndInfestGoal extends WanderAroundGoal {
      @Nullable
      private Direction direction;
      private boolean canInfest;

      public WanderAndInfestGoal(SilverfishEntity silverfish) {
         super(silverfish, 1.0, 10);
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public boolean canStart() {
         if (this.mob.getTarget() != null) {
            return false;
         } else if (!this.mob.getNavigation().isIdle()) {
            return false;
         } else {
            Random lv = this.mob.getRandom();
            if (this.mob.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && lv.nextInt(toGoalTicks(10)) == 0) {
               this.direction = Direction.random(lv);
               BlockPos lv2 = BlockPos.ofFloored(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()).offset(this.direction);
               BlockState lv3 = this.mob.getWorld().getBlockState(lv2);
               if (InfestedBlock.isInfestable(lv3)) {
                  this.canInfest = true;
                  return true;
               }
            }

            this.canInfest = false;
            return super.canStart();
         }
      }

      public boolean shouldContinue() {
         return this.canInfest ? false : super.shouldContinue();
      }

      public void start() {
         if (!this.canInfest) {
            super.start();
         } else {
            WorldAccess lv = this.mob.getWorld();
            BlockPos lv2 = BlockPos.ofFloored(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()).offset(this.direction);
            BlockState lv3 = lv.getBlockState(lv2);
            if (InfestedBlock.isInfestable(lv3)) {
               lv.setBlockState(lv2, InfestedBlock.fromRegularState(lv3), Block.NOTIFY_ALL);
               this.mob.playSpawnEffects();
               this.mob.discard();
            }

         }
      }
   }
}
