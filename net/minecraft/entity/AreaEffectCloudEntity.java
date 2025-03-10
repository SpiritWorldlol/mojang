package net.minecraft.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.command.argument.ParticleEffectArgumentType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AreaEffectCloudEntity extends Entity implements Ownable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_29972 = 5;
   private static final TrackedData RADIUS;
   private static final TrackedData COLOR;
   private static final TrackedData WAITING;
   private static final TrackedData PARTICLE_ID;
   private static final float MAX_RADIUS = 32.0F;
   private static final float field_40730 = 0.5F;
   private static final float field_40731 = 3.0F;
   public static final float field_40732 = 6.0F;
   public static final float field_40733 = 0.5F;
   private Potion potion;
   private final List effects;
   private final Map affectedEntities;
   private int duration;
   private int waitTime;
   private int reapplicationDelay;
   private boolean customColor;
   private int durationOnUse;
   private float radiusOnUse;
   private float radiusGrowth;
   @Nullable
   private LivingEntity owner;
   @Nullable
   private UUID ownerUuid;

   public AreaEffectCloudEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.potion = Potions.EMPTY;
      this.effects = Lists.newArrayList();
      this.affectedEntities = Maps.newHashMap();
      this.duration = 600;
      this.waitTime = 20;
      this.reapplicationDelay = 20;
      this.noClip = true;
   }

   public AreaEffectCloudEntity(World world, double x, double y, double z) {
      this(EntityType.AREA_EFFECT_CLOUD, world);
      this.setPosition(x, y, z);
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(COLOR, 0);
      this.getDataTracker().startTracking(RADIUS, 3.0F);
      this.getDataTracker().startTracking(WAITING, false);
      this.getDataTracker().startTracking(PARTICLE_ID, ParticleTypes.ENTITY_EFFECT);
   }

   public void setRadius(float radius) {
      if (!this.getWorld().isClient) {
         this.getDataTracker().set(RADIUS, MathHelper.clamp(radius, 0.0F, 32.0F));
      }

   }

   public void calculateDimensions() {
      double d = this.getX();
      double e = this.getY();
      double f = this.getZ();
      super.calculateDimensions();
      this.setPosition(d, e, f);
   }

   public float getRadius() {
      return (Float)this.getDataTracker().get(RADIUS);
   }

   public void setPotion(Potion potion) {
      this.potion = potion;
      if (!this.customColor) {
         this.updateColor();
      }

   }

   private void updateColor() {
      if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
         this.getDataTracker().set(COLOR, 0);
      } else {
         this.getDataTracker().set(COLOR, PotionUtil.getColor((Collection)PotionUtil.getPotionEffects(this.potion, this.effects)));
      }

   }

   public void addEffect(StatusEffectInstance effect) {
      this.effects.add(effect);
      if (!this.customColor) {
         this.updateColor();
      }

   }

   public int getColor() {
      return (Integer)this.getDataTracker().get(COLOR);
   }

   public void setColor(int rgb) {
      this.customColor = true;
      this.getDataTracker().set(COLOR, rgb);
   }

   public ParticleEffect getParticleType() {
      return (ParticleEffect)this.getDataTracker().get(PARTICLE_ID);
   }

   public void setParticleType(ParticleEffect particle) {
      this.getDataTracker().set(PARTICLE_ID, particle);
   }

   protected void setWaiting(boolean waiting) {
      this.getDataTracker().set(WAITING, waiting);
   }

   public boolean isWaiting() {
      return (Boolean)this.getDataTracker().get(WAITING);
   }

   public int getDuration() {
      return this.duration;
   }

   public void setDuration(int duration) {
      this.duration = duration;
   }

   public void tick() {
      super.tick();
      boolean bl = this.isWaiting();
      float f = this.getRadius();
      if (this.getWorld().isClient) {
         if (bl && this.random.nextBoolean()) {
            return;
         }

         ParticleEffect lv = this.getParticleType();
         int i;
         float g;
         if (bl) {
            i = 2;
            g = 0.2F;
         } else {
            i = MathHelper.ceil(3.1415927F * f * f);
            g = f;
         }

         for(int j = 0; j < i; ++j) {
            float h = this.random.nextFloat() * 6.2831855F;
            float k = MathHelper.sqrt(this.random.nextFloat()) * g;
            double d = this.getX() + (double)(MathHelper.cos(h) * k);
            double e = this.getY();
            double l = this.getZ() + (double)(MathHelper.sin(h) * k);
            double n;
            double o;
            double p;
            if (lv.getType() != ParticleTypes.ENTITY_EFFECT) {
               if (bl) {
                  n = 0.0;
                  o = 0.0;
                  p = 0.0;
               } else {
                  n = (0.5 - this.random.nextDouble()) * 0.15;
                  o = 0.009999999776482582;
                  p = (0.5 - this.random.nextDouble()) * 0.15;
               }
            } else {
               int m = bl && this.random.nextBoolean() ? 16777215 : this.getColor();
               n = (double)((float)(m >> 16 & 255) / 255.0F);
               o = (double)((float)(m >> 8 & 255) / 255.0F);
               p = (double)((float)(m & 255) / 255.0F);
            }

            this.getWorld().addImportantParticle(lv, d, e, l, n, o, p);
         }
      } else {
         if (this.age >= this.waitTime + this.duration) {
            this.discard();
            return;
         }

         boolean bl2 = this.age < this.waitTime;
         if (bl != bl2) {
            this.setWaiting(bl2);
         }

         if (bl2) {
            return;
         }

         if (this.radiusGrowth != 0.0F) {
            f += this.radiusGrowth;
            if (f < 0.5F) {
               this.discard();
               return;
            }

            this.setRadius(f);
         }

         if (this.age % 5 == 0) {
            this.affectedEntities.entrySet().removeIf((entry) -> {
               return this.age >= (Integer)entry.getValue();
            });
            List list = Lists.newArrayList();
            Iterator var24 = this.potion.getEffects().iterator();

            while(var24.hasNext()) {
               StatusEffectInstance lv2 = (StatusEffectInstance)var24.next();
               list.add(new StatusEffectInstance(lv2.getEffectType(), lv2.mapDuration((ix) -> {
                  return ix / 4;
               }), lv2.getAmplifier(), lv2.isAmbient(), lv2.shouldShowParticles()));
            }

            list.addAll(this.effects);
            if (list.isEmpty()) {
               this.affectedEntities.clear();
            } else {
               List list2 = this.getWorld().getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox());
               if (!list2.isEmpty()) {
                  Iterator var27 = list2.iterator();

                  while(true) {
                     double s;
                     LivingEntity lv3;
                     do {
                        do {
                           do {
                              if (!var27.hasNext()) {
                                 return;
                              }

                              lv3 = (LivingEntity)var27.next();
                           } while(this.affectedEntities.containsKey(lv3));
                        } while(!lv3.isAffectedBySplashPotions());

                        double q = lv3.getX() - this.getX();
                        double r = lv3.getZ() - this.getZ();
                        s = q * q + r * r;
                     } while(!(s <= (double)(f * f)));

                     this.affectedEntities.put(lv3, this.age + this.reapplicationDelay);
                     Iterator var14 = list.iterator();

                     while(var14.hasNext()) {
                        StatusEffectInstance lv4 = (StatusEffectInstance)var14.next();
                        if (lv4.getEffectType().isInstant()) {
                           lv4.getEffectType().applyInstantEffect(this, this.getOwner(), lv3, lv4.getAmplifier(), 0.5);
                        } else {
                           lv3.addStatusEffect(new StatusEffectInstance(lv4), this);
                        }
                     }

                     if (this.radiusOnUse != 0.0F) {
                        f += this.radiusOnUse;
                        if (f < 0.5F) {
                           this.discard();
                           return;
                        }

                        this.setRadius(f);
                     }

                     if (this.durationOnUse != 0) {
                        this.duration += this.durationOnUse;
                        if (this.duration <= 0) {
                           this.discard();
                           return;
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public float getRadiusOnUse() {
      return this.radiusOnUse;
   }

   public void setRadiusOnUse(float radiusOnUse) {
      this.radiusOnUse = radiusOnUse;
   }

   public float getRadiusGrowth() {
      return this.radiusGrowth;
   }

   public void setRadiusGrowth(float radiusGrowth) {
      this.radiusGrowth = radiusGrowth;
   }

   public int getDurationOnUse() {
      return this.durationOnUse;
   }

   public void setDurationOnUse(int durationOnUse) {
      this.durationOnUse = durationOnUse;
   }

   public int getWaitTime() {
      return this.waitTime;
   }

   public void setWaitTime(int waitTime) {
      this.waitTime = waitTime;
   }

   public void setOwner(@Nullable LivingEntity owner) {
      this.owner = owner;
      this.ownerUuid = owner == null ? null : owner.getUuid();
   }

   @Nullable
   public LivingEntity getOwner() {
      if (this.owner == null && this.ownerUuid != null && this.getWorld() instanceof ServerWorld) {
         Entity lv = ((ServerWorld)this.getWorld()).getEntity(this.ownerUuid);
         if (lv instanceof LivingEntity) {
            this.owner = (LivingEntity)lv;
         }
      }

      return this.owner;
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      this.age = nbt.getInt("Age");
      this.duration = nbt.getInt("Duration");
      this.waitTime = nbt.getInt("WaitTime");
      this.reapplicationDelay = nbt.getInt("ReapplicationDelay");
      this.durationOnUse = nbt.getInt("DurationOnUse");
      this.radiusOnUse = nbt.getFloat("RadiusOnUse");
      this.radiusGrowth = nbt.getFloat("RadiusPerTick");
      this.setRadius(nbt.getFloat("Radius"));
      if (nbt.containsUuid("Owner")) {
         this.ownerUuid = nbt.getUuid("Owner");
      }

      if (nbt.contains("Particle", NbtElement.STRING_TYPE)) {
         try {
            this.setParticleType(ParticleEffectArgumentType.readParameters(new StringReader(nbt.getString("Particle")), (RegistryWrapper)Registries.PARTICLE_TYPE.getReadOnlyWrapper()));
         } catch (CommandSyntaxException var5) {
            LOGGER.warn("Couldn't load custom particle {}", nbt.getString("Particle"), var5);
         }
      }

      if (nbt.contains("Color", NbtElement.NUMBER_TYPE)) {
         this.setColor(nbt.getInt("Color"));
      }

      if (nbt.contains("Potion", NbtElement.STRING_TYPE)) {
         this.setPotion(PotionUtil.getPotion(nbt));
      }

      if (nbt.contains("Effects", NbtElement.LIST_TYPE)) {
         NbtList lv = nbt.getList("Effects", NbtElement.COMPOUND_TYPE);
         this.effects.clear();

         for(int i = 0; i < lv.size(); ++i) {
            StatusEffectInstance lv2 = StatusEffectInstance.fromNbt(lv.getCompound(i));
            if (lv2 != null) {
               this.addEffect(lv2);
            }
         }
      }

   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.putInt("Age", this.age);
      nbt.putInt("Duration", this.duration);
      nbt.putInt("WaitTime", this.waitTime);
      nbt.putInt("ReapplicationDelay", this.reapplicationDelay);
      nbt.putInt("DurationOnUse", this.durationOnUse);
      nbt.putFloat("RadiusOnUse", this.radiusOnUse);
      nbt.putFloat("RadiusPerTick", this.radiusGrowth);
      nbt.putFloat("Radius", this.getRadius());
      nbt.putString("Particle", this.getParticleType().asString());
      if (this.ownerUuid != null) {
         nbt.putUuid("Owner", this.ownerUuid);
      }

      if (this.customColor) {
         nbt.putInt("Color", this.getColor());
      }

      if (this.potion != Potions.EMPTY) {
         nbt.putString("Potion", Registries.POTION.getId(this.potion).toString());
      }

      if (!this.effects.isEmpty()) {
         NbtList lv = new NbtList();
         Iterator var3 = this.effects.iterator();

         while(var3.hasNext()) {
            StatusEffectInstance lv2 = (StatusEffectInstance)var3.next();
            lv.add(lv2.writeNbt(new NbtCompound()));
         }

         nbt.put("Effects", lv);
      }

   }

   public void onTrackedDataSet(TrackedData data) {
      if (RADIUS.equals(data)) {
         this.calculateDimensions();
      }

      super.onTrackedDataSet(data);
   }

   public Potion getPotion() {
      return this.potion;
   }

   public PistonBehavior getPistonBehavior() {
      return PistonBehavior.IGNORE;
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return EntityDimensions.changing(this.getRadius() * 2.0F, 0.5F);
   }

   // $FF: synthetic method
   @Nullable
   public Entity getOwner() {
      return this.getOwner();
   }

   static {
      RADIUS = DataTracker.registerData(AreaEffectCloudEntity.class, TrackedDataHandlerRegistry.FLOAT);
      COLOR = DataTracker.registerData(AreaEffectCloudEntity.class, TrackedDataHandlerRegistry.INTEGER);
      WAITING = DataTracker.registerData(AreaEffectCloudEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      PARTICLE_ID = DataTracker.registerData(AreaEffectCloudEntity.class, TrackedDataHandlerRegistry.PARTICLE);
   }
}
