package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class AbstractDustParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   protected AbstractDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, AbstractDustParticleEffect parameters, SpriteProvider spriteProvider) {
      super(world, x, y, z, velocityX, velocityY, velocityZ);
      this.velocityMultiplier = 0.96F;
      this.field_28787 = true;
      this.spriteProvider = spriteProvider;
      this.velocityX *= 0.10000000149011612;
      this.velocityY *= 0.10000000149011612;
      this.velocityZ *= 0.10000000149011612;
      float j = this.random.nextFloat() * 0.4F + 0.6F;
      this.red = this.darken(parameters.getColor().x(), j);
      this.green = this.darken(parameters.getColor().y(), j);
      this.blue = this.darken(parameters.getColor().z(), j);
      this.scale *= 0.75F * parameters.getScale();
      int k = (int)(8.0 / (this.random.nextDouble() * 0.8 + 0.2));
      this.maxAge = (int)Math.max((float)k * parameters.getScale(), 1.0F);
      this.setSpriteForAge(spriteProvider);
   }

   protected float darken(float colorComponent, float multiplier) {
      return (this.random.nextFloat() * 0.2F + 0.8F) * colorComponent * multiplier;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      super.tick();
      this.setSpriteForAge(this.spriteProvider);
   }
}
