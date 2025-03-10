package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RemoveEntityStatusEffectS2CPacket implements Packet {
   private final int entityId;
   private final StatusEffect effectType;

   public RemoveEntityStatusEffectS2CPacket(int entityId, StatusEffect effectType) {
      this.entityId = entityId;
      this.effectType = effectType;
   }

   public RemoveEntityStatusEffectS2CPacket(PacketByteBuf buf) {
      this.entityId = buf.readVarInt();
      this.effectType = (StatusEffect)buf.readRegistryValue(Registries.STATUS_EFFECT);
   }

   public void write(PacketByteBuf buf) {
      buf.writeVarInt(this.entityId);
      buf.writeRegistryValue(Registries.STATUS_EFFECT, this.effectType);
   }

   public void apply(ClientPlayPacketListener arg) {
      arg.onRemoveEntityStatusEffect(this);
   }

   @Nullable
   public Entity getEntity(World world) {
      return world.getEntityById(this.entityId);
   }

   @Nullable
   public StatusEffect getEffectType() {
      return this.effectType;
   }
}
