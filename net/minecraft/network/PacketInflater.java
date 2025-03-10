package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.zip.Inflater;

public class PacketInflater extends ByteToMessageDecoder {
   public static final int field_34057 = 2097152;
   public static final int MAXIMUM_PACKET_SIZE = 8388608;
   private final Inflater inflater;
   private int compressionThreshold;
   private boolean rejectsBadPackets;

   public PacketInflater(int compressionThreshold, boolean rejectsBadPackets) {
      this.compressionThreshold = compressionThreshold;
      this.rejectsBadPackets = rejectsBadPackets;
      this.inflater = new Inflater();
   }

   protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List objects) throws Exception {
      if (buf.readableBytes() != 0) {
         PacketByteBuf lv = new PacketByteBuf(buf);
         int i = lv.readVarInt();
         if (i == 0) {
            objects.add(lv.readBytes(lv.readableBytes()));
         } else {
            if (this.rejectsBadPackets) {
               if (i < this.compressionThreshold) {
                  throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.compressionThreshold);
               }

               if (i > 8388608) {
                  throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of 8388608");
               }
            }

            byte[] bs = new byte[lv.readableBytes()];
            lv.readBytes(bs);
            this.inflater.setInput(bs);
            byte[] cs = new byte[i];
            this.inflater.inflate(cs);
            objects.add(Unpooled.wrappedBuffer(cs));
            this.inflater.reset();
         }
      }
   }

   public void setCompressionThreshold(int compressionThreshold, boolean rejectsBadPackets) {
      this.compressionThreshold = compressionThreshold;
      this.rejectsBadPackets = rejectsBadPackets;
   }
}
