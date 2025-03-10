package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SkeletonEntityRenderer extends BipedEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/skeleton/skeleton.png");

   public SkeletonEntityRenderer(EntityRendererFactory.Context arg) {
      this(arg, EntityModelLayers.SKELETON, EntityModelLayers.SKELETON_INNER_ARMOR, EntityModelLayers.SKELETON_OUTER_ARMOR);
   }

   public SkeletonEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, EntityModelLayer legArmorLayer, EntityModelLayer bodyArmorLayer) {
      super(ctx, new SkeletonEntityModel(ctx.getPart(layer)), 0.5F);
      this.addFeature(new ArmorFeatureRenderer(this, new SkeletonEntityModel(ctx.getPart(legArmorLayer)), new SkeletonEntityModel(ctx.getPart(bodyArmorLayer)), ctx.getModelManager()));
   }

   public Identifier getTexture(AbstractSkeletonEntity arg) {
      return TEXTURE;
   }

   protected boolean isShaking(AbstractSkeletonEntity arg) {
      return arg.isShaking();
   }

   // $FF: synthetic method
   protected boolean isShaking(LivingEntity entity) {
      return this.isShaking((AbstractSkeletonEntity)entity);
   }
}
