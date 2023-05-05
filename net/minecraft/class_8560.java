package net.minecraft;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class class_8560 implements DebugRenderer.Renderer {
   private final MinecraftClient field_44828;
   private double field_44829 = Double.MIN_VALUE;
   private List field_44830 = Collections.emptyList();

   public class_8560(MinecraftClient arg) {
      this.field_44828 = arg;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      double g = (double)Util.getMeasuringTimeNano();
      if (g - this.field_44829 > 1.0E8) {
         this.field_44829 = g;
         Entity lv = this.field_44828.gameRenderer.getCamera().getFocusedEntity();
         this.field_44830 = ImmutableList.copyOf(lv.getWorld().getOtherEntities(lv, lv.getBoundingBox().expand(16.0)));
      }

      PlayerEntity lv2 = this.field_44828.player;
      if (lv2 != null && lv2.field_44784.isPresent()) {
         this.method_51790(matrices, vertexConsumers, cameraX, cameraY, cameraZ, lv2, () -> {
            return 0.0;
         }, 1.0F, 0.0F, 0.0F);
      }

      Iterator var12 = this.field_44830.iterator();

      while(var12.hasNext()) {
         Entity lv3 = (Entity)var12.next();
         if (lv3 != lv2) {
            this.method_51790(matrices, vertexConsumers, cameraX, cameraY, cameraZ, lv3, () -> {
               return this.method_51789(lv3);
            }, 0.0F, 1.0F, 0.0F);
         }
      }

   }

   private void method_51790(MatrixStack arg, VertexConsumerProvider arg2, double d, double e, double f, Entity arg3, DoubleSupplier doubleSupplier, float g, float h, float i) {
      arg3.field_44784.ifPresent((arg4) -> {
         double j = doubleSupplier.getAsDouble();
         BlockPos lv = arg3.getSteppingPos();
         this.method_51791(lv, arg, d, e, f, arg2, 0.02 + j, g, h, i);
         BlockPos lv2 = arg3.getLandingPos();
         if (!lv2.equals(lv)) {
            this.method_51791(lv2, arg, d, e, f, arg2, 0.04 + j, 0.0F, 1.0F, 1.0F);
         }

      });
   }

   private double method_51789(Entity arg) {
      return 0.02 * (double)(String.valueOf((double)arg.getId() + 0.132453657).hashCode() % 1000) / 1000.0;
   }

   private void method_51791(BlockPos arg, MatrixStack arg2, double d, double e, double f, VertexConsumerProvider arg3, double g, float h, float i, float j) {
      double k = (double)arg.getX() - d - 2.0 * g;
      double l = (double)arg.getY() - e - 2.0 * g;
      double m = (double)arg.getZ() - f - 2.0 * g;
      double n = k + 1.0 + 4.0 * g;
      double o = l + 1.0 + 4.0 * g;
      double p = m + 1.0 + 4.0 * g;
      WorldRenderer.drawBox(arg2, arg3.getBuffer(RenderLayer.getLines()), k, l, m, n, o, p, h, i, j, 0.4F);
      WorldRenderer.drawShapeOutline(arg2, arg3.getBuffer(RenderLayer.getLines()), this.field_44828.world.getBlockState(arg).getCollisionShape(this.field_44828.world, arg, ShapeContext.absent()).offset((double)arg.getX(), (double)arg.getY(), (double)arg.getZ()), -d, -e, -f, h, i, j, 1.0F, false);
   }
}
