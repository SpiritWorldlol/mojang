package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class KeepAliveC2SPacket implements Packet {
   private final long id;

   public KeepAliveC2SPacket(long id) {
      this.id = id;
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onKeepAlive(this);
   }

   public KeepAliveC2SPacket(PacketByteBuf buf) {
      this.id = buf.readLong();
   }

   public void write(PacketByteBuf buf) {
      buf.writeLong(this.id);
   }

   public long getId() {
      return this.id;
   }
}
