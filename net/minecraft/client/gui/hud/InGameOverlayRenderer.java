package net.minecraft.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class InGameOverlayRenderer {
   private static final Identifier UNDERWATER_TEXTURE = new Identifier("textures/misc/underwater.png");

   public static void renderOverlays(MinecraftClient client, MatrixStack matrices) {
      PlayerEntity lv = client.player;
      if (!lv.noClip) {
         BlockState lv2 = getInWallBlockState(lv);
         if (lv2 != null) {
            renderInWallOverlay(client.getBlockRenderManager().getModels().getModelParticleSprite(lv2), matrices);
         }
      }

      if (!client.player.isSpectator()) {
         if (client.player.isSubmergedIn(FluidTags.WATER)) {
            renderUnderwaterOverlay(client, matrices);
         }

         if (client.player.isOnFire()) {
            renderFireOverlay(client, matrices);
         }
      }

   }

   @Nullable
   private static BlockState getInWallBlockState(PlayerEntity player) {
      BlockPos.Mutable lv = new BlockPos.Mutable();

      for(int i = 0; i < 8; ++i) {
         double d = player.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * player.getWidth() * 0.8F);
         double e = player.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
         double f = player.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * player.getWidth() * 0.8F);
         lv.set(d, e, f);
         BlockState lv2 = player.getWorld().getBlockState(lv);
         if (lv2.getRenderType() != BlockRenderType.INVISIBLE && lv2.shouldBlockVision(player.getWorld(), lv)) {
            return lv2;
         }
      }

      return null;
   }

   private static void renderInWallOverlay(Sprite sprite, MatrixStack matrices) {
      RenderSystem.setShaderTexture(0, sprite.getAtlasId());
      RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
      BufferBuilder lv = Tessellator.getInstance().getBuffer();
      float f = 0.1F;
      float g = -1.0F;
      float h = 1.0F;
      float i = -1.0F;
      float j = 1.0F;
      float k = -0.5F;
      float l = sprite.getMinU();
      float m = sprite.getMaxU();
      float n = sprite.getMinV();
      float o = sprite.getMaxV();
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      lv.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
      lv.vertex(matrix4f, -1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).texture(m, o).next();
      lv.vertex(matrix4f, 1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).texture(l, o).next();
      lv.vertex(matrix4f, 1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).texture(l, n).next();
      lv.vertex(matrix4f, -1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).texture(m, n).next();
      BufferRenderer.drawWithGlobalProgram(lv.end());
   }

   private static void renderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices) {
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      RenderSystem.setShaderTexture(0, UNDERWATER_TEXTURE);
      BufferBuilder lv = Tessellator.getInstance().getBuffer();
      BlockPos lv2 = BlockPos.ofFloored(client.player.getX(), client.player.getEyeY(), client.player.getZ());
      float f = LightmapTextureManager.getBrightness(client.player.getWorld().getDimension(), client.player.getWorld().getLightLevel(lv2));
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(f, f, f, 0.1F);
      float g = 4.0F;
      float h = -1.0F;
      float i = 1.0F;
      float j = -1.0F;
      float k = 1.0F;
      float l = -0.5F;
      float m = -client.player.getYaw() / 64.0F;
      float n = client.player.getPitch() / 64.0F;
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      lv.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      lv.vertex(matrix4f, -1.0F, -1.0F, -0.5F).texture(4.0F + m, 4.0F + n).next();
      lv.vertex(matrix4f, 1.0F, -1.0F, -0.5F).texture(0.0F + m, 4.0F + n).next();
      lv.vertex(matrix4f, 1.0F, 1.0F, -0.5F).texture(0.0F + m, 0.0F + n).next();
      lv.vertex(matrix4f, -1.0F, 1.0F, -0.5F).texture(4.0F + m, 0.0F + n).next();
      BufferRenderer.drawWithGlobalProgram(lv.end());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
   }

   private static void renderFireOverlay(MinecraftClient client, MatrixStack matrices) {
      BufferBuilder lv = Tessellator.getInstance().getBuffer();
      RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
      RenderSystem.depthFunc(519);
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      Sprite lv2 = ModelLoader.FIRE_1.getSprite();
      RenderSystem.setShaderTexture(0, lv2.getAtlasId());
      float f = lv2.getMinU();
      float g = lv2.getMaxU();
      float h = (f + g) / 2.0F;
      float i = lv2.getMinV();
      float j = lv2.getMaxV();
      float k = (i + j) / 2.0F;
      float l = lv2.getAnimationFrameDelta();
      float m = MathHelper.lerp(l, f, h);
      float n = MathHelper.lerp(l, g, h);
      float o = MathHelper.lerp(l, i, k);
      float p = MathHelper.lerp(l, j, k);
      float q = 1.0F;

      for(int r = 0; r < 2; ++r) {
         matrices.push();
         float s = -0.5F;
         float t = 0.5F;
         float u = -0.5F;
         float v = 0.5F;
         float w = -0.5F;
         matrices.translate((float)(-(r * 2 - 1)) * 0.24F, -0.3F, 0.0F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(r * 2 - 1) * 10.0F));
         Matrix4f matrix4f = matrices.peek().getPositionMatrix();
         lv.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
         lv.vertex(matrix4f, -0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).texture(n, p).next();
         lv.vertex(matrix4f, 0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).texture(m, p).next();
         lv.vertex(matrix4f, 0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).texture(m, o).next();
         lv.vertex(matrix4f, -0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).texture(n, o).next();
         BufferRenderer.drawWithGlobalProgram(lv.end());
         matrices.pop();
      }

      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.depthFunc(515);
   }
}
