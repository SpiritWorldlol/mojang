package net.minecraft.block.entity;

import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class DecoratedPotBlockEntity extends BlockEntity {
   public static final String SHERDS_NBT_KEY = "sherds";
   private Sherds sherds;

   public DecoratedPotBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.DECORATED_POT, pos, state);
      this.sherds = DecoratedPotBlockEntity.Sherds.DEFAULT;
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      this.sherds.toNbt(nbt);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.sherds = DecoratedPotBlockEntity.Sherds.fromNbt(nbt);
   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.createNbt();
   }

   public Direction getHorizontalFacing() {
      return (Direction)this.getCachedState().get(Properties.HORIZONTAL_FACING);
   }

   public Sherds getSherds() {
      return this.sherds;
   }

   public void readNbtFromStack(ItemStack stack) {
      this.sherds = DecoratedPotBlockEntity.Sherds.fromNbt(BlockItem.getBlockEntityNbt(stack));
   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }

   public static record Sherds(Item back, Item left, Item right, Item front) {
      public static final Sherds DEFAULT;

      public Sherds(Item arg, Item arg2, Item arg3, Item arg4) {
         this.back = arg;
         this.left = arg2;
         this.right = arg3;
         this.front = arg4;
      }

      public NbtCompound toNbt(NbtCompound nbt) {
         NbtList lv = new NbtList();
         this.stream().forEach((sherd) -> {
            lv.add(NbtString.of(Registries.ITEM.getId(sherd).toString()));
         });
         nbt.put("sherds", lv);
         return nbt;
      }

      public Stream stream() {
         return Stream.of(this.back, this.left, this.right, this.front);
      }

      public static Sherds fromNbt(@Nullable NbtCompound nbt) {
         if (nbt != null && nbt.contains("sherds", NbtElement.LIST_TYPE)) {
            NbtList lv = nbt.getList("sherds", NbtElement.STRING_TYPE);
            return new Sherds(getSherd(lv, 0), getSherd(lv, 1), getSherd(lv, 2), getSherd(lv, 3));
         } else {
            return DEFAULT;
         }
      }

      private static Item getSherd(NbtList list, int index) {
         if (index >= list.size()) {
            return Items.BRICK;
         } else {
            NbtElement lv = list.get(index);
            return (Item)Registries.ITEM.get(new Identifier(lv.asString()));
         }
      }

      public Item back() {
         return this.back;
      }

      public Item left() {
         return this.left;
      }

      public Item right() {
         return this.right;
      }

      public Item front() {
         return this.front;
      }

      static {
         DEFAULT = new Sherds(Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK);
      }
   }
}
