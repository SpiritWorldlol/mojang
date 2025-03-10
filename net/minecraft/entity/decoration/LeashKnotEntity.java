package net.minecraft.entity.decoration;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class LeashKnotEntity extends AbstractDecorationEntity {
   public static final double field_30455 = 0.375;

   public LeashKnotEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public LeashKnotEntity(World world, BlockPos pos) {
      super(EntityType.LEASH_KNOT, world, pos);
      this.setPosition((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
   }

   protected void updateAttachmentPosition() {
      this.setPos((double)this.attachmentPos.getX() + 0.5, (double)this.attachmentPos.getY() + 0.375, (double)this.attachmentPos.getZ() + 0.5);
      double d = (double)this.getType().getWidth() / 2.0;
      double e = (double)this.getType().getHeight();
      this.setBoundingBox(new Box(this.getX() - d, this.getY(), this.getZ() - d, this.getX() + d, this.getY() + e, this.getZ() + d));
   }

   public void setFacing(Direction facing) {
   }

   public int getWidthPixels() {
      return 9;
   }

   public int getHeightPixels() {
      return 9;
   }

   protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.0625F;
   }

   public boolean shouldRender(double distance) {
      return distance < 1024.0;
   }

   public void onBreak(@Nullable Entity entity) {
      this.playSound(SoundEvents.ENTITY_LEASH_KNOT_BREAK, 1.0F, 1.0F);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      if (this.getWorld().isClient) {
         return ActionResult.SUCCESS;
      } else {
         boolean bl = false;
         double d = 7.0;
         List list = this.getWorld().getNonSpectatingEntities(MobEntity.class, new Box(this.getX() - 7.0, this.getY() - 7.0, this.getZ() - 7.0, this.getX() + 7.0, this.getY() + 7.0, this.getZ() + 7.0));
         Iterator var7 = list.iterator();

         while(var7.hasNext()) {
            MobEntity lv = (MobEntity)var7.next();
            if (lv.getHoldingEntity() == player) {
               lv.attachLeash(this, true);
               bl = true;
            }
         }

         boolean bl2 = false;
         if (!bl) {
            this.discard();
            if (player.getAbilities().creativeMode) {
               Iterator var11 = list.iterator();

               while(var11.hasNext()) {
                  MobEntity lv2 = (MobEntity)var11.next();
                  if (lv2.isLeashed() && lv2.getHoldingEntity() == this) {
                     lv2.detachLeash(true, false);
                     bl2 = true;
                  }
               }
            }
         }

         if (bl || bl2) {
            this.emitGameEvent(GameEvent.BLOCK_ATTACH, player);
         }

         return ActionResult.CONSUME;
      }
   }

   public boolean canStayAttached() {
      return this.getWorld().getBlockState(this.attachmentPos).isIn(BlockTags.FENCES);
   }

   public static LeashKnotEntity getOrCreate(World world, BlockPos pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      List list = world.getNonSpectatingEntities(LeashKnotEntity.class, new Box((double)i - 1.0, (double)j - 1.0, (double)k - 1.0, (double)i + 1.0, (double)j + 1.0, (double)k + 1.0));
      Iterator var6 = list.iterator();

      LeashKnotEntity lv;
      do {
         if (!var6.hasNext()) {
            LeashKnotEntity lv2 = new LeashKnotEntity(world, pos);
            world.spawnEntity(lv2);
            return lv2;
         }

         lv = (LeashKnotEntity)var6.next();
      } while(!lv.getDecorationBlockPos().equals(pos));

      return lv;
   }

   public void onPlace() {
      this.playSound(SoundEvents.ENTITY_LEASH_KNOT_PLACE, 1.0F, 1.0F);
   }

   public Packet createSpawnPacket() {
      return new EntitySpawnS2CPacket(this, 0, this.getDecorationBlockPos());
   }

   public Vec3d getLeashPos(float delta) {
      return this.getLerpedPos(delta).add(0.0, 0.2, 0.0);
   }

   public ItemStack getPickBlockStack() {
      return new ItemStack(Items.LEAD);
   }
}
