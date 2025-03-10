package net.minecraft.world.poi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;

public class PointOfInterest {
   private final BlockPos pos;
   private final RegistryEntry type;
   private int freeTickets;
   private final Runnable updateListener;

   public static Codec createCodec(Runnable updateListener) {
      return RecordCodecBuilder.create((instance) -> {
         return instance.group(BlockPos.CODEC.fieldOf("pos").forGetter((poi) -> {
            return poi.pos;
         }), RegistryFixedCodec.of(RegistryKeys.POINT_OF_INTEREST_TYPE).fieldOf("type").forGetter((poi) -> {
            return poi.type;
         }), Codec.INT.fieldOf("free_tickets").orElse(0).forGetter((poi) -> {
            return poi.freeTickets;
         }), RecordCodecBuilder.point(updateListener)).apply(instance, PointOfInterest::new);
      });
   }

   private PointOfInterest(BlockPos pos, RegistryEntry type, int freeTickets, Runnable updateListener) {
      this.pos = pos.toImmutable();
      this.type = type;
      this.freeTickets = freeTickets;
      this.updateListener = updateListener;
   }

   public PointOfInterest(BlockPos pos, RegistryEntry type, Runnable updateListener) {
      this(pos, type, ((PointOfInterestType)type.value()).ticketCount(), updateListener);
   }

   /** @deprecated */
   @Deprecated
   @Debug
   public int getFreeTickets() {
      return this.freeTickets;
   }

   protected boolean reserveTicket() {
      if (this.freeTickets <= 0) {
         return false;
      } else {
         --this.freeTickets;
         this.updateListener.run();
         return true;
      }
   }

   protected boolean releaseTicket() {
      if (this.freeTickets >= ((PointOfInterestType)this.type.value()).ticketCount()) {
         return false;
      } else {
         ++this.freeTickets;
         this.updateListener.run();
         return true;
      }
   }

   public boolean hasSpace() {
      return this.freeTickets > 0;
   }

   public boolean isOccupied() {
      return this.freeTickets != ((PointOfInterestType)this.type.value()).ticketCount();
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public RegistryEntry getType() {
      return this.type;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && this.getClass() == o.getClass() ? Objects.equals(this.pos, ((PointOfInterest)o).pos) : false;
      }
   }

   public int hashCode() {
      return this.pos.hashCode();
   }
}
