package net.minecraft.network.packet.s2c.play;

import java.util.BitSet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

public class ChunkDataS2CPacket implements Packet {
   private final int chunkX;
   private final int chunkZ;
   private final ChunkData chunkData;
   private final LightData lightData;

   public ChunkDataS2CPacket(WorldChunk chunk, LightingProvider lightProvider, @Nullable BitSet skyBits, @Nullable BitSet blockBits) {
      ChunkPos lv = chunk.getPos();
      this.chunkX = lv.x;
      this.chunkZ = lv.z;
      this.chunkData = new ChunkData(chunk);
      this.lightData = new LightData(lv, lightProvider, skyBits, blockBits);
   }

   public ChunkDataS2CPacket(PacketByteBuf buf) {
      this.chunkX = buf.readInt();
      this.chunkZ = buf.readInt();
      this.chunkData = new ChunkData(buf, this.chunkX, this.chunkZ);
      this.lightData = new LightData(buf, this.chunkX, this.chunkZ);
   }

   public void write(PacketByteBuf buf) {
      buf.writeInt(this.chunkX);
      buf.writeInt(this.chunkZ);
      this.chunkData.write(buf);
      this.lightData.write(buf);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onChunkData(this);
   }

   public int getX() {
      return this.chunkX;
   }

   public int getZ() {
      return this.chunkZ;
   }

   public ChunkData getChunkData() {
      return this.chunkData;
   }

   public LightData getLightData() {
      return this.lightData;
   }
}
