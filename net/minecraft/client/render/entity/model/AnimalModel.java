package net.minecraft.client.render.entity.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public abstract class AnimalModel extends EntityModel {
   private final boolean headScaled;
   private final float childHeadYOffset;
   private final float childHeadZOffset;
   private final float invertedChildHeadScale;
   private final float invertedChildBodyScale;
   private final float childBodyYOffset;

   protected AnimalModel(boolean headScaled, float childHeadYOffset, float childHeadZOffset) {
      this(headScaled, childHeadYOffset, childHeadZOffset, 2.0F, 2.0F, 24.0F);
   }

   protected AnimalModel(boolean headScaled, float childHeadYOffset, float childHeadZOffset, float invertedChildHeadScale, float invertedChildBodyScale, float childBodyYOffset) {
      this(RenderLayer::getEntityCutoutNoCull, headScaled, childHeadYOffset, childHeadZOffset, invertedChildHeadScale, invertedChildBodyScale, childBodyYOffset);
   }

   protected AnimalModel(Function renderLayerFactory, boolean headScaled, float childHeadYOffset, float childHeadZOffset, float invertedChildHeadScale, float invertedChildBodyScale, float childBodyYOffset) {
      super(renderLayerFactory);
      this.headScaled = headScaled;
      this.childHeadYOffset = childHeadYOffset;
      this.childHeadZOffset = childHeadZOffset;
      this.invertedChildHeadScale = invertedChildHeadScale;
      this.invertedChildBodyScale = invertedChildBodyScale;
      this.childBodyYOffset = childBodyYOffset;
   }

   protected AnimalModel() {
      this(false, 5.0F, 2.0F);
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      if (this.child) {
         matrices.push();
         float l;
         if (this.headScaled) {
            l = 1.5F / this.invertedChildHeadScale;
            matrices.scale(l, l, l);
         }

         matrices.translate(0.0F, this.childHeadYOffset / 16.0F, this.childHeadZOffset / 16.0F);
         this.getHeadParts().forEach((headPart) -> {
            headPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         });
         matrices.pop();
         matrices.push();
         l = 1.0F / this.invertedChildBodyScale;
         matrices.scale(l, l, l);
         matrices.translate(0.0F, this.childBodyYOffset / 16.0F, 0.0F);
         this.getBodyParts().forEach((bodyPart) -> {
            bodyPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         });
         matrices.pop();
      } else {
         this.getHeadParts().forEach((headPart) -> {
            headPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         });
         this.getBodyParts().forEach((bodyPart) -> {
            bodyPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         });
      }

   }

   protected abstract Iterable getHeadParts();

   protected abstract Iterable getBodyParts();
}
