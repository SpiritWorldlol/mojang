package net.minecraft.world.border;

import com.google.common.collect.Lists;
import com.mojang.serialization.DynamicLike;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class WorldBorder {
   public static final double STATIC_AREA_SIZE = 5.9999968E7;
   public static final double MAX_CENTER_COORDINATES = 2.9999984E7;
   private final List listeners = Lists.newArrayList();
   private double damagePerBlock = 0.2;
   private double safeZone = 5.0;
   private int warningTime = 15;
   private int warningBlocks = 5;
   private double centerX;
   private double centerZ;
   int maxRadius = 29999984;
   private Area area = new StaticArea(5.9999968E7);
   public static final Properties DEFAULT_BORDER = new Properties(0.0, 0.0, 0.2, 5.0, 5, 15, 5.9999968E7, 0L, 0.0);

   public boolean contains(BlockPos pos) {
      return (double)(pos.getX() + 1) > this.getBoundWest() && (double)pos.getX() < this.getBoundEast() && (double)(pos.getZ() + 1) > this.getBoundNorth() && (double)pos.getZ() < this.getBoundSouth();
   }

   public boolean contains(ChunkPos pos) {
      return (double)pos.getEndX() > this.getBoundWest() && (double)pos.getStartX() < this.getBoundEast() && (double)pos.getEndZ() > this.getBoundNorth() && (double)pos.getStartZ() < this.getBoundSouth();
   }

   public boolean contains(double x, double z) {
      return x > this.getBoundWest() && x < this.getBoundEast() && z > this.getBoundNorth() && z < this.getBoundSouth();
   }

   public boolean contains(double x, double z, double margin) {
      return x > this.getBoundWest() - margin && x < this.getBoundEast() + margin && z > this.getBoundNorth() - margin && z < this.getBoundSouth() + margin;
   }

   public boolean contains(Box box) {
      return box.maxX > this.getBoundWest() && box.minX < this.getBoundEast() && box.maxZ > this.getBoundNorth() && box.minZ < this.getBoundSouth();
   }

   public BlockPos clamp(double x, double y, double z) {
      return BlockPos.ofFloored(MathHelper.clamp(x, this.getBoundWest(), this.getBoundEast()), y, MathHelper.clamp(z, this.getBoundNorth(), this.getBoundSouth()));
   }

   public double getDistanceInsideBorder(Entity entity) {
      return this.getDistanceInsideBorder(entity.getX(), entity.getZ());
   }

   public VoxelShape asVoxelShape() {
      return this.area.asVoxelShape();
   }

   public double getDistanceInsideBorder(double x, double z) {
      double f = z - this.getBoundNorth();
      double g = this.getBoundSouth() - z;
      double h = x - this.getBoundWest();
      double i = this.getBoundEast() - x;
      double j = Math.min(h, i);
      j = Math.min(j, f);
      return Math.min(j, g);
   }

   public boolean canCollide(Entity entity, Box box) {
      double d = Math.max(MathHelper.absMax(box.getXLength(), box.getZLength()), 1.0);
      return this.getDistanceInsideBorder(entity) < d * 2.0 && this.contains(entity.getX(), entity.getZ(), d);
   }

   public WorldBorderStage getStage() {
      return this.area.getStage();
   }

   public double getBoundWest() {
      return this.area.getBoundWest();
   }

   public double getBoundNorth() {
      return this.area.getBoundNorth();
   }

   public double getBoundEast() {
      return this.area.getBoundEast();
   }

   public double getBoundSouth() {
      return this.area.getBoundSouth();
   }

   public double getCenterX() {
      return this.centerX;
   }

   public double getCenterZ() {
      return this.centerZ;
   }

   public void setCenter(double x, double z) {
      this.centerX = x;
      this.centerZ = z;
      this.area.onCenterChanged();
      Iterator var5 = this.getListeners().iterator();

      while(var5.hasNext()) {
         WorldBorderListener lv = (WorldBorderListener)var5.next();
         lv.onCenterChanged(this, x, z);
      }

   }

   public double getSize() {
      return this.area.getSize();
   }

   public long getSizeLerpTime() {
      return this.area.getSizeLerpTime();
   }

   public double getSizeLerpTarget() {
      return this.area.getSizeLerpTarget();
   }

   public void setSize(double size) {
      this.area = new StaticArea(size);
      Iterator var3 = this.getListeners().iterator();

      while(var3.hasNext()) {
         WorldBorderListener lv = (WorldBorderListener)var3.next();
         lv.onSizeChange(this, size);
      }

   }

   public void interpolateSize(double fromSize, double toSize, long time) {
      this.area = (Area)(fromSize == toSize ? new StaticArea(toSize) : new MovingArea(fromSize, toSize, time));
      Iterator var7 = this.getListeners().iterator();

      while(var7.hasNext()) {
         WorldBorderListener lv = (WorldBorderListener)var7.next();
         lv.onInterpolateSize(this, fromSize, toSize, time);
      }

   }

   protected List getListeners() {
      return Lists.newArrayList(this.listeners);
   }

   public void addListener(WorldBorderListener listener) {
      this.listeners.add(listener);
   }

   public void removeListener(WorldBorderListener listener) {
      this.listeners.remove(listener);
   }

   public void setMaxRadius(int maxRadius) {
      this.maxRadius = maxRadius;
      this.area.onMaxRadiusChanged();
   }

   public int getMaxRadius() {
      return this.maxRadius;
   }

   public double getSafeZone() {
      return this.safeZone;
   }

   public void setSafeZone(double safeZone) {
      this.safeZone = safeZone;
      Iterator var3 = this.getListeners().iterator();

      while(var3.hasNext()) {
         WorldBorderListener lv = (WorldBorderListener)var3.next();
         lv.onSafeZoneChanged(this, safeZone);
      }

   }

   public double getDamagePerBlock() {
      return this.damagePerBlock;
   }

   public void setDamagePerBlock(double damagePerBlock) {
      this.damagePerBlock = damagePerBlock;
      Iterator var3 = this.getListeners().iterator();

      while(var3.hasNext()) {
         WorldBorderListener lv = (WorldBorderListener)var3.next();
         lv.onDamagePerBlockChanged(this, damagePerBlock);
      }

   }

   public double getShrinkingSpeed() {
      return this.area.getShrinkingSpeed();
   }

   public int getWarningTime() {
      return this.warningTime;
   }

   public void setWarningTime(int warningTime) {
      this.warningTime = warningTime;
      Iterator var2 = this.getListeners().iterator();

      while(var2.hasNext()) {
         WorldBorderListener lv = (WorldBorderListener)var2.next();
         lv.onWarningTimeChanged(this, warningTime);
      }

   }

   public int getWarningBlocks() {
      return this.warningBlocks;
   }

   public void setWarningBlocks(int warningBlocks) {
      this.warningBlocks = warningBlocks;
      Iterator var2 = this.getListeners().iterator();

      while(var2.hasNext()) {
         WorldBorderListener lv = (WorldBorderListener)var2.next();
         lv.onWarningBlocksChanged(this, warningBlocks);
      }

   }

   public void tick() {
      this.area = this.area.getAreaInstance();
   }

   public Properties write() {
      return new Properties(this);
   }

   public void load(Properties properties) {
      this.setCenter(properties.getCenterX(), properties.getCenterZ());
      this.setDamagePerBlock(properties.getDamagePerBlock());
      this.setSafeZone(properties.getSafeZone());
      this.setWarningBlocks(properties.getWarningBlocks());
      this.setWarningTime(properties.getWarningTime());
      if (properties.getSizeLerpTime() > 0L) {
         this.interpolateSize(properties.getSize(), properties.getSizeLerpTarget(), properties.getSizeLerpTime());
      } else {
         this.setSize(properties.getSize());
      }

   }

   class StaticArea implements Area {
      private final double size;
      private double boundWest;
      private double boundNorth;
      private double boundEast;
      private double boundSouth;
      private VoxelShape shape;

      public StaticArea(double size) {
         this.size = size;
         this.recalculateBounds();
      }

      public double getBoundWest() {
         return this.boundWest;
      }

      public double getBoundEast() {
         return this.boundEast;
      }

      public double getBoundNorth() {
         return this.boundNorth;
      }

      public double getBoundSouth() {
         return this.boundSouth;
      }

      public double getSize() {
         return this.size;
      }

      public WorldBorderStage getStage() {
         return WorldBorderStage.STATIONARY;
      }

      public double getShrinkingSpeed() {
         return 0.0;
      }

      public long getSizeLerpTime() {
         return 0L;
      }

      public double getSizeLerpTarget() {
         return this.size;
      }

      private void recalculateBounds() {
         this.boundWest = MathHelper.clamp(WorldBorder.this.getCenterX() - this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
         this.boundNorth = MathHelper.clamp(WorldBorder.this.getCenterZ() - this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
         this.boundEast = MathHelper.clamp(WorldBorder.this.getCenterX() + this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
         this.boundSouth = MathHelper.clamp(WorldBorder.this.getCenterZ() + this.size / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
         this.shape = VoxelShapes.combineAndSimplify(VoxelShapes.UNBOUNDED, VoxelShapes.cuboid(Math.floor(this.getBoundWest()), Double.NEGATIVE_INFINITY, Math.floor(this.getBoundNorth()), Math.ceil(this.getBoundEast()), Double.POSITIVE_INFINITY, Math.ceil(this.getBoundSouth())), BooleanBiFunction.ONLY_FIRST);
      }

      public void onMaxRadiusChanged() {
         this.recalculateBounds();
      }

      public void onCenterChanged() {
         this.recalculateBounds();
      }

      public Area getAreaInstance() {
         return this;
      }

      public VoxelShape asVoxelShape() {
         return this.shape;
      }
   }

   private interface Area {
      double getBoundWest();

      double getBoundEast();

      double getBoundNorth();

      double getBoundSouth();

      double getSize();

      double getShrinkingSpeed();

      long getSizeLerpTime();

      double getSizeLerpTarget();

      WorldBorderStage getStage();

      void onMaxRadiusChanged();

      void onCenterChanged();

      Area getAreaInstance();

      VoxelShape asVoxelShape();
   }

   private class MovingArea implements Area {
      private final double oldSize;
      private final double newSize;
      private final long timeEnd;
      private final long timeStart;
      private final double timeDuration;

      MovingArea(double oldSize, double newSize, long timeDuration) {
         this.oldSize = oldSize;
         this.newSize = newSize;
         this.timeDuration = (double)timeDuration;
         this.timeStart = Util.getMeasuringTimeMs();
         this.timeEnd = this.timeStart + timeDuration;
      }

      public double getBoundWest() {
         return MathHelper.clamp(WorldBorder.this.getCenterX() - this.getSize() / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
      }

      public double getBoundNorth() {
         return MathHelper.clamp(WorldBorder.this.getCenterZ() - this.getSize() / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
      }

      public double getBoundEast() {
         return MathHelper.clamp(WorldBorder.this.getCenterX() + this.getSize() / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
      }

      public double getBoundSouth() {
         return MathHelper.clamp(WorldBorder.this.getCenterZ() + this.getSize() / 2.0, (double)(-WorldBorder.this.maxRadius), (double)WorldBorder.this.maxRadius);
      }

      public double getSize() {
         double d = (double)(Util.getMeasuringTimeMs() - this.timeStart) / this.timeDuration;
         return d < 1.0 ? MathHelper.lerp(d, this.oldSize, this.newSize) : this.newSize;
      }

      public double getShrinkingSpeed() {
         return Math.abs(this.oldSize - this.newSize) / (double)(this.timeEnd - this.timeStart);
      }

      public long getSizeLerpTime() {
         return this.timeEnd - Util.getMeasuringTimeMs();
      }

      public double getSizeLerpTarget() {
         return this.newSize;
      }

      public WorldBorderStage getStage() {
         return this.newSize < this.oldSize ? WorldBorderStage.SHRINKING : WorldBorderStage.GROWING;
      }

      public void onCenterChanged() {
      }

      public void onMaxRadiusChanged() {
      }

      public Area getAreaInstance() {
         return (Area)(this.getSizeLerpTime() <= 0L ? WorldBorder.this.new StaticArea(this.newSize) : this);
      }

      public VoxelShape asVoxelShape() {
         return VoxelShapes.combineAndSimplify(VoxelShapes.UNBOUNDED, VoxelShapes.cuboid(Math.floor(this.getBoundWest()), Double.NEGATIVE_INFINITY, Math.floor(this.getBoundNorth()), Math.ceil(this.getBoundEast()), Double.POSITIVE_INFINITY, Math.ceil(this.getBoundSouth())), BooleanBiFunction.ONLY_FIRST);
      }
   }

   public static class Properties {
      private final double centerX;
      private final double centerZ;
      private final double damagePerBlock;
      private final double safeZone;
      private final int warningBlocks;
      private final int warningTime;
      private final double size;
      private final long sizeLerpTime;
      private final double sizeLerpTarget;

      Properties(double centerX, double centerZ, double damagePerBlock, double safeZone, int warningBlocks, int warningTime, double size, long sizeLerpTime, double sizeLerpTarget) {
         this.centerX = centerX;
         this.centerZ = centerZ;
         this.damagePerBlock = damagePerBlock;
         this.safeZone = safeZone;
         this.warningBlocks = warningBlocks;
         this.warningTime = warningTime;
         this.size = size;
         this.sizeLerpTime = sizeLerpTime;
         this.sizeLerpTarget = sizeLerpTarget;
      }

      Properties(WorldBorder worldBorder) {
         this.centerX = worldBorder.getCenterX();
         this.centerZ = worldBorder.getCenterZ();
         this.damagePerBlock = worldBorder.getDamagePerBlock();
         this.safeZone = worldBorder.getSafeZone();
         this.warningBlocks = worldBorder.getWarningBlocks();
         this.warningTime = worldBorder.getWarningTime();
         this.size = worldBorder.getSize();
         this.sizeLerpTime = worldBorder.getSizeLerpTime();
         this.sizeLerpTarget = worldBorder.getSizeLerpTarget();
      }

      public double getCenterX() {
         return this.centerX;
      }

      public double getCenterZ() {
         return this.centerZ;
      }

      public double getDamagePerBlock() {
         return this.damagePerBlock;
      }

      public double getSafeZone() {
         return this.safeZone;
      }

      public int getWarningBlocks() {
         return this.warningBlocks;
      }

      public int getWarningTime() {
         return this.warningTime;
      }

      public double getSize() {
         return this.size;
      }

      public long getSizeLerpTime() {
         return this.sizeLerpTime;
      }

      public double getSizeLerpTarget() {
         return this.sizeLerpTarget;
      }

      public static Properties fromDynamic(DynamicLike dynamic, Properties properties) {
         double d = MathHelper.clamp(dynamic.get("BorderCenterX").asDouble(properties.centerX), -2.9999984E7, 2.9999984E7);
         double e = MathHelper.clamp(dynamic.get("BorderCenterZ").asDouble(properties.centerZ), -2.9999984E7, 2.9999984E7);
         double f = dynamic.get("BorderSize").asDouble(properties.size);
         long l = dynamic.get("BorderSizeLerpTime").asLong(properties.sizeLerpTime);
         double g = dynamic.get("BorderSizeLerpTarget").asDouble(properties.sizeLerpTarget);
         double h = dynamic.get("BorderSafeZone").asDouble(properties.safeZone);
         double i = dynamic.get("BorderDamagePerBlock").asDouble(properties.damagePerBlock);
         int j = dynamic.get("BorderWarningBlocks").asInt(properties.warningBlocks);
         int k = dynamic.get("BorderWarningTime").asInt(properties.warningTime);
         return new Properties(d, e, i, h, j, k, f, l, g);
      }

      public void writeNbt(NbtCompound nbt) {
         nbt.putDouble("BorderCenterX", this.centerX);
         nbt.putDouble("BorderCenterZ", this.centerZ);
         nbt.putDouble("BorderSize", this.size);
         nbt.putLong("BorderSizeLerpTime", this.sizeLerpTime);
         nbt.putDouble("BorderSafeZone", this.safeZone);
         nbt.putDouble("BorderDamagePerBlock", this.damagePerBlock);
         nbt.putDouble("BorderSizeLerpTarget", this.sizeLerpTarget);
         nbt.putDouble("BorderWarningBlocks", (double)this.warningBlocks);
         nbt.putDouble("BorderWarningTime", (double)this.warningTime);
      }
   }
}
