package net.minecraft.util.math.noise;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class SimplexNoiseSampler {
   protected static final int[][] GRADIENTS = new int[][]{{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}, {1, 1, 0}, {0, -1, 1}, {-1, 1, 0}, {0, -1, -1}};
   private static final double SQRT_3 = Math.sqrt(3.0);
   private static final double SKEW_FACTOR_2D;
   private static final double UNSKEW_FACTOR_2D;
   private final int[] permutation = new int[512];
   public final double originX;
   public final double originY;
   public final double originZ;

   public SimplexNoiseSampler(Random random) {
      this.originX = random.nextDouble() * 256.0;
      this.originY = random.nextDouble() * 256.0;
      this.originZ = random.nextDouble() * 256.0;

      int i;
      for(i = 0; i < 256; this.permutation[i] = i++) {
      }

      for(i = 0; i < 256; ++i) {
         int j = random.nextInt(256 - i);
         int k = this.permutation[i];
         this.permutation[i] = this.permutation[j + i];
         this.permutation[j + i] = k;
      }

   }

   private int map(int input) {
      return this.permutation[input & 255];
   }

   protected static double dot(int[] gradient, double x, double y, double z) {
      return (double)gradient[0] * x + (double)gradient[1] * y + (double)gradient[2] * z;
   }

   private double grad(int hash, double x, double y, double z, double distance) {
      double h = distance - x * x - y * y - z * z;
      double j;
      if (h < 0.0) {
         j = 0.0;
      } else {
         h *= h;
         j = h * h * dot(GRADIENTS[hash], x, y, z);
      }

      return j;
   }

   public double sample(double x, double y) {
      double f = (x + y) * SKEW_FACTOR_2D;
      int i = MathHelper.floor(x + f);
      int j = MathHelper.floor(y + f);
      double g = (double)(i + j) * UNSKEW_FACTOR_2D;
      double h = (double)i - g;
      double k = (double)j - g;
      double l = x - h;
      double m = y - k;
      byte n;
      byte o;
      if (l > m) {
         n = 1;
         o = 0;
      } else {
         n = 0;
         o = 1;
      }

      double p = l - (double)n + UNSKEW_FACTOR_2D;
      double q = m - (double)o + UNSKEW_FACTOR_2D;
      double r = l - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
      double s = m - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
      int t = i & 255;
      int u = j & 255;
      int v = this.map(t + this.map(u)) % 12;
      int w = this.map(t + n + this.map(u + o)) % 12;
      int x = this.map(t + 1 + this.map(u + 1)) % 12;
      double y = this.grad(v, l, m, 0.0, 0.5);
      double z = this.grad(w, p, q, 0.0, 0.5);
      double aa = this.grad(x, r, s, 0.0, 0.5);
      return 70.0 * (y + z + aa);
   }

   public double sample(double x, double y, double z) {
      double g = 0.3333333333333333;
      double h = (x + y + z) * 0.3333333333333333;
      int i = MathHelper.floor(x + h);
      int j = MathHelper.floor(y + h);
      int k = MathHelper.floor(z + h);
      double l = 0.16666666666666666;
      double m = (double)(i + j + k) * 0.16666666666666666;
      double n = (double)i - m;
      double o = (double)j - m;
      double p = (double)k - m;
      double q = x - n;
      double r = y - o;
      double s = z - p;
      byte t;
      byte u;
      byte v;
      byte w;
      byte x;
      byte y;
      if (q >= r) {
         if (r >= s) {
            t = 1;
            u = 0;
            v = 0;
            w = 1;
            x = 1;
            y = 0;
         } else if (q >= s) {
            t = 1;
            u = 0;
            v = 0;
            w = 1;
            x = 0;
            y = 1;
         } else {
            t = 0;
            u = 0;
            v = 1;
            w = 1;
            x = 0;
            y = 1;
         }
      } else if (r < s) {
         t = 0;
         u = 0;
         v = 1;
         w = 0;
         x = 1;
         y = 1;
      } else if (q < s) {
         t = 0;
         u = 1;
         v = 0;
         w = 0;
         x = 1;
         y = 1;
      } else {
         t = 0;
         u = 1;
         v = 0;
         w = 1;
         x = 1;
         y = 0;
      }

      double z = q - (double)t + 0.16666666666666666;
      double aa = r - (double)u + 0.16666666666666666;
      double ab = s - (double)v + 0.16666666666666666;
      double ac = q - (double)w + 0.3333333333333333;
      double ad = r - (double)x + 0.3333333333333333;
      double ae = s - (double)y + 0.3333333333333333;
      double af = q - 1.0 + 0.5;
      double ag = r - 1.0 + 0.5;
      double ah = s - 1.0 + 0.5;
      int ai = i & 255;
      int aj = j & 255;
      int ak = k & 255;
      int al = this.map(ai + this.map(aj + this.map(ak))) % 12;
      int am = this.map(ai + t + this.map(aj + u + this.map(ak + v))) % 12;
      int an = this.map(ai + w + this.map(aj + x + this.map(ak + y))) % 12;
      int ao = this.map(ai + 1 + this.map(aj + 1 + this.map(ak + 1))) % 12;
      double ap = this.grad(al, q, r, s, 0.6);
      double aq = this.grad(am, z, aa, ab, 0.6);
      double ar = this.grad(an, ac, ad, ae, 0.6);
      double as = this.grad(ao, af, ag, ah, 0.6);
      return 32.0 * (ap + aq + ar + as);
   }

   static {
      SKEW_FACTOR_2D = 0.5 * (SQRT_3 - 1.0);
      UNSKEW_FACTOR_2D = (3.0 - SQRT_3) / 6.0;
   }
}
