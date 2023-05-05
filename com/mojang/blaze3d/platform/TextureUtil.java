package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.annotation.DeobfuscateClass;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
@DeobfuscateClass
public class TextureUtil {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int MIN_MIPMAP_LEVEL = 0;
   private static final int DEFAULT_IMAGE_BUFFER_SIZE = 8192;

   public static int generateTextureId() {
      RenderSystem.assertOnRenderThreadOrInit();
      if (SharedConstants.isDevelopment) {
         int[] is = new int[ThreadLocalRandom.current().nextInt(15) + 1];
         GlStateManager._genTextures(is);
         int i = GlStateManager._genTexture();
         GlStateManager._deleteTextures(is);
         return i;
      } else {
         return GlStateManager._genTexture();
      }
   }

   public static void releaseTextureId(int id) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._deleteTexture(id);
   }

   public static void prepareImage(int id, int width, int height) {
      prepareImage(NativeImage.InternalFormat.RGBA, id, 0, width, height);
   }

   public static void prepareImage(NativeImage.InternalFormat internalFormat, int id, int width, int height) {
      prepareImage(internalFormat, id, 0, width, height);
   }

   public static void prepareImage(int id, int maxLevel, int width, int height) {
      prepareImage(NativeImage.InternalFormat.RGBA, id, maxLevel, width, height);
   }

   public static void prepareImage(NativeImage.InternalFormat internalFormat, int id, int maxLevel, int width, int height) {
      RenderSystem.assertOnRenderThreadOrInit();
      bind(id);
      if (maxLevel >= 0) {
         GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, maxLevel);
         GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0);
         GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, maxLevel);
         GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);
      }

      for(int m = 0; m <= maxLevel; ++m) {
         GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, m, internalFormat.getValue(), width >> m, height >> m, 0, GlConst.GL_RGBA, GlConst.GL_UNSIGNED_BYTE, (IntBuffer)null);
      }

   }

   private static void bind(int id) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._bindTexture(id);
   }

   public static ByteBuffer readResource(InputStream inputStream) throws IOException {
      ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
      if (readableByteChannel instanceof SeekableByteChannel seekableByteChannel) {
         return readResource(readableByteChannel, (int)seekableByteChannel.size() + 1);
      } else {
         return readResource(readableByteChannel, 8192);
      }
   }

   private static ByteBuffer readResource(ReadableByteChannel channel, int bufSize) throws IOException {
      ByteBuffer byteBuffer = MemoryUtil.memAlloc(bufSize);

      try {
         while(channel.read(byteBuffer) != -1) {
            if (!byteBuffer.hasRemaining()) {
               byteBuffer = MemoryUtil.memRealloc(byteBuffer, byteBuffer.capacity() * 2);
            }
         }

         return byteBuffer;
      } catch (IOException var4) {
         MemoryUtil.memFree(byteBuffer);
         throw var4;
      }
   }

   public static void writeAsPNG(Path directory, String prefix, int textureId, int scales, int width, int height) {
      writeAsPNG(directory, prefix, textureId, scales, width, height, (IntUnaryOperator)null);
   }

   public static void writeAsPNG(Path directory, String prefix, int textureId, int scales, int width, int height, @Nullable IntUnaryOperator operator) {
      RenderSystem.assertOnRenderThread();
      bind(textureId);

      for(int m = 0; m <= scales; ++m) {
         int n = width >> m;
         int o = height >> m;

         try {
            NativeImage lv = new NativeImage(n, o, false);

            try {
               lv.loadFromTextureImage(m, false);
               if (operator != null) {
                  lv.apply(operator);
               }

               Path path2 = directory.resolve(prefix + "_" + m + ".png");
               lv.writeTo(path2);
               LOGGER.debug("Exported png to: {}", path2.toAbsolutePath());
            } catch (Throwable var14) {
               try {
                  lv.close();
               } catch (Throwable var13) {
                  var14.addSuppressed(var13);
               }

               throw var14;
            }

            lv.close();
         } catch (IOException var15) {
            LOGGER.debug("Unable to write: ", var15);
         }
      }

   }

   public static Path getDebugTexturePath(Path path) {
      return path.resolve("screenshots").resolve("debug");
   }

   public static Path getDebugTexturePath() {
      return getDebugTexturePath(Path.of("."));
   }
}
