package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.CamelEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CamelEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = new Identifier("textures/entity/camel/camel.png");

   public CamelEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer) {
      super(ctx, new CamelEntityModel(ctx.getPart(layer)), 0.7F);
   }

   public Identifier getTexture(CamelEntity arg) {
      return TEXTURE;
   }
}
