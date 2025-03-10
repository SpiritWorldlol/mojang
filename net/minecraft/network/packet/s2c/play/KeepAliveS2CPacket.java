package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class KeepAliveS2CPacket implements Packet {
   private final long id;

   public KeepAliveS2CPacket(long id) {
      this.id = id;
   }

   public KeepAliveS2CPacket(PacketByteBuf buf) {
      this.id = buf.readLong();
   }

   public void write(PacketByteBuf buf) {
      buf.writeLong(this.id);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onKeepAlive(this);
   }

   public long getId() {
      return this.id;
   }
}
