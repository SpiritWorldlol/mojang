package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;

public class PacketDeflater extends MessageToByteEncoder {
   private final byte[] deflateBuffer = new byte[8192];
   private final Deflater deflater;
   private int compressionThreshold;

   public PacketDeflater(int compressionThreshold) {
      this.compressionThreshold = compressionThreshold;
      this.deflater = new Deflater();
   }

   protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
      int i = byteBuf.readableBytes();
      PacketByteBuf lv = new PacketByteBuf(byteBuf2);
      if (i < this.compressionThreshold) {
         lv.writeVarInt(0);
         lv.writeBytes(byteBuf);
      } else {
         byte[] bs = new byte[i];
         byteBuf.readBytes(bs);
         lv.writeVarInt(bs.length);
         this.deflater.setInput(bs, 0, i);
         this.deflater.finish();

         while(!this.deflater.finished()) {
            int j = this.deflater.deflate(this.deflateBuffer);
            lv.writeBytes((byte[])this.deflateBuffer, 0, j);
         }

         this.deflater.reset();
      }

   }

   public int getCompressionThreshold() {
      return this.compressionThreshold;
   }

   public void setCompressionThreshold(int compressionThreshold) {
      this.compressionThreshold = compressionThreshold;
   }

   // $FF: synthetic method
   protected void encode(ChannelHandlerContext ctx, Object input, ByteBuf output) throws Exception {
      this.encode(ctx, (ByteBuf)input, output);
   }
}
