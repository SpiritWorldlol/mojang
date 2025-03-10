package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldEvents;

public class BoneMealTask extends MultiTickTask {
   private static final int MAX_DURATION = 80;
   private long startTime;
   private long lastEndEntityAge;
   private int duration;
   private Optional pos = Optional.empty();

   public BoneMealTask() {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));
   }

   protected boolean shouldRun(ServerWorld arg, VillagerEntity arg2) {
      if (arg2.age % 10 == 0 && (this.lastEndEntityAge == 0L || this.lastEndEntityAge + 160L <= (long)arg2.age)) {
         if (arg2.getInventory().count(Items.BONE_MEAL) <= 0) {
            return false;
         } else {
            this.pos = this.findBoneMealPos(arg, arg2);
            return this.pos.isPresent();
         }
      } else {
         return false;
      }
   }

   protected boolean shouldKeepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      return this.duration < 80 && this.pos.isPresent();
   }

   private Optional findBoneMealPos(ServerWorld world, VillagerEntity entity) {
      BlockPos.Mutable lv = new BlockPos.Mutable();
      Optional optional = Optional.empty();
      int i = 0;

      for(int j = -1; j <= 1; ++j) {
         for(int k = -1; k <= 1; ++k) {
            for(int l = -1; l <= 1; ++l) {
               lv.set((Vec3i)entity.getBlockPos(), j, k, l);
               if (this.canBoneMeal(lv, world)) {
                  ++i;
                  if (world.random.nextInt(i) == 0) {
                     optional = Optional.of(lv.toImmutable());
                  }
               }
            }
         }
      }

      return optional;
   }

   private boolean canBoneMeal(BlockPos pos, ServerWorld world) {
      BlockState lv = world.getBlockState(pos);
      Block lv2 = lv.getBlock();
      return lv2 instanceof CropBlock && !((CropBlock)lv2).isMature(lv);
   }

   protected void run(ServerWorld arg, VillagerEntity arg2, long l) {
      this.addLookWalkTargets(arg2);
      arg2.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BONE_MEAL));
      this.startTime = l;
      this.duration = 0;
   }

   private void addLookWalkTargets(VillagerEntity villager) {
      this.pos.ifPresent((pos) -> {
         BlockPosLookTarget lv = new BlockPosLookTarget(pos);
         villager.getBrain().remember(MemoryModuleType.LOOK_TARGET, (Object)lv);
         villager.getBrain().remember(MemoryModuleType.WALK_TARGET, (Object)(new WalkTarget(lv, 0.5F, 1)));
      });
   }

   protected void finishRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      arg2.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      this.lastEndEntityAge = (long)arg2.age;
   }

   protected void keepRunning(ServerWorld arg, VillagerEntity arg2, long l) {
      BlockPos lv = (BlockPos)this.pos.get();
      if (l >= this.startTime && lv.isWithinDistance(arg2.getPos(), 1.0)) {
         ItemStack lv2 = ItemStack.EMPTY;
         SimpleInventory lv3 = arg2.getInventory();
         int i = lv3.size();

         for(int j = 0; j < i; ++j) {
            ItemStack lv4 = lv3.getStack(j);
            if (lv4.isOf(Items.BONE_MEAL)) {
               lv2 = lv4;
               break;
            }
         }

         if (!lv2.isEmpty() && BoneMealItem.useOnFertilizable(lv2, arg, lv)) {
            arg.syncWorldEvent(WorldEvents.BONE_MEAL_USED, lv, 0);
            this.pos = this.findBoneMealPos(arg, arg2);
            this.addLookWalkTargets(arg2);
            this.startTime = l + 40L;
         }

         ++this.duration;
      }
   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (VillagerEntity)entity, time);
   }

   // $FF: synthetic method
   protected void keepRunning(ServerWorld world, LivingEntity entity, long time) {
      this.keepRunning(world, (VillagerEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (VillagerEntity)entity, time);
   }
}
