package net.minecraft.block.entity;

import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ShulkerBoxBlockEntity extends LootableContainerBlockEntity implements SidedInventory {
   public static final int field_31354 = 9;
   public static final int field_31355 = 3;
   public static final int INVENTORY_SIZE = 27;
   public static final int field_31357 = 1;
   public static final int field_31358 = 10;
   public static final float field_31359 = 0.5F;
   public static final float field_31360 = 270.0F;
   public static final String ITEMS_KEY = "Items";
   private static final int[] AVAILABLE_SLOTS = IntStream.range(0, 27).toArray();
   private DefaultedList inventory;
   private int viewerCount;
   private AnimationStage animationStage;
   private float animationProgress;
   private float prevAnimationProgress;
   @Nullable
   private final DyeColor cachedColor;

   public ShulkerBoxBlockEntity(@Nullable DyeColor color, BlockPos pos, BlockState state) {
      super(BlockEntityType.SHULKER_BOX, pos, state);
      this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
      this.animationStage = ShulkerBoxBlockEntity.AnimationStage.CLOSED;
      this.cachedColor = color;
   }

   public ShulkerBoxBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.SHULKER_BOX, pos, state);
      this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
      this.animationStage = ShulkerBoxBlockEntity.AnimationStage.CLOSED;
      this.cachedColor = ShulkerBoxBlock.getColor(state.getBlock());
   }

   public static void tick(World world, BlockPos pos, BlockState state, ShulkerBoxBlockEntity blockEntity) {
      blockEntity.updateAnimation(world, pos, state);
   }

   private void updateAnimation(World world, BlockPos pos, BlockState state) {
      this.prevAnimationProgress = this.animationProgress;
      switch (this.animationStage) {
         case CLOSED:
            this.animationProgress = 0.0F;
            break;
         case OPENING:
            this.animationProgress += 0.1F;
            if (this.animationProgress >= 1.0F) {
               this.animationStage = ShulkerBoxBlockEntity.AnimationStage.OPENED;
               this.animationProgress = 1.0F;
               updateNeighborStates(world, pos, state);
            }

            this.pushEntities(world, pos, state);
            break;
         case CLOSING:
            this.animationProgress -= 0.1F;
            if (this.animationProgress <= 0.0F) {
               this.animationStage = ShulkerBoxBlockEntity.AnimationStage.CLOSED;
               this.animationProgress = 0.0F;
               updateNeighborStates(world, pos, state);
            }
            break;
         case OPENED:
            this.animationProgress = 1.0F;
      }

   }

   public AnimationStage getAnimationStage() {
      return this.animationStage;
   }

   public Box getBoundingBox(BlockState state) {
      return ShulkerEntity.calculateBoundingBox((Direction)state.get(ShulkerBoxBlock.FACING), 0.5F * this.getAnimationProgress(1.0F));
   }

   private void pushEntities(World world, BlockPos pos, BlockState state) {
      if (state.getBlock() instanceof ShulkerBoxBlock) {
         Direction lv = (Direction)state.get(ShulkerBoxBlock.FACING);
         Box lv2 = ShulkerEntity.calculateBoundingBox(lv, this.prevAnimationProgress, this.animationProgress).offset(pos);
         List list = world.getOtherEntities((Entity)null, lv2);
         if (!list.isEmpty()) {
            for(int i = 0; i < list.size(); ++i) {
               Entity lv3 = (Entity)list.get(i);
               if (lv3.getPistonBehavior() != PistonBehavior.IGNORE) {
                  lv3.move(MovementType.SHULKER_BOX, new Vec3d((lv2.getXLength() + 0.01) * (double)lv.getOffsetX(), (lv2.getYLength() + 0.01) * (double)lv.getOffsetY(), (lv2.getZLength() + 0.01) * (double)lv.getOffsetZ()));
               }
            }

         }
      }
   }

   public int size() {
      return this.inventory.size();
   }

   public boolean onSyncedBlockEvent(int type, int data) {
      if (type == 1) {
         this.viewerCount = data;
         if (data == 0) {
            this.animationStage = ShulkerBoxBlockEntity.AnimationStage.CLOSING;
            updateNeighborStates(this.getWorld(), this.pos, this.getCachedState());
         }

         if (data == 1) {
            this.animationStage = ShulkerBoxBlockEntity.AnimationStage.OPENING;
            updateNeighborStates(this.getWorld(), this.pos, this.getCachedState());
         }

         return true;
      } else {
         return super.onSyncedBlockEvent(type, data);
      }
   }

   private static void updateNeighborStates(World world, BlockPos pos, BlockState state) {
      state.updateNeighbors(world, pos, Block.NOTIFY_ALL);
   }

   public void onOpen(PlayerEntity player) {
      if (!this.removed && !player.isSpectator()) {
         if (this.viewerCount < 0) {
            this.viewerCount = 0;
         }

         ++this.viewerCount;
         this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.viewerCount);
         if (this.viewerCount == 1) {
            this.world.emitGameEvent(player, GameEvent.CONTAINER_OPEN, this.pos);
            this.world.playSound((PlayerEntity)null, this.pos, SoundEvents.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
         }
      }

   }

   public void onClose(PlayerEntity player) {
      if (!this.removed && !player.isSpectator()) {
         --this.viewerCount;
         this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.viewerCount);
         if (this.viewerCount <= 0) {
            this.world.emitGameEvent(player, GameEvent.CONTAINER_CLOSE, this.pos);
            this.world.playSound((PlayerEntity)null, this.pos, SoundEvents.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
         }
      }

   }

   protected Text getContainerName() {
      return Text.translatable("container.shulkerBox");
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.readInventoryNbt(nbt);
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (!this.serializeLootTable(nbt)) {
         Inventories.writeNbt(nbt, this.inventory, false);
      }

   }

   public void readInventoryNbt(NbtCompound nbt) {
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
      if (!this.deserializeLootTable(nbt) && nbt.contains("Items", NbtElement.LIST_TYPE)) {
         Inventories.readNbt(nbt, this.inventory);
      }

   }

   protected DefaultedList getInvStackList() {
      return this.inventory;
   }

   protected void setInvStackList(DefaultedList list) {
      this.inventory = list;
   }

   public int[] getAvailableSlots(Direction side) {
      return AVAILABLE_SLOTS;
   }

   public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
      return !(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock);
   }

   public boolean canExtract(int slot, ItemStack stack, Direction dir) {
      return true;
   }

   public float getAnimationProgress(float delta) {
      return MathHelper.lerp(delta, this.prevAnimationProgress, this.animationProgress);
   }

   @Nullable
   public DyeColor getColor() {
      return this.cachedColor;
   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new ShulkerBoxScreenHandler(syncId, playerInventory, this);
   }

   public boolean suffocates() {
      return this.animationStage == ShulkerBoxBlockEntity.AnimationStage.CLOSED;
   }

   public static enum AnimationStage {
      CLOSED,
      OPENING,
      OPENED,
      CLOSING;

      // $FF: synthetic method
      private static AnimationStage[] method_36717() {
         return new AnimationStage[]{CLOSED, OPENING, OPENED, CLOSING};
      }
   }
}
