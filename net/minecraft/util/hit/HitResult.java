package net.minecraft.util.hit;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public abstract class HitResult {
   protected final Vec3d pos;

   protected HitResult(Vec3d pos) {
      this.pos = pos;
   }

   public double squaredDistanceTo(Entity entity) {
      double d = this.pos.x - entity.getX();
      double e = this.pos.y - entity.getY();
      double f = this.pos.z - entity.getZ();
      return d * d + e * e + f * f;
   }

   public abstract Type getType();

   public Vec3d getPos() {
      return this.pos;
   }

   public static enum Type {
      MISS,
      BLOCK,
      ENTITY;

      // $FF: synthetic method
      private static Type[] method_36796() {
         return new Type[]{MISS, BLOCK, ENTITY};
      }
   }
}
