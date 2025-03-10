package net.minecraft.client.render.block.entity;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class BeaconBlockEntityRenderer implements BlockEntityRenderer {
   public static final Identifier BEAM_TEXTURE = new Identifier("textures/entity/beacon_beam.png");
   public static final int MAX_BEAM_HEIGHT = 1024;

   public BeaconBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
   }

   public void render(BeaconBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
      long l = arg.getWorld().getTime();
      List list = arg.getBeamSegments();
      int k = 0;

      for(int m = 0; m < list.size(); ++m) {
         BeaconBlockEntity.BeamSegment lv = (BeaconBlockEntity.BeamSegment)list.get(m);
         renderBeam(arg2, arg3, f, l, k, m == list.size() - 1 ? 1024 : lv.getHeight(), lv.getColor());
         k += lv.getHeight();
      }

   }

   private static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, long worldTime, int yOffset, int maxY, float[] color) {
      renderBeam(matrices, vertexConsumers, BEAM_TEXTURE, tickDelta, 1.0F, worldTime, yOffset, maxY, color, 0.2F, 0.25F);
   }

   public static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier textureId, float tickDelta, float heightScale, long worldTime, int yOffset, int maxY, float[] color, float innerRadius, float outerRadius) {
      int m = yOffset + maxY;
      matrices.push();
      matrices.translate(0.5, 0.0, 0.5);
      float n = (float)Math.floorMod(worldTime, 40) + tickDelta;
      float o = maxY < 0 ? n : -n;
      float p = MathHelper.fractionalPart(o * 0.2F - (float)MathHelper.floor(o * 0.1F));
      float q = color[0];
      float r = color[1];
      float s = color[2];
      matrices.push();
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n * 2.25F - 45.0F));
      float t = 0.0F;
      float w = 0.0F;
      float x = -innerRadius;
      float y = 0.0F;
      float z = 0.0F;
      float aa = -innerRadius;
      float ab = 0.0F;
      float ac = 1.0F;
      float ad = -1.0F + p;
      float ae = (float)maxY * heightScale * (0.5F / innerRadius) + ad;
      renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, false)), q, r, s, 1.0F, yOffset, m, 0.0F, innerRadius, innerRadius, 0.0F, x, 0.0F, 0.0F, aa, 0.0F, 1.0F, ae, ad);
      matrices.pop();
      t = -outerRadius;
      float u = -outerRadius;
      w = -outerRadius;
      x = -outerRadius;
      ab = 0.0F;
      ac = 1.0F;
      ad = -1.0F + p;
      ae = (float)maxY * heightScale + ad;
      renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, true)), q, r, s, 0.125F, yOffset, m, t, u, outerRadius, w, x, outerRadius, outerRadius, outerRadius, 0.0F, 1.0F, ae, ad);
      matrices.pop();
   }

   private static void renderBeamLayer(MatrixStack matrices, VertexConsumer vertices, float red, float green, float blue, float alpha, int yOffset, int height, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float u1, float u2, float v1, float v2) {
      MatrixStack.Entry lv = matrices.peek();
      Matrix4f matrix4f = lv.getPositionMatrix();
      Matrix3f matrix3f = lv.getNormalMatrix();
      renderBeamFace(matrix4f, matrix3f, vertices, red, green, blue, alpha, yOffset, height, x1, z1, x2, z2, u1, u2, v1, v2);
      renderBeamFace(matrix4f, matrix3f, vertices, red, green, blue, alpha, yOffset, height, x4, z4, x3, z3, u1, u2, v1, v2);
      renderBeamFace(matrix4f, matrix3f, vertices, red, green, blue, alpha, yOffset, height, x2, z2, x4, z4, u1, u2, v1, v2);
      renderBeamFace(matrix4f, matrix3f, vertices, red, green, blue, alpha, yOffset, height, x3, z3, x1, z1, u1, u2, v1, v2);
   }

   private static void renderBeamFace(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertices, float red, float green, float blue, float alpha, int yOffset, int height, float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2) {
      renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, height, x1, z1, u2, v1);
      renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, yOffset, x1, z1, u2, v2);
      renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, yOffset, x2, z2, u1, v2);
      renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, height, x2, z2, u1, v1);
   }

   private static void renderBeamVertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertices, float red, float green, float blue, float alpha, int y, float x, float z, float u, float v) {
      vertices.vertex(positionMatrix, x, (float)y, z).color(red, green, blue, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
   }

   public boolean rendersOutsideBoundingBox(BeaconBlockEntity arg) {
      return true;
   }

   public int getRenderDistance() {
      return 256;
   }

   public boolean isInRenderDistance(BeaconBlockEntity arg, Vec3d arg2) {
      return Vec3d.ofCenter(arg.getPos()).multiply(1.0, 0.0, 1.0).isInRange(arg2.multiply(1.0, 0.0, 1.0), (double)this.getRenderDistance());
   }
}
