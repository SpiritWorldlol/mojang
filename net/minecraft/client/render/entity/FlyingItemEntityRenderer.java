package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class FlyingItemEntityRenderer extends EntityRenderer {
   private static final float MIN_DISTANCE = 12.25F;
   private final ItemRenderer itemRenderer;
   private final float scale;
   private final boolean lit;

   public FlyingItemEntityRenderer(EntityRendererFactory.Context ctx, float scale, boolean lit) {
      super(ctx);
      this.itemRenderer = ctx.getItemRenderer();
      this.scale = scale;
      this.lit = lit;
   }

   public FlyingItemEntityRenderer(EntityRendererFactory.Context arg) {
      this(arg, 1.0F, false);
   }

   protected int getBlockLight(Entity entity, BlockPos pos) {
      return this.lit ? 15 : super.getBlockLight(entity, pos);
   }

   public void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (entity.age >= 2 || !(this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(entity) < 12.25)) {
         matrices.push();
         matrices.scale(this.scale, this.scale, this.scale);
         matrices.multiply(this.dispatcher.getRotation());
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
         this.itemRenderer.renderItem(((FlyingItemEntity)entity).getStack(), ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId());
         matrices.pop();
         super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
      }
   }

   public Identifier getTexture(Entity entity) {
      return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
   }
}
