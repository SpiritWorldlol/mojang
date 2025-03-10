package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT)
public class NoRenderParticle extends Particle {
   protected NoRenderParticle(ClientWorld arg, double d, double e, double f) {
      super(arg, d, e, f);
   }

   protected NoRenderParticle(ClientWorld arg, double d, double e, double f, double g, double h, double i) {
      super(arg, d, e, f, g, h, i);
   }

   public final void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.NO_RENDER;
   }
}
