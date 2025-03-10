package net.minecraft.client.render;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class OutlineVertexConsumerProvider implements VertexConsumerProvider {
   private final VertexConsumerProvider.Immediate parent;
   private final VertexConsumerProvider.Immediate plainDrawer = VertexConsumerProvider.immediate(new BufferBuilder(256));
   private int red = 255;
   private int green = 255;
   private int blue = 255;
   private int alpha = 255;

   public OutlineVertexConsumerProvider(VertexConsumerProvider.Immediate parent) {
      this.parent = parent;
   }

   public VertexConsumer getBuffer(RenderLayer arg) {
      VertexConsumer lv;
      if (arg.isOutline()) {
         lv = this.plainDrawer.getBuffer(arg);
         return new OutlineVertexConsumer(lv, this.red, this.green, this.blue, this.alpha);
      } else {
         lv = this.parent.getBuffer(arg);
         Optional optional = arg.getAffectedOutline();
         if (optional.isPresent()) {
            VertexConsumer lv2 = this.plainDrawer.getBuffer((RenderLayer)optional.get());
            OutlineVertexConsumer lv3 = new OutlineVertexConsumer(lv2, this.red, this.green, this.blue, this.alpha);
            return VertexConsumers.union(lv3, lv);
         } else {
            return lv;
         }
      }
   }

   public void setColor(int red, int green, int blue, int alpha) {
      this.red = red;
      this.green = green;
      this.blue = blue;
      this.alpha = alpha;
   }

   public void draw() {
      this.plainDrawer.draw();
   }

   @Environment(EnvType.CLIENT)
   static class OutlineVertexConsumer extends FixedColorVertexConsumer {
      private final VertexConsumer delegate;
      private double x;
      private double y;
      private double z;
      private float textureU;
      private float textureV;

      OutlineVertexConsumer(VertexConsumer delegate, int red, int green, int blue, int alpha) {
         this.delegate = delegate;
         super.fixedColor(red, green, blue, alpha);
      }

      public void fixedColor(int red, int green, int blue, int alpha) {
      }

      public void unfixColor() {
      }

      public VertexConsumer vertex(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
         return this;
      }

      public VertexConsumer color(int red, int green, int blue, int alpha) {
         return this;
      }

      public VertexConsumer texture(float u, float v) {
         this.textureU = u;
         this.textureV = v;
         return this;
      }

      public VertexConsumer overlay(int u, int v) {
         return this;
      }

      public VertexConsumer light(int u, int v) {
         return this;
      }

      public VertexConsumer normal(float x, float y, float z) {
         return this;
      }

      public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
         this.delegate.vertex((double)x, (double)y, (double)z).color(this.fixedRed, this.fixedGreen, this.fixedBlue, this.fixedAlpha).texture(u, v).next();
      }

      public void next() {
         this.delegate.vertex(this.x, this.y, this.z).color(this.fixedRed, this.fixedGreen, this.fixedBlue, this.fixedAlpha).texture(this.textureU, this.textureV).next();
      }
   }
}
