package net.minecraft.entity.vehicle;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FurnaceMinecartEntity extends AbstractMinecartEntity {
   private static final TrackedData LIT;
   private int fuel;
   public double pushX;
   public double pushZ;
   private static final Ingredient ACCEPTABLE_FUEL;

   public FurnaceMinecartEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public FurnaceMinecartEntity(World world, double x, double y, double z) {
      super(EntityType.FURNACE_MINECART, world, x, y, z);
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.FURNACE;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(LIT, false);
   }

   public void tick() {
      super.tick();
      if (!this.getWorld().isClient()) {
         if (this.fuel > 0) {
            --this.fuel;
         }

         if (this.fuel <= 0) {
            this.pushX = 0.0;
            this.pushZ = 0.0;
         }

         this.setLit(this.fuel > 0);
      }

      if (this.isLit() && this.random.nextInt(4) == 0) {
         this.getWorld().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8, this.getZ(), 0.0, 0.0, 0.0);
      }

   }

   protected double getMaxSpeed() {
      return (this.isTouchingWater() ? 3.0 : 4.0) / 20.0;
   }

   protected Item getItem() {
      return Items.FURNACE_MINECART;
   }

   protected void moveOnRail(BlockPos pos, BlockState state) {
      double d = 1.0E-4;
      double e = 0.001;
      super.moveOnRail(pos, state);
      Vec3d lv = this.getVelocity();
      double f = lv.horizontalLengthSquared();
      double g = this.pushX * this.pushX + this.pushZ * this.pushZ;
      if (g > 1.0E-4 && f > 0.001) {
         double h = Math.sqrt(f);
         double i = Math.sqrt(g);
         this.pushX = lv.x / h * i;
         this.pushZ = lv.z / h * i;
      }

   }

   protected void applySlowdown() {
      double d = this.pushX * this.pushX + this.pushZ * this.pushZ;
      if (d > 1.0E-7) {
         d = Math.sqrt(d);
         this.pushX /= d;
         this.pushZ /= d;
         Vec3d lv = this.getVelocity().multiply(0.8, 0.0, 0.8).add(this.pushX, 0.0, this.pushZ);
         if (this.isTouchingWater()) {
            lv = lv.multiply(0.1);
         }

         this.setVelocity(lv);
      } else {
         this.setVelocity(this.getVelocity().multiply(0.98, 0.0, 0.98));
      }

      super.applySlowdown();
   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      ItemStack lv = player.getStackInHand(hand);
      if (ACCEPTABLE_FUEL.test(lv) && this.fuel + 3600 <= 32000) {
         if (!player.getAbilities().creativeMode) {
            lv.decrement(1);
         }

         this.fuel += 3600;
      }

      if (this.fuel > 0) {
         this.pushX = this.getX() - player.getX();
         this.pushZ = this.getZ() - player.getZ();
      }

      return ActionResult.success(this.getWorld().isClient);
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putDouble("PushX", this.pushX);
      nbt.putDouble("PushZ", this.pushZ);
      nbt.putShort("Fuel", (short)this.fuel);
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.pushX = nbt.getDouble("PushX");
      this.pushZ = nbt.getDouble("PushZ");
      this.fuel = nbt.getShort("Fuel");
   }

   protected boolean isLit() {
      return (Boolean)this.dataTracker.get(LIT);
   }

   protected void setLit(boolean lit) {
      this.dataTracker.set(LIT, lit);
   }

   public BlockState getDefaultContainedBlock() {
      return (BlockState)((BlockState)Blocks.FURNACE.getDefaultState().with(FurnaceBlock.FACING, Direction.NORTH)).with(FurnaceBlock.LIT, this.isLit());
   }

   static {
      LIT = DataTracker.registerData(FurnaceMinecartEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      ACCEPTABLE_FUEL = Ingredient.ofItems(Items.COAL, Items.CHARCOAL);
   }
}
