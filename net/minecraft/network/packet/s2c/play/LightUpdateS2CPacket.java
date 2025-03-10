package net.minecraft.network.packet.s2c.play;

import java.util.BitSet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

public class LightUpdateS2CPacket implements Packet {
   private final int chunkX;
   private final int chunkZ;
   private final LightData data;

   public LightUpdateS2CPacket(ChunkPos chunkPos, LightingProvider lightProvider, @Nullable BitSet skyBits, @Nullable BitSet blockBits) {
      this.chunkX = chunkPos.x;
      this.chunkZ = chunkPos.z;
      this.data = new LightData(chunkPos, lightProvider, skyBits, blockBits);
   }

   public LightUpdateS2CPacket(PacketByteBuf buf) {
      this.chunkX = buf.readVarInt();
      this.chunkZ = buf.readVarInt();
      this.data = new LightData(buf, this.chunkX, this.chunkZ);
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.chunkX);
      buf.writeVarInt(this.chunkZ);
      this.data.write(buf);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onLightUpdate(this);
   }

   public int getChunkX() {
      return this.chunkX;
   }

   public int getChunkZ() {
      return this.chunkZ;
   }

   public LightData getData() {
      return this.data;
   }
}
