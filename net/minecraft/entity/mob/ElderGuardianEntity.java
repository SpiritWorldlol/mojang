package net.minecraft.entity.mob;

import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class ElderGuardianEntity extends GuardianEntity {
   public static final float SCALE;
   private static final int field_38119 = 1200;
   private static final int AFFECTED_PLAYER_RANGE = 50;
   private static final int MINING_FATIGUE_DURATION = 6000;
   private static final int MINING_FATIGUE_AMPLIFIER = 2;
   private static final int field_38118 = 1200;

   public ElderGuardianEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.setPersistent();
      if (this.wanderGoal != null) {
         this.wanderGoal.setChance(400);
      }

   }

   public static DefaultAttributeContainer.Builder createElderGuardianAttributes() {
      return GuardianEntity.createGuardianAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30000001192092896).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0);
   }

   public int getWarmupTime() {
      return 60;
   }

   protected SoundEvent getAmbientSound() {
      return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_ELDER_GUARDIAN_AMBIENT : SoundEvents.ENTITY_ELDER_GUARDIAN_AMBIENT_LAND;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_ELDER_GUARDIAN_HURT : SoundEvents.ENTITY_ELDER_GUARDIAN_HURT_LAND;
   }

   protected SoundEvent getDeathSound() {
      return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_ELDER_GUARDIAN_DEATH : SoundEvents.ENTITY_ELDER_GUARDIAN_DEATH_LAND;
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.ENTITY_ELDER_GUARDIAN_FLOP;
   }

   protected void mobTick() {
      super.mobTick();
      if ((this.age + this.getId()) % 1200 == 0) {
         StatusEffectInstance lv = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 6000, 2);
         List list = StatusEffectUtil.addEffectToPlayersWithinDistance((ServerWorld)this.getWorld(), this, this.getPos(), 50.0, lv, 1200);
         list.forEach((arg) -> {
            arg.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.ELDER_GUARDIAN_EFFECT, this.isSilent() ? GameStateChangeS2CPacket.field_33328 : 1.0F));
         });
      }

      if (!this.hasPositionTarget()) {
         this.setPositionTarget(this.getBlockPos(), 16);
      }

   }

   static {
      SCALE = EntityType.ELDER_GUARDIAN.getWidth() / EntityType.GUARDIAN.getWidth();
   }
}
