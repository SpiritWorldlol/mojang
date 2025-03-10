package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.WitherArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WitherEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class WitherEntityRenderer extends MobEntityRenderer {
   private static final Identifier INVULNERABLE_TEXTURE = new Identifier("textures/entity/wither/wither_invulnerable.png");
   private static final Identifier TEXTURE = new Identifier("textures/entity/wither/wither.png");

   public WitherEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new WitherEntityModel(arg.getPart(EntityModelLayers.WITHER)), 1.0F);
      this.addFeature(new WitherArmorFeatureRenderer(this, arg.getModelLoader()));
   }

   protected int getBlockLight(WitherEntity arg, BlockPos arg2) {
      return 15;
   }

   public Identifier getTexture(WitherEntity arg) {
      int i = arg.getInvulnerableTimer();
      return i > 0 && (i > 80 || i / 5 % 2 != 1) ? INVULNERABLE_TEXTURE : TEXTURE;
   }

   protected void scale(WitherEntity arg, MatrixStack arg2, float f) {
      float g = 2.0F;
      int i = arg.getInvulnerableTimer();
      if (i > 0) {
         g -= ((float)i - f) / 220.0F * 0.5F;
      }

      arg2.scale(g, g, g);
   }
}
