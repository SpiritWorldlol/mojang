package net.minecraft.network.packet.c2s.login;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerLoginPacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

public class LoginQueryResponseC2SPacket implements Packet {
   private static final int MAX_PAYLOAD_SIZE = 1048576;
   private final int queryId;
   @Nullable
   private final PacketByteBuf response;

   public LoginQueryResponseC2SPacket(int queryId, @Nullable PacketByteBuf response) {
      this.queryId = queryId;
      this.response = response;
   }

   public LoginQueryResponseC2SPacket(PacketByteBuf buf) {
      this.queryId = buf.readVarInt();
      this.response = (PacketByteBuf)buf.readNullable((buf2) -> {
         int i = buf2.readableBytes();
         if (i >= 0 && i <= 1048576) {
            return new PacketByteBuf(buf2.readBytes(i));
         } else {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
         }
      });
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.queryId);
      buf.writeNullable(this.response, (buf2, response) -> {
         buf2.writeBytes(response.slice());
      });
   }

   public void apply(ServerLoginPacketListener arg) {
      arg.onQueryResponse(this);
   }

   public int getQueryId() {
      return this.queryId;
   }

   @Nullable
   public PacketByteBuf getResponse() {
      return this.response;
   }
}
