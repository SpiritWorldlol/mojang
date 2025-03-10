package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.MemoryModuleType;

public class StopPanickingTask {
   private static final int MAX_DISTANCE = 36;

   public static Task create() {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryOptional(MemoryModuleType.HURT_BY), context.queryMemoryOptional(MemoryModuleType.HURT_BY_ENTITY), context.queryMemoryOptional(MemoryModuleType.NEAREST_HOSTILE)).apply(context, (hurtBy, hurtByEntity, nearestHostile) -> {
            return (world, entity, time) -> {
               boolean bl = context.getOptionalValue(hurtBy).isPresent() || context.getOptionalValue(nearestHostile).isPresent() || context.getOptionalValue(hurtByEntity).filter((hurtByx) -> {
                  return hurtByx.squaredDistanceTo(entity) <= 36.0;
               }).isPresent();
               if (!bl) {
                  hurtBy.forget();
                  hurtByEntity.forget();
                  entity.getBrain().refreshActivities(world.getTimeOfDay(), world.getTime());
               }

               return true;
            };
         });
      });
   }
}
