package net.minecraft.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ScreenshotRecorder {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String SCREENSHOTS_DIRECTORY = "screenshots";
   private int unitHeight;
   private final DataOutputStream stream;
   private final byte[] buffer;
   private final int width;
   private final int height;
   private File file;

   public static void saveScreenshot(File gameDirectory, Framebuffer framebuffer, Consumer messageReceiver) {
      saveScreenshot(gameDirectory, (String)null, framebuffer, messageReceiver);
   }

   public static void saveScreenshot(File gameDirectory, @Nullable String fileName, Framebuffer framebuffer, Consumer messageReceiver) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            saveScreenshotInner(gameDirectory, fileName, framebuffer, messageReceiver);
         });
      } else {
         saveScreenshotInner(gameDirectory, fileName, framebuffer, messageReceiver);
      }

   }

   private static void saveScreenshotInner(File gameDirectory, @Nullable String fileName, Framebuffer framebuffer, Consumer messageReceiver) {
      NativeImage lv = takeScreenshot(framebuffer);
      File file2 = new File(gameDirectory, "screenshots");
      file2.mkdir();
      File file3;
      if (fileName == null) {
         file3 = getScreenshotFilename(file2);
      } else {
         file3 = new File(file2, fileName);
      }

      Util.getIoWorkerExecutor().execute(() -> {
         try {
            lv.writeTo(file3);
            Text lvx = Text.literal(file3.getName()).formatted(Formatting.UNDERLINE).styled((style) -> {
               return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file3.getAbsolutePath()));
            });
            messageReceiver.accept(Text.translatable("screenshot.success", lvx));
         } catch (Exception var7) {
            LOGGER.warn("Couldn't save screenshot", var7);
            messageReceiver.accept(Text.translatable("screenshot.failure", var7.getMessage()));
         } finally {
            lv.close();
         }

      });
   }

   public static NativeImage takeScreenshot(Framebuffer framebuffer) {
      int i = framebuffer.textureWidth;
      int j = framebuffer.textureHeight;
      NativeImage lv = new NativeImage(i, j, false);
      RenderSystem.bindTexture(framebuffer.getColorAttachment());
      lv.loadFromTextureImage(0, true);
      lv.mirrorVertically();
      return lv;
   }

   private static File getScreenshotFilename(File directory) {
      String string = Util.getFormattedCurrentTime();
      int i = 1;

      while(true) {
         File file2 = new File(directory, string + (i == 1 ? "" : "_" + i) + ".png");
         if (!file2.exists()) {
            return file2;
         }

         ++i;
      }
   }

   public ScreenshotRecorder(File gameDirectory, int width, int height, int unitHeight) throws IOException {
      this.width = width;
      this.height = height;
      this.unitHeight = unitHeight;
      File file2 = new File(gameDirectory, "screenshots");
      file2.mkdir();
      String string = "huge_" + Util.getFormattedCurrentTime();

      for(int l = 1; (this.file = new File(file2, string + (l == 1 ? "" : "_" + l) + ".tga")).exists(); ++l) {
      }

      byte[] bs = new byte[18];
      bs[2] = 2;
      bs[12] = (byte)(width % 256);
      bs[13] = (byte)(width / 256);
      bs[14] = (byte)(height % 256);
      bs[15] = (byte)(height / 256);
      bs[16] = 24;
      this.buffer = new byte[width * unitHeight * 3];
      this.stream = new DataOutputStream(new FileOutputStream(this.file));
      this.stream.write(bs);
   }

   public void getIntoBuffer(ByteBuffer data, int startWidth, int startHeight, int unitWidth, int unitHeight) {
      int m = unitWidth;
      int n = unitHeight;
      if (unitWidth > this.width - startWidth) {
         m = this.width - startWidth;
      }

      if (unitHeight > this.height - startHeight) {
         n = this.height - startHeight;
      }

      this.unitHeight = n;

      for(int o = 0; o < n; ++o) {
         data.position((unitHeight - n) * unitWidth * 3 + o * unitWidth * 3);
         int p = (startWidth + o * this.width) * 3;
         data.get(this.buffer, p, m * 3);
      }

   }

   public void writeToStream() throws IOException {
      this.stream.write(this.buffer, 0, this.width * 3 * this.unitHeight);
   }

   public File finish() throws IOException {
      this.stream.close();
      return this.file;
   }
}
