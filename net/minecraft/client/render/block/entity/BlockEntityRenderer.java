package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public interface BlockEntityRenderer {
   void render(BlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);

   default boolean rendersOutsideBoundingBox(BlockEntity blockEntity) {
      return false;
   }

   default int getRenderDistance() {
      return 64;
   }

   default boolean isInRenderDistance(BlockEntity blockEntity, Vec3d pos) {
      return Vec3d.ofCenter(blockEntity.getPos()).isInRange(pos, (double)this.getRenderDistance());
   }
}
