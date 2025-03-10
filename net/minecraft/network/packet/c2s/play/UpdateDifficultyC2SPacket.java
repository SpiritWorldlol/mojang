package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.Difficulty;

public class UpdateDifficultyC2SPacket implements Packet {
   private final Difficulty difficulty;

   public UpdateDifficultyC2SPacket(Difficulty difficulty) {
      this.difficulty = difficulty;
   }

   public void apply(ServerPlayPacketListener arg) {
      arg.onUpdateDifficulty(this);
   }

   public UpdateDifficultyC2SPacket(PacketByteBuf buf) {
      this.difficulty = Difficulty.byId(buf.readUnsignedByte());
   }

   public void write(PacketByteBuf buf) {
      buf.writeByte(this.difficulty.getId());
   }

   public Difficulty getDifficulty() {
      return this.difficulty;
   }
}
