package net.minecraft.entity.ai.pathing;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class PathNode {
   public final int x;
   public final int y;
   public final int z;
   private final int hashCode;
   public int heapIndex = -1;
   public float penalizedPathLength;
   public float distanceToNearestTarget;
   public float heapWeight;
   @Nullable
   public PathNode previous;
   public boolean visited;
   public float pathLength;
   public float penalty;
   public PathNodeType type;

   public PathNode(int x, int y, int z) {
      this.type = PathNodeType.BLOCKED;
      this.x = x;
      this.y = y;
      this.z = z;
      this.hashCode = hash(x, y, z);
   }

   public PathNode copyWithNewPosition(int x, int y, int z) {
      PathNode lv = new PathNode(x, y, z);
      lv.heapIndex = this.heapIndex;
      lv.penalizedPathLength = this.penalizedPathLength;
      lv.distanceToNearestTarget = this.distanceToNearestTarget;
      lv.heapWeight = this.heapWeight;
      lv.previous = this.previous;
      lv.visited = this.visited;
      lv.pathLength = this.pathLength;
      lv.penalty = this.penalty;
      lv.type = this.type;
      return lv;
   }

   public static int hash(int x, int y, int z) {
      return y & 255 | (x & 32767) << 8 | (z & 32767) << 24 | (x < 0 ? Integer.MIN_VALUE : 0) | (z < 0 ? '耀' : 0);
   }

   public float getDistance(PathNode node) {
      float f = (float)(node.x - this.x);
      float g = (float)(node.y - this.y);
      float h = (float)(node.z - this.z);
      return MathHelper.sqrt(f * f + g * g + h * h);
   }

   public float getHorizontalDistance(PathNode node) {
      float f = (float)(node.x - this.x);
      float g = (float)(node.z - this.z);
      return MathHelper.sqrt(f * f + g * g);
   }

   public float getDistance(BlockPos pos) {
      float f = (float)(pos.getX() - this.x);
      float g = (float)(pos.getY() - this.y);
      float h = (float)(pos.getZ() - this.z);
      return MathHelper.sqrt(f * f + g * g + h * h);
   }

   public float getSquaredDistance(PathNode node) {
      float f = (float)(node.x - this.x);
      float g = (float)(node.y - this.y);
      float h = (float)(node.z - this.z);
      return f * f + g * g + h * h;
   }

   public float getSquaredDistance(BlockPos pos) {
      float f = (float)(pos.getX() - this.x);
      float g = (float)(pos.getY() - this.y);
      float h = (float)(pos.getZ() - this.z);
      return f * f + g * g + h * h;
   }

   public float getManhattanDistance(PathNode node) {
      float f = (float)Math.abs(node.x - this.x);
      float g = (float)Math.abs(node.y - this.y);
      float h = (float)Math.abs(node.z - this.z);
      return f + g + h;
   }

   public float getManhattanDistance(BlockPos pos) {
      float f = (float)Math.abs(pos.getX() - this.x);
      float g = (float)Math.abs(pos.getY() - this.y);
      float h = (float)Math.abs(pos.getZ() - this.z);
      return f + g + h;
   }

   public BlockPos getBlockPos() {
      return new BlockPos(this.x, this.y, this.z);
   }

   public Vec3d getPos() {
      return new Vec3d((double)this.x, (double)this.y, (double)this.z);
   }

   public boolean equals(Object o) {
      if (!(o instanceof PathNode lv)) {
         return false;
      } else {
         return this.hashCode == lv.hashCode && this.x == lv.x && this.y == lv.y && this.z == lv.z;
      }
   }

   public int hashCode() {
      return this.hashCode;
   }

   public boolean isInHeap() {
      return this.heapIndex >= 0;
   }

   public String toString() {
      return "Node{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
   }

   public void write(PacketByteBuf buf) {
      buf.writeInt(this.x);
      buf.writeInt(this.y);
      buf.writeInt(this.z);
      buf.writeFloat(this.pathLength);
      buf.writeFloat(this.penalty);
      buf.writeBoolean(this.visited);
      buf.writeEnumConstant(this.type);
      buf.writeFloat(this.heapWeight);
   }

   public static PathNode fromBuf(PacketByteBuf buf) {
      PathNode lv = new PathNode(buf.readInt(), buf.readInt(), buf.readInt());
      readFromBuf(buf, lv);
      return lv;
   }

   protected static void readFromBuf(PacketByteBuf buf, PathNode target) {
      target.pathLength = buf.readFloat();
      target.penalty = buf.readFloat();
      target.visited = buf.readBoolean();
      target.type = (PathNodeType)buf.readEnumConstant(PathNodeType.class);
      target.heapWeight = buf.readFloat();
   }
}
