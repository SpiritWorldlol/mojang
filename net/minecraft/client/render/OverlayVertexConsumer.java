package net.minecraft.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public class OverlayVertexConsumer extends FixedColorVertexConsumer {
   private final VertexConsumer delegate;
   private final Matrix4f inverseTextureMatrix;
   private final Matrix3f inverseNormalMatrix;
   private final float textureScale;
   private float x;
   private float y;
   private float z;
   private int u1;
   private int v1;
   private int light;
   private float normalX;
   private float normalY;
   private float normalZ;

   public OverlayVertexConsumer(VertexConsumer delegate, Matrix4f textureMatrix, Matrix3f normalMatrix, float textureScale) {
      this.delegate = delegate;
      this.inverseTextureMatrix = (new Matrix4f(textureMatrix)).invert();
      this.inverseNormalMatrix = (new Matrix3f(normalMatrix)).invert();
      this.textureScale = textureScale;
      this.init();
   }

   private void init() {
      this.x = 0.0F;
      this.y = 0.0F;
      this.z = 0.0F;
      this.u1 = 0;
      this.v1 = 10;
      this.light = 15728880;
      this.normalX = 0.0F;
      this.normalY = 1.0F;
      this.normalZ = 0.0F;
   }

   public void next() {
      Vector3f vector3f = this.inverseNormalMatrix.transform(new Vector3f(this.normalX, this.normalY, this.normalZ));
      Direction lv = Direction.getFacing(vector3f.x(), vector3f.y(), vector3f.z());
      Vector4f vector4f = this.inverseTextureMatrix.transform(new Vector4f(this.x, this.y, this.z, 1.0F));
      vector4f.rotateY(3.1415927F);
      vector4f.rotateX(-1.5707964F);
      vector4f.rotate(lv.getRotationQuaternion());
      float f = -vector4f.x() * this.textureScale;
      float g = -vector4f.y() * this.textureScale;
      this.delegate.vertex((double)this.x, (double)this.y, (double)this.z).color(1.0F, 1.0F, 1.0F, 1.0F).texture(f, g).overlay(this.u1, this.v1).light(this.light).normal(this.normalX, this.normalY, this.normalZ).next();
      this.init();
   }

   public VertexConsumer vertex(double x, double y, double z) {
      this.x = (float)x;
      this.y = (float)y;
      this.z = (float)z;
      return this;
   }

   public VertexConsumer color(int red, int green, int blue, int alpha) {
      return this;
   }

   public VertexConsumer texture(float u, float v) {
      return this;
   }

   public VertexConsumer overlay(int u, int v) {
      this.u1 = u;
      this.v1 = v;
      return this;
   }

   public VertexConsumer light(int u, int v) {
      this.light = u | v << 16;
      return this;
   }

   public VertexConsumer normal(float x, float y, float z) {
      this.normalX = x;
      this.normalY = y;
      this.normalZ = z;
      return this;
   }
}
