package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Objects;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BrushableBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class BrushableBlockEntity extends BlockEntity {
   private static final Logger field_42801 = LogUtils.getLogger();
   private static final String LOOT_TABLE_NBT_KEY = "LootTable";
   private static final String LOOT_TABLE_SEED_NBT_KEY = "LootTableSeed";
   private static final String HIT_DIRECTION_NBT_KEY = "hit_direction";
   private static final String ITEM_NBT_KEY = "item";
   private static final int field_42806 = 10;
   private static final int field_42807 = 40;
   private static final int field_42808 = 10;
   private int brushesCount;
   private long nextDustTime;
   private long nextBrushTime;
   private ItemStack item;
   @Nullable
   private Direction hitDirection;
   @Nullable
   private Identifier lootTable;
   private long lootTableSeed;

   public BrushableBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.BRUSHABLE_BLOCK, pos, state);
      this.item = ItemStack.EMPTY;
   }

   public boolean brush(long worldTime, PlayerEntity player, Direction hitDirection) {
      if (this.hitDirection == null) {
         this.hitDirection = hitDirection;
      }

      this.nextDustTime = worldTime + 40L;
      if (worldTime >= this.nextBrushTime && this.world instanceof ServerWorld) {
         this.nextBrushTime = worldTime + 10L;
         this.generateItem(player);
         int i = this.getDustedLevel();
         if (++this.brushesCount >= 10) {
            this.finishBrushing(player);
            return true;
         } else {
            this.world.scheduleBlockTick(this.getPos(), this.getCachedState().getBlock(), 40);
            int j = this.getDustedLevel();
            if (i != j) {
               BlockState lv = this.getCachedState();
               BlockState lv2 = (BlockState)lv.with(Properties.DUSTED, j);
               this.world.setBlockState(this.getPos(), lv2, Block.NOTIFY_ALL);
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public void generateItem(PlayerEntity player) {
      if (this.lootTable != null && this.world != null && !this.world.isClient() && this.world.getServer() != null) {
         LootTable lv = this.world.getServer().getLootManager().getLootTable(this.lootTable);
         if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv2 = (ServerPlayerEntity)player;
            Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger(lv2, this.lootTable);
         }

         LootContext.Builder lv3 = (new LootContext.Builder((ServerWorld)this.world)).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos)).random(this.lootTableSeed).luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
         ObjectArrayList objectArrayList = lv.generateLoot(lv3.build(LootContextTypes.CHEST));
         ItemStack var10001;
         switch (objectArrayList.size()) {
            case 0:
               var10001 = ItemStack.EMPTY;
               break;
            case 1:
               var10001 = (ItemStack)objectArrayList.get(0);
               break;
            default:
               Identifier var10002 = this.lootTable;
               field_42801.warn("Expected max 1 loot from loot table " + var10002 + " got " + objectArrayList.size());
               var10001 = (ItemStack)objectArrayList.get(0);
         }

         this.item = var10001;
         this.lootTable = null;
         this.markDirty();
      }
   }

   private void finishBrushing(PlayerEntity player) {
      if (this.world != null && this.world.getServer() != null) {
         this.spawnItem(player);
         BlockState lv = this.getCachedState();
         this.world.syncWorldEvent(WorldEvents.BLOCK_FINISHED_BRUSHING, this.getPos(), Block.getRawIdFromState(lv));
         Block lv2 = this.getCachedState().getBlock();
         Block lv4;
         if (lv2 instanceof BrushableBlock) {
            BrushableBlock lv3 = (BrushableBlock)lv2;
            lv4 = lv3.getBaseBlock();
         } else {
            lv4 = Blocks.AIR;
         }

         this.world.setBlockState(this.pos, lv4.getDefaultState(), Block.NOTIFY_ALL);
      }
   }

   private void spawnItem(PlayerEntity player) {
      if (this.world != null && this.world.getServer() != null) {
         this.generateItem(player);
         if (!this.item.isEmpty()) {
            double d = (double)EntityType.ITEM.getWidth();
            double e = 1.0 - d;
            double f = d / 2.0;
            Direction lv = (Direction)Objects.requireNonNullElse(this.hitDirection, Direction.UP);
            BlockPos lv2 = this.pos.offset((Direction)lv, 1);
            double g = (double)lv2.getX() + 0.5 * e + f;
            double h = (double)lv2.getY() + 0.5 + (double)(EntityType.ITEM.getHeight() / 2.0F);
            double i = (double)lv2.getZ() + 0.5 * e + f;
            ItemEntity lv3 = new ItemEntity(this.world, g, h, i, this.item.split(this.world.random.nextInt(21) + 10));
            lv3.setVelocity(Vec3d.ZERO);
            this.world.spawnEntity(lv3);
            this.item = ItemStack.EMPTY;
         }

      }
   }

   public void scheduledTick() {
      if (this.world != null) {
         if (this.brushesCount != 0 && this.world.getTime() >= this.nextDustTime) {
            int i = this.getDustedLevel();
            this.brushesCount = Math.max(0, this.brushesCount - 2);
            int j = this.getDustedLevel();
            if (i != j) {
               this.world.setBlockState(this.getPos(), (BlockState)this.getCachedState().with(Properties.DUSTED, j), Block.NOTIFY_ALL);
            }

            int k = true;
            this.nextDustTime = this.world.getTime() + 4L;
         }

         if (this.brushesCount == 0) {
            this.hitDirection = null;
            this.nextDustTime = 0L;
            this.nextBrushTime = 0L;
         } else {
            this.world.scheduleBlockTick(this.getPos(), this.getCachedState().getBlock(), (int)(this.nextDustTime - this.world.getTime()));
         }

      }
   }

   private boolean readLootTableFromNbt(NbtCompound nbt) {
      if (nbt.contains("LootTable", NbtElement.STRING_TYPE)) {
         this.lootTable = new Identifier(nbt.getString("LootTable"));
         this.lootTableSeed = nbt.getLong("LootTableSeed");
         return true;
      } else {
         return false;
      }
   }

   private boolean writeLootTableToNbt(NbtCompound nbt) {
      if (this.lootTable == null) {
         return false;
      } else {
         nbt.putString("LootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            nbt.putLong("LootTableSeed", this.lootTableSeed);
         }

         return true;
      }
   }

   public NbtCompound toInitialChunkDataNbt() {
      NbtCompound lv = super.toInitialChunkDataNbt();
      if (this.hitDirection != null) {
         lv.putInt("hit_direction", this.hitDirection.ordinal());
      }

      lv.put("item", this.item.writeNbt(new NbtCompound()));
      return lv;
   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public void readNbt(NbtCompound nbt) {
      if (!this.readLootTableFromNbt(nbt) && nbt.contains("item")) {
         this.item = ItemStack.fromNbt(nbt.getCompound("item"));
      }

      if (nbt.contains("hit_direction")) {
         this.hitDirection = Direction.values()[nbt.getInt("hit_direction")];
      }

   }

   protected void writeNbt(NbtCompound nbt) {
      if (!this.writeLootTableToNbt(nbt)) {
         nbt.put("item", this.item.writeNbt(new NbtCompound()));
      }

   }

   public void setLootTable(Identifier lootTable, long seed) {
      this.lootTable = lootTable;
      this.lootTableSeed = seed;
   }

   private int getDustedLevel() {
      if (this.brushesCount == 0) {
         return 0;
      } else if (this.brushesCount < 3) {
         return 1;
      } else {
         return this.brushesCount < 6 ? 2 : 3;
      }
   }

   @Nullable
   public Direction getHitDirection() {
      return this.hitDirection;
   }

   public ItemStack getItem() {
      return this.item;
   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }
}
