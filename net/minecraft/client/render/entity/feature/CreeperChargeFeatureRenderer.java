package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CreeperChargeFeatureRenderer extends EnergySwirlOverlayFeatureRenderer {
   private static final Identifier SKIN = new Identifier("textures/entity/creeper/creeper_armor.png");
   private final CreeperEntityModel model;

   public CreeperChargeFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.model = new CreeperEntityModel(loader.getModelPart(EntityModelLayers.CREEPER_ARMOR));
   }

   protected float getEnergySwirlX(float partialAge) {
      return partialAge * 0.01F;
   }

   protected Identifier getEnergySwirlTexture() {
      return SKIN;
   }

   protected EntityModel getEnergySwirlModel() {
      return this.model;
   }
}
