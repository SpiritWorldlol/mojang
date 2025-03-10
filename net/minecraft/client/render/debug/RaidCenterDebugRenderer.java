package net.minecraft.client.render.debug;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class RaidCenterDebugRenderer implements DebugRenderer.Renderer {
   private static final int RANGE = 160;
   private static final float DRAWN_STRING_SIZE = 0.04F;
   private final MinecraftClient client;
   private Collection raidCenters = Lists.newArrayList();

   public RaidCenterDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void setRaidCenters(Collection raidCenters) {
      this.raidCenters = raidCenters;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      BlockPos lv = this.getCamera().getBlockPos();
      Iterator var10 = this.raidCenters.iterator();

      while(var10.hasNext()) {
         BlockPos lv2 = (BlockPos)var10.next();
         if (lv.isWithinDistance(lv2, 160.0)) {
            drawRaidCenter(matrices, vertexConsumers, lv2);
         }
      }

   }

   private static void drawRaidCenter(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos) {
      DebugRenderer.drawBox(matrices, vertexConsumers, pos.add(-1, -1, -1), pos.add(1, 1, 1), 1.0F, 0.0F, 0.0F, 0.15F);
      int i = -65536;
      drawString(matrices, vertexConsumers, "Raid center", pos, -65536);
   }

   private static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, BlockPos pos, int color) {
      double d = (double)pos.getX() + 0.5;
      double e = (double)pos.getY() + 1.3;
      double f = (double)pos.getZ() + 0.5;
      DebugRenderer.drawString(matrices, vertexConsumers, string, d, e, f, color, 0.04F, true, 0.0F, true);
   }

   private Camera getCamera() {
      return this.client.gameRenderer.getCamera();
   }
}
