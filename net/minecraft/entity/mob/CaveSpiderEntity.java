package net.minecraft.entity.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CaveSpiderEntity extends SpiderEntity {
   public CaveSpiderEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public static DefaultAttributeContainer.Builder createCaveSpiderAttributes() {
      return SpiderEntity.createSpiderAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 12.0);
   }

   public boolean tryAttack(Entity target) {
      if (super.tryAttack(target)) {
         if (target instanceof LivingEntity) {
            int i = 0;
            if (this.getWorld().getDifficulty() == Difficulty.NORMAL) {
               i = 7;
            } else if (this.getWorld().getDifficulty() == Difficulty.HARD) {
               i = 15;
            }

            if (i > 0) {
               ((LivingEntity)target).addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, i * 20, 0), this);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      return entityData;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.45F;
   }
}
