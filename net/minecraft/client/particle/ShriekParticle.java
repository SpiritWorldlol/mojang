package net.minecraft.client.particle;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ShriekParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class ShriekParticle extends SpriteBillboardParticle {
   private static final Vector3f field_38334 = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
   private static final Vector3f field_38335 = new Vector3f(-1.0F, -1.0F, 0.0F);
   private static final float X_ROTATION = 1.0472F;
   private int delay;

   ShriekParticle(ClientWorld world, double x, double y, double z, int delay) {
      super(world, x, y, z, 0.0, 0.0, 0.0);
      this.scale = 0.85F;
      this.delay = delay;
      this.maxAge = 30;
      this.gravityStrength = 0.0F;
      this.velocityX = 0.0;
      this.velocityY = 0.1;
      this.velocityZ = 0.0;
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 0.75F, 0.0F, 1.0F);
   }

   public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
      if (this.delay <= 0) {
         this.alpha = 1.0F - MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge, 0.0F, 1.0F);
         this.buildGeometry(vertexConsumer, camera, tickDelta, (quaternion) -> {
            quaternion.mul((new Quaternionf()).rotationX(-1.0472F));
         });
         this.buildGeometry(vertexConsumer, camera, tickDelta, (quaternion) -> {
            quaternion.mul((new Quaternionf()).rotationYXZ(-3.1415927F, 1.0472F, 0.0F));
         });
      }
   }

   private void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta, Consumer rotator) {
      Vec3d lv = camera.getPos();
      float g = (float)(MathHelper.lerp((double)tickDelta, this.prevPosX, this.x) - lv.getX());
      float h = (float)(MathHelper.lerp((double)tickDelta, this.prevPosY, this.y) - lv.getY());
      float i = (float)(MathHelper.lerp((double)tickDelta, this.prevPosZ, this.z) - lv.getZ());
      Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(0.0F, field_38334.x(), field_38334.y(), field_38334.z());
      rotator.accept(quaternionf);
      quaternionf.transform(field_38335);
      Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
      float j = this.getSize(tickDelta);

      int k;
      for(k = 0; k < 4; ++k) {
         Vector3f vector3f = vector3fs[k];
         vector3f.rotate(quaternionf);
         vector3f.mul(j);
         vector3f.add(g, h, i);
      }

      k = this.getBrightness(tickDelta);
      this.vertex(vertexConsumer, vector3fs[0], this.getMaxU(), this.getMaxV(), k);
      this.vertex(vertexConsumer, vector3fs[1], this.getMaxU(), this.getMinV(), k);
      this.vertex(vertexConsumer, vector3fs[2], this.getMinU(), this.getMinV(), k);
      this.vertex(vertexConsumer, vector3fs[3], this.getMinU(), this.getMaxV(), k);
   }

   private void vertex(VertexConsumer vertexConsumer, Vector3f pos, float u, float v, int light) {
      vertexConsumer.vertex((double)pos.x(), (double)pos.y(), (double)pos.z()).texture(u, v).color(this.red, this.green, this.blue, this.alpha).light(light).next();
   }

   public int getBrightness(float tint) {
      return 240;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      if (this.delay > 0) {
         --this.delay;
      } else {
         super.tick();
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(ShriekParticleEffect arg, ClientWorld arg2, double d, double e, double f, double g, double h, double i) {
         ShriekParticle lv = new ShriekParticle(arg2, d, e, f, arg.getDelay());
         lv.setSprite(this.spriteProvider);
         lv.setAlpha(1.0F);
         return lv;
      }

      // $FF: synthetic method
      public Particle createParticle(ParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
         return this.createParticle((ShriekParticleEffect)parameters, world, x, y, z, velocityX, velocityY, velocityZ);
      }
   }
}
