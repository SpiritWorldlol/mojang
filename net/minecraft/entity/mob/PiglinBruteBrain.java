package net.minecraft.entity.mob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.task.FindEntityTask;
import net.minecraft.entity.ai.brain.task.FindInteractionTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetAngryAtTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.GoToIfNearbyTask;
import net.minecraft.entity.ai.brain.task.GoToNearbyPositionTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobTask;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.util.math.GlobalPos;

public class PiglinBruteBrain {
   private static final int field_30589 = 600;
   private static final int field_30590 = 20;
   private static final double field_30591 = 0.0125;
   private static final int field_30592 = 8;
   private static final int field_30593 = 8;
   private static final double TARGET_RANGE = 12.0;
   private static final float field_30595 = 0.6F;
   private static final int field_30596 = 2;
   private static final int field_30597 = 100;
   private static final int field_30598 = 5;

   protected static Brain create(PiglinBruteEntity piglinBrute, Brain brain) {
      addCoreActivities(piglinBrute, brain);
      addIdleActivities(piglinBrute, brain);
      addFightActivities(piglinBrute, brain);
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.resetPossibleActivities();
      return brain;
   }

   protected static void setCurrentPosAsHome(PiglinBruteEntity piglinBrute) {
      GlobalPos lv = GlobalPos.create(piglinBrute.getWorld().getRegistryKey(), piglinBrute.getBlockPos());
      piglinBrute.getBrain().remember(MemoryModuleType.HOME, (Object)lv);
   }

   private static void addCoreActivities(PiglinBruteEntity piglinBrute, Brain brain) {
      brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90), new WanderAroundTask(), OpenDoorsTask.create(), ForgetAngryAtTargetTask.create()));
   }

   private static void addIdleActivities(PiglinBruteEntity piglinBrute, Brain brain) {
      brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(UpdateAttackTargetTask.create(PiglinBruteBrain::getTarget), getFollowTasks(), getIdleTasks(), FindInteractionTargetTask.create(EntityType.PLAYER, 4)));
   }

   private static void addFightActivities(PiglinBruteEntity piglinBrute, Brain brain) {
      brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(ForgetAttackTargetTask.create((entity) -> {
         return !isTarget(piglinBrute, entity);
      }), RangedApproachTask.create(1.0F), MeleeAttackTask.create(20)), MemoryModuleType.ATTACK_TARGET);
   }

   private static RandomTask getFollowTasks() {
      return new RandomTask(ImmutableList.of(Pair.of(LookAtMobTask.create(EntityType.PLAYER, 8.0F), 1), Pair.of(LookAtMobTask.create(EntityType.PIGLIN, 8.0F), 1), Pair.of(LookAtMobTask.create(EntityType.PIGLIN_BRUTE, 8.0F), 1), Pair.of(LookAtMobTask.create(8.0F), 1), Pair.of(new WaitTask(30, 60), 1)));
   }

   private static RandomTask getIdleTasks() {
      return new RandomTask(ImmutableList.of(Pair.of(StrollTask.create(0.6F), 2), Pair.of(FindEntityTask.create(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(FindEntityTask.create(EntityType.PIGLIN_BRUTE, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(GoToNearbyPositionTask.create(MemoryModuleType.HOME, 0.6F, 2, 100), 2), Pair.of(GoToIfNearbyTask.create(MemoryModuleType.HOME, 0.6F, 5), 2), Pair.of(new WaitTask(30, 60), 1)));
   }

   protected static void tick(PiglinBruteEntity piglinBrute) {
      Brain lv = piglinBrute.getBrain();
      Activity lv2 = (Activity)lv.getFirstPossibleNonCoreActivity().orElse((Object)null);
      lv.resetPossibleActivities((List)ImmutableList.of(Activity.FIGHT, Activity.IDLE));
      Activity lv3 = (Activity)lv.getFirstPossibleNonCoreActivity().orElse((Object)null);
      if (lv2 != lv3) {
         playSoundIfAngry(piglinBrute);
      }

      piglinBrute.setAttacking(lv.hasMemoryModule(MemoryModuleType.ATTACK_TARGET));
   }

   private static boolean isTarget(AbstractPiglinEntity piglin, LivingEntity entity) {
      return getTarget(piglin).filter((target) -> {
         return target == entity;
      }).isPresent();
   }

   private static Optional getTarget(AbstractPiglinEntity piglin) {
      Optional optional = LookTargetUtil.getEntity(piglin, MemoryModuleType.ANGRY_AT);
      if (optional.isPresent() && Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, (LivingEntity)optional.get())) {
         return optional;
      } else {
         Optional optional2 = getTargetIfInRange(piglin, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
         return optional2.isPresent() ? optional2 : piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
      }
   }

   private static Optional getTargetIfInRange(AbstractPiglinEntity piglin, MemoryModuleType targetMemoryModule) {
      return piglin.getBrain().getOptionalRegisteredMemory(targetMemoryModule).filter((target) -> {
         return target.isInRange(piglin, 12.0);
      });
   }

   protected static void tryRevenge(PiglinBruteEntity piglinBrute, LivingEntity target) {
      if (!(target instanceof AbstractPiglinEntity)) {
         PiglinBrain.tryRevenge(piglinBrute, target);
      }
   }

   protected static void setTarget(PiglinBruteEntity piglinBrute, LivingEntity target) {
      piglinBrute.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      piglinBrute.getBrain().remember(MemoryModuleType.ANGRY_AT, target.getUuid(), 600L);
   }

   protected static void playSoundRandomly(PiglinBruteEntity piglinBrute) {
      if ((double)piglinBrute.getWorld().random.nextFloat() < 0.0125) {
         playSoundIfAngry(piglinBrute);
      }

   }

   private static void playSoundIfAngry(PiglinBruteEntity piglinBrute) {
      piglinBrute.getBrain().getFirstPossibleNonCoreActivity().ifPresent((activity) -> {
         if (activity == Activity.FIGHT) {
            piglinBrute.playAngrySound();
         }

      });
   }
}
