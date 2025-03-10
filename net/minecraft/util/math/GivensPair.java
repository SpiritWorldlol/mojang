package net.minecraft.util.math;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

public record GivensPair(float sinHalf, float cosHalf) {
   public GivensPair(float f, float g) {
      this.sinHalf = f;
      this.cosHalf = g;
   }

   public static GivensPair normalize(float a, float b) {
      float h = Math.invsqrt(a * a + b * b);
      return new GivensPair(h * a, h * b);
   }

   public static GivensPair fromAngle(float radians) {
      float g = Math.sin(radians / 2.0F);
      float h = Math.cosFromSin(g, radians / 2.0F);
      return new GivensPair(g, h);
   }

   public GivensPair negateSin() {
      return new GivensPair(-this.sinHalf, this.cosHalf);
   }

   public Quaternionf method_49729(Quaternionf quaternionf) {
      return quaternionf.set(this.sinHalf, 0.0F, 0.0F, this.cosHalf);
   }

   public Quaternionf method_49732(Quaternionf quaternionf) {
      return quaternionf.set(0.0F, this.sinHalf, 0.0F, this.cosHalf);
   }

   public Quaternionf method_49735(Quaternionf quaternionf) {
      return quaternionf.set(0.0F, 0.0F, this.sinHalf, this.cosHalf);
   }

   public float cosDouble() {
      return this.cosHalf * this.cosHalf - this.sinHalf * this.sinHalf;
   }

   public float sinDouble() {
      return 2.0F * this.sinHalf * this.cosHalf;
   }

   public Matrix3f method_49728(Matrix3f matrix3f) {
      matrix3f.m01 = 0.0F;
      matrix3f.m02 = 0.0F;
      matrix3f.m10 = 0.0F;
      matrix3f.m20 = 0.0F;
      float f = this.cosDouble();
      float g = this.sinDouble();
      matrix3f.m11 = f;
      matrix3f.m22 = f;
      matrix3f.m12 = g;
      matrix3f.m21 = -g;
      matrix3f.m00 = 1.0F;
      return matrix3f;
   }

   public Matrix3f method_49731(Matrix3f matrix3f) {
      matrix3f.m01 = 0.0F;
      matrix3f.m10 = 0.0F;
      matrix3f.m12 = 0.0F;
      matrix3f.m21 = 0.0F;
      float f = this.cosDouble();
      float g = this.sinDouble();
      matrix3f.m00 = f;
      matrix3f.m22 = f;
      matrix3f.m02 = -g;
      matrix3f.m20 = g;
      matrix3f.m11 = 1.0F;
      return matrix3f;
   }

   public Matrix3f method_49734(Matrix3f matrix3f) {
      matrix3f.m02 = 0.0F;
      matrix3f.m12 = 0.0F;
      matrix3f.m20 = 0.0F;
      matrix3f.m21 = 0.0F;
      float f = this.cosDouble();
      float g = this.sinDouble();
      matrix3f.m00 = f;
      matrix3f.m11 = f;
      matrix3f.m01 = g;
      matrix3f.m10 = -g;
      matrix3f.m22 = 1.0F;
      return matrix3f;
   }

   public float sinHalf() {
      return this.sinHalf;
   }

   public float cosHalf() {
      return this.cosHalf;
   }
}
