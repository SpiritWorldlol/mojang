package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import java.util.BitSet;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

public class LightData {
   private final BitSet initedSky;
   private final BitSet initedBlock;
   private final BitSet uninitedSky;
   private final BitSet uninitedBlock;
   private final List skyNibbles;
   private final List blockNibbles;

   public LightData(ChunkPos pos, LightingProvider lightProvider, @Nullable BitSet skyBits, @Nullable BitSet blockBits) {
      this.initedSky = new BitSet();
      this.initedBlock = new BitSet();
      this.uninitedSky = new BitSet();
      this.uninitedBlock = new BitSet();
      this.skyNibbles = Lists.newArrayList();
      this.blockNibbles = Lists.newArrayList();

      for(int i = 0; i < lightProvider.getHeight(); ++i) {
         if (skyBits == null || skyBits.get(i)) {
            this.putChunk(pos, lightProvider, LightType.SKY, i, this.initedSky, this.uninitedSky, this.skyNibbles);
         }

         if (blockBits == null || blockBits.get(i)) {
            this.putChunk(pos, lightProvider, LightType.BLOCK, i, this.initedBlock, this.uninitedBlock, this.blockNibbles);
         }
      }

   }

   public LightData(PacketByteBuf buf, int x, int y) {
      this.initedSky = buf.readBitSet();
      this.initedBlock = buf.readBitSet();
      this.uninitedSky = buf.readBitSet();
      this.uninitedBlock = buf.readBitSet();
      this.skyNibbles = buf.readList((b) -> {
         return b.readByteArray(2048);
      });
      this.blockNibbles = buf.readList((b) -> {
         return b.readByteArray(2048);
      });
   }

   public void write(PacketByteBuf buf) {
      buf.writeBitSet(this.initedSky);
      buf.writeBitSet(this.initedBlock);
      buf.writeBitSet(this.uninitedSky);
      buf.writeBitSet(this.uninitedBlock);
      buf.writeCollection(this.skyNibbles, PacketByteBuf::writeByteArray);
      buf.writeCollection(this.blockNibbles, PacketByteBuf::writeByteArray);
   }

   private void putChunk(ChunkPos pos, LightingProvider lightProvider, LightType type, int y, BitSet initialized, BitSet uninitialized, List nibbles) {
      ChunkNibbleArray lv = lightProvider.get(type).getLightSection(ChunkSectionPos.from(pos, lightProvider.getBottomY() + y));
      if (lv != null) {
         if (lv.isUninitialized()) {
            uninitialized.set(y);
         } else {
            initialized.set(y);
            nibbles.add(lv.copy().asByteArray());
         }
      }

   }

   public BitSet getInitedSky() {
      return this.initedSky;
   }

   public BitSet getUninitedSky() {
      return this.uninitedSky;
   }

   public List getSkyNibbles() {
      return this.skyNibbles;
   }

   public BitSet getInitedBlock() {
      return this.initedBlock;
   }

   public BitSet getUninitedBlock() {
      return this.uninitedBlock;
   }

   public List getBlockNibbles() {
      return this.blockNibbles;
   }
}
