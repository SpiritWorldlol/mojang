package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;

@Environment(EnvType.CLIENT)
public class CrackParticle extends SpriteBillboardParticle {
   private final float sampleU;
   private final float sampleV;

   CrackParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, ItemStack stack) {
      this(world, x, y, z, stack);
      this.velocityX *= 0.10000000149011612;
      this.velocityY *= 0.10000000149011612;
      this.velocityZ *= 0.10000000149011612;
      this.velocityX += velocityX;
      this.velocityY += velocityY;
      this.velocityZ += velocityZ;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.TERRAIN_SHEET;
   }

   protected CrackParticle(ClientWorld world, double x, double y, double z, ItemStack stack) {
      super(world, x, y, z, 0.0, 0.0, 0.0);
      this.setSprite(MinecraftClient.getInstance().getItemRenderer().getModel(stack, world, (LivingEntity)null, 0).getParticleSprite());
      this.gravityStrength = 1.0F;
      this.scale /= 2.0F;
      this.sampleU = this.random.nextFloat() * 3.0F;
      this.sampleV = this.random.nextFloat() * 3.0F;
   }

   protected float getMinU() {
      return this.sprite.getFrameU((double)((this.sampleU + 1.0F) / 4.0F * 16.0F));
   }

   protected float getMaxU() {
      return this.sprite.getFrameU((double)(this.sampleU / 4.0F * 16.0F));
   }

   protected float getMinV() {
      return this.sprite.getFrameV((double)(this.sampleV / 4.0F * 16.0F));
   }

   protected float getMaxV() {
      return this.sprite.getFrameV((double)((this.sampleV + 1.0F) / 4.0F * 16.0F));
   }

   @Environment(EnvType.CLIENT)
   public static class SnowballFactory implements ParticleFactory {
      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new CrackParticle(arg2, d, e, f, new ItemStack(Items.SNOWBALL));
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class SlimeballFactory implements ParticleFactory {
      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new CrackParticle(arg2, d, e, f, new ItemStack(Items.SLIME_BALL));
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class ItemFactory implements ParticleFactory {
      public Particle createParticle(ItemStackParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         return new CrackParticle(arg2, d, e, f, g, h, i, arg.getItemStack());
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((ItemStackParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
