package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class BubbleColumnUpParticle extends SpriteBillboardParticle {
   BubbleColumnUpParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
      super(arg, d, e, f);
      this.gravityStrength = -0.125F;
      this.velocityMultiplier = 0.85F;
      this.setBoundingBoxSpacing(0.02F, 0.02F);
      this.scale *= this.random.nextFloat() * 0.6F + 0.2F;
      this.velocityX = g * 0.20000000298023224 + (Math.random() * 2.0 - 1.0) * 0.019999999552965164;
      this.velocityY = h * 0.20000000298023224 + (Math.random() * 2.0 - 1.0) * 0.019999999552965164;
      this.velocityZ = i * 0.20000000298023224 + (Math.random() * 2.0 - 1.0) * 0.019999999552965164;
      this.maxAge = (int)(40.0 / (Math.random() * 0.8 + 0.2));
   }

   public void tick() {
      super.tick();
      if (!this.dead && !this.world.getFluidState(BlockPos.ofFloored(this.x, this.y, this.z)).isIn(FluidTags.WATER)) {
         this.markDead();
      }

   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         BubbleColumnUpParticle lv = new BubbleColumnUpParticle(arg2, d, e, f, g, h, i);
         lv.setSprite(this.spriteProvider);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((DefaultParticleType)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
