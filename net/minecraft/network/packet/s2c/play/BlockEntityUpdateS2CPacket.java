package net.minecraft.network.packet.s2c.play;

import java.util.function.Function;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BlockEntityUpdateS2CPacket implements Packet {
   private final BlockPos pos;
   private final BlockEntityType blockEntityType;
   @Nullable
   private final NbtCompound nbt;

   public static BlockEntityUpdateS2CPacket create(BlockEntity blockEntity, Function nbtGetter) {
      return new BlockEntityUpdateS2CPacket(blockEntity.getPos(), blockEntity.getType(), (NbtCompound)nbtGetter.apply(blockEntity));
   }

   public static BlockEntityUpdateS2CPacket create(BlockEntity blockEntity) {
      return create(blockEntity, BlockEntity::toInitialChunkDataNbt);
   }

   private BlockEntityUpdateS2CPacket(BlockPos pos, BlockEntityType blockEntityType, NbtCompound nbt) {
      this.pos = pos;
      this.blockEntityType = blockEntityType;
      this.nbt = nbt.isEmpty() ? null : nbt;
   }

   public BlockEntityUpdateS2CPacket(PacketByteBuf buf) {
      this.pos = buf.readBlockPos();
      this.blockEntityType = (BlockEntityType)buf.readRegistryValue(Registries.BLOCK_ENTITY_TYPE);
      this.nbt = buf.readNbt();
   }

   public void write(PacketByteBuf buf) {
      buf.writeBlockPos(this.pos);
      buf.writeRegistryValue(Registries.BLOCK_ENTITY_TYPE, this.blockEntityType);
      buf.writeNbt(this.nbt);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onBlockEntityUpdate(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public BlockEntityType getBlockEntityType() {
      return this.blockEntityType;
   }

   @Nullable
   public NbtCompound getNbt() {
      return this.nbt;
   }
}
