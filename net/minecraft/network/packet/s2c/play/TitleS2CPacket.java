package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

public class TitleS2CPacket implements Packet {
   private final Text title;

   public TitleS2CPacket(Text title) {
      this.title = title;
   }

   public TitleS2CPacket(PacketByteBuf buf) {
      this.title = buf.readText();
   }

   public void write(PacketByteBuf buf) {
      buf.writeText(this.title);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onTitle(this);
   }

   public Text getTitle() {
      return this.title;
   }
}
