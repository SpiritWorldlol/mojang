package net.minecraft.client.gl;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class VertexBuffer implements AutoCloseable {
   private final class_8555 field_44792;
   private int vertexBufferId;
   private int indexBufferId;
   private int vertexArrayId;
   @Nullable
   private VertexFormat vertexFormat;
   @Nullable
   private RenderSystem.ShapeIndexBuffer sharedSequentialIndexBuffer;
   private VertexFormat.IndexType indexType;
   private int indexCount;
   private VertexFormat.DrawMode drawMode;

   public VertexBuffer(class_8555 arg) {
      this.field_44792 = arg;
      RenderSystem.assertOnRenderThread();
      this.vertexBufferId = GlStateManager._glGenBuffers();
      this.indexBufferId = GlStateManager._glGenBuffers();
      this.vertexArrayId = GlStateManager._glGenVertexArrays();
   }

   public void upload(BufferBuilder.BuiltBuffer buffer) {
      if (!this.isClosed()) {
         RenderSystem.assertOnRenderThread();

         try {
            BufferBuilder.DrawParameters lv = buffer.getParameters();
            this.vertexFormat = this.uploadVertexBuffer(lv, buffer.getVertexBuffer());
            this.sharedSequentialIndexBuffer = this.uploadIndexBuffer(lv, buffer.getIndexBuffer());
            this.indexCount = lv.indexCount();
            this.indexType = lv.indexType();
            this.drawMode = lv.mode();
         } finally {
            buffer.release();
         }

      }
   }

   private VertexFormat uploadVertexBuffer(BufferBuilder.DrawParameters parameters, ByteBuffer vertexBuffer) {
      boolean bl = false;
      if (!parameters.format().equals(this.vertexFormat)) {
         if (this.vertexFormat != null) {
            this.vertexFormat.clearState();
         }

         GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, this.vertexBufferId);
         parameters.format().setupState();
         bl = true;
      }

      if (!parameters.indexOnly()) {
         if (!bl) {
            GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, this.vertexBufferId);
         }

         RenderSystem.glBufferData(GlConst.GL_ARRAY_BUFFER, vertexBuffer, this.field_44792.field_44795);
      }

      return parameters.format();
   }

   @Nullable
   private RenderSystem.ShapeIndexBuffer uploadIndexBuffer(BufferBuilder.DrawParameters parameters, ByteBuffer indexBuffer) {
      if (!parameters.sequentialIndex()) {
         GlStateManager._glBindBuffer(GlConst.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
         RenderSystem.glBufferData(GlConst.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, this.field_44792.field_44795);
         return null;
      } else {
         RenderSystem.ShapeIndexBuffer lv = RenderSystem.getSequentialBuffer(parameters.mode());
         if (lv != this.sharedSequentialIndexBuffer || !lv.isLargeEnough(parameters.indexCount())) {
            lv.bindAndGrow(parameters.indexCount());
         }

         return lv;
      }
   }

   public void bind() {
      BufferRenderer.resetCurrentVertexBuffer();
      GlStateManager._glBindVertexArray(this.vertexArrayId);
   }

   public static void unbind() {
      BufferRenderer.resetCurrentVertexBuffer();
      GlStateManager._glBindVertexArray(0);
   }

   public void draw() {
      RenderSystem.drawElements(this.drawMode.glMode, this.indexCount, this.getIndexType().glType);
   }

   private VertexFormat.IndexType getIndexType() {
      RenderSystem.ShapeIndexBuffer lv = this.sharedSequentialIndexBuffer;
      return lv != null ? lv.getIndexType() : this.indexType;
   }

   public void draw(Matrix4f viewMatrix, Matrix4f projectionMatrix, ShaderProgram program) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this.drawInternal(new Matrix4f(viewMatrix), new Matrix4f(projectionMatrix), program);
         });
      } else {
         this.drawInternal(viewMatrix, projectionMatrix, program);
      }

   }

   private void drawInternal(Matrix4f viewMatrix, Matrix4f projectionMatrix, ShaderProgram program) {
      for(int i = 0; i < 12; ++i) {
         int j = RenderSystem.getShaderTexture(i);
         program.addSampler("Sampler" + i, j);
      }

      if (program.modelViewMat != null) {
         program.modelViewMat.set(viewMatrix);
      }

      if (program.projectionMat != null) {
         program.projectionMat.set(projectionMatrix);
      }

      if (program.viewRotationMat != null) {
         program.viewRotationMat.set(RenderSystem.getInverseViewRotationMatrix());
      }

      if (program.colorModulator != null) {
         program.colorModulator.set(RenderSystem.getShaderColor());
      }

      if (program.glintAlpha != null) {
         program.glintAlpha.set(RenderSystem.getShaderGlintAlpha());
      }

      if (program.fogStart != null) {
         program.fogStart.set(RenderSystem.getShaderFogStart());
      }

      if (program.fogEnd != null) {
         program.fogEnd.set(RenderSystem.getShaderFogEnd());
      }

      if (program.fogColor != null) {
         program.fogColor.set(RenderSystem.getShaderFogColor());
      }

      if (program.fogShape != null) {
         program.fogShape.set(RenderSystem.getShaderFogShape().getId());
      }

      if (program.textureMat != null) {
         program.textureMat.set(RenderSystem.getTextureMatrix());
      }

      if (program.gameTime != null) {
         program.gameTime.set(RenderSystem.getShaderGameTime());
      }

      if (program.screenSize != null) {
         Window lv = MinecraftClient.getInstance().getWindow();
         program.screenSize.set((float)lv.getFramebufferWidth(), (float)lv.getFramebufferHeight());
      }

      if (program.lineWidth != null && (this.drawMode == VertexFormat.DrawMode.LINES || this.drawMode == VertexFormat.DrawMode.LINE_STRIP)) {
         program.lineWidth.set(RenderSystem.getShaderLineWidth());
      }

      RenderSystem.setupShaderLights(program);
      program.bind();
      this.draw();
      program.unbind();
   }

   public void close() {
      if (this.vertexBufferId >= 0) {
         RenderSystem.glDeleteBuffers(this.vertexBufferId);
         this.vertexBufferId = -1;
      }

      if (this.indexBufferId >= 0) {
         RenderSystem.glDeleteBuffers(this.indexBufferId);
         this.indexBufferId = -1;
      }

      if (this.vertexArrayId >= 0) {
         RenderSystem.glDeleteVertexArrays(this.vertexArrayId);
         this.vertexArrayId = -1;
      }

   }

   public VertexFormat getVertexFormat() {
      return this.vertexFormat;
   }

   public boolean isClosed() {
      return this.vertexArrayId == -1;
   }

   @Environment(EnvType.CLIENT)
   public static enum class_8555 {
      STATIC(35044),
      DYNAMIC(35048);

      final int field_44795;

      private class_8555(int j) {
         this.field_44795 = j;
      }

      // $FF: synthetic method
      private static class_8555[] method_51735() {
         return new class_8555[]{STATIC, DYNAMIC};
      }
   }
}
