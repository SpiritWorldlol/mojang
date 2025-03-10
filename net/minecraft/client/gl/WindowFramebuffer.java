package net.minecraft.client.gl;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class WindowFramebuffer extends Framebuffer {
   public static final int DEFAULT_WIDTH = 854;
   public static final int DEFAULT_HEIGHT = 480;
   static final Size DEFAULT = new Size(854, 480);

   public WindowFramebuffer(int width, int height) {
      super(true);
      RenderSystem.assertOnRenderThreadOrInit();
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this.init(width, height);
         });
      } else {
         this.init(width, height);
      }

   }

   private void init(int width, int height) {
      RenderSystem.assertOnRenderThreadOrInit();
      Size lv = this.findSuitableSize(width, height);
      this.fbo = GlStateManager.glGenFramebuffers();
      GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this.fbo);
      GlStateManager._bindTexture(this.colorAttachment);
      GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
      GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
      GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
      GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
      GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GlConst.GL_COLOR_ATTACHMENT0, GlConst.GL_TEXTURE_2D, this.colorAttachment, 0);
      GlStateManager._bindTexture(this.depthAttachment);
      GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_COMPARE_MODE, 0);
      GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
      GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
      GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
      GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
      GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GlConst.GL_DEPTH_ATTACHMENT, GlConst.GL_TEXTURE_2D, this.depthAttachment, 0);
      GlStateManager._bindTexture(0);
      this.viewportWidth = lv.width;
      this.viewportHeight = lv.height;
      this.textureWidth = lv.width;
      this.textureHeight = lv.height;
      this.checkFramebufferStatus();
      GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
   }

   private Size findSuitableSize(int width, int height) {
      RenderSystem.assertOnRenderThreadOrInit();
      this.colorAttachment = TextureUtil.generateTextureId();
      this.depthAttachment = TextureUtil.generateTextureId();
      Attachment lv = WindowFramebuffer.Attachment.NONE;
      Iterator var4 = WindowFramebuffer.Size.findCompatible(width, height).iterator();

      Size lv2;
      do {
         if (!var4.hasNext()) {
            throw new RuntimeException("Unrecoverable GL_OUT_OF_MEMORY (allocated attachments = " + lv.name() + ")");
         }

         lv2 = (Size)var4.next();
         lv = WindowFramebuffer.Attachment.NONE;
         if (this.supportsColor(lv2)) {
            lv = lv.with(WindowFramebuffer.Attachment.COLOR);
         }

         if (this.supportsDepth(lv2)) {
            lv = lv.with(WindowFramebuffer.Attachment.DEPTH);
         }
      } while(lv != WindowFramebuffer.Attachment.COLOR_DEPTH);

      return lv2;
   }

   private boolean supportsColor(Size size) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._getError();
      GlStateManager._bindTexture(this.colorAttachment);
      GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GlConst.GL_RGBA8, size.width, size.height, 0, GlConst.GL_RGBA, GlConst.GL_UNSIGNED_BYTE, (IntBuffer)null);
      return GlStateManager._getError() != GlConst.GL_OUT_OF_MEMORY;
   }

   private boolean supportsDepth(Size size) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._getError();
      GlStateManager._bindTexture(this.depthAttachment);
      GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GlConst.GL_DEPTH_COMPONENT, size.width, size.height, 0, GlConst.GL_DEPTH_COMPONENT, GlConst.GL_FLOAT, (IntBuffer)null);
      return GlStateManager._getError() != GlConst.GL_OUT_OF_MEMORY;
   }

   @Environment(EnvType.CLIENT)
   static class Size {
      public final int width;
      public final int height;

      Size(int width, int height) {
         this.width = width;
         this.height = height;
      }

      static List findCompatible(int width, int height) {
         RenderSystem.assertOnRenderThreadOrInit();
         int k = RenderSystem.maxSupportedTextureSize();
         return width > 0 && width <= k && height > 0 && height <= k ? ImmutableList.of(new Size(width, height), WindowFramebuffer.DEFAULT) : ImmutableList.of(WindowFramebuffer.DEFAULT);
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Size lv = (Size)o;
            return this.width == lv.width && this.height == lv.height;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.width, this.height});
      }

      public String toString() {
         return this.width + "x" + this.height;
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum Attachment {
      NONE,
      COLOR,
      DEPTH,
      COLOR_DEPTH;

      private static final Attachment[] VALUES = values();

      Attachment with(Attachment other) {
         return VALUES[this.ordinal() | other.ordinal()];
      }

      // $FF: synthetic method
      private static Attachment[] method_36806() {
         return new Attachment[]{NONE, COLOR, DEPTH, COLOR_DEPTH};
      }
   }
}
