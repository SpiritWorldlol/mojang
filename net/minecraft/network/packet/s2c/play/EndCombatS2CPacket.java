package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class EndCombatS2CPacket implements Packet {
   private final int attackerId;
   private final int timeSinceLastAttack;

   public EndCombatS2CPacket(DamageTracker damageTracker) {
      this(damageTracker.getBiggestAttackerId(), damageTracker.getTimeSinceLastAttack());
   }

   public EndCombatS2CPacket(int attackerId, int timeSinceLastAttack) {
      this.attackerId = attackerId;
      this.timeSinceLastAttack = timeSinceLastAttack;
   }

   public EndCombatS2CPacket(PacketByteBuf buf) {
      this.timeSinceLastAttack = buf.readVarInt();
      this.attackerId = buf.readInt();
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.timeSinceLastAttack);
      buf.writeInt(this.attackerId);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onEndCombat(this);
   }
}
