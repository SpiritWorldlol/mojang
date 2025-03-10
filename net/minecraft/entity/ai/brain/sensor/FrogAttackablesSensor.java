package net.minecraft.entity.ai.brain.sensor;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.FrogEntity;

public class FrogAttackablesSensor extends NearestVisibleLivingEntitySensor {
   public static final float RANGE = 10.0F;

   protected boolean matches(LivingEntity entity, LivingEntity target) {
      return !entity.getBrain().hasMemoryModule(MemoryModuleType.HAS_HUNTING_COOLDOWN) && Sensor.testAttackableTargetPredicate(entity, target) && FrogEntity.isValidFrogFood(target) && !this.isTargetUnreachable(entity, target) ? target.isInRange(entity, 10.0) : false;
   }

   private boolean isTargetUnreachable(LivingEntity entity, LivingEntity target) {
      List list = (List)entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
      return list.contains(target.getUuid());
   }

   protected MemoryModuleType getOutputMemoryModule() {
      return MemoryModuleType.NEAREST_ATTACKABLE;
   }
}
