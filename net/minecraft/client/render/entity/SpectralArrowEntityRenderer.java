package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SpectralArrowEntityRenderer extends ProjectileEntityRenderer {
   public static final Identifier TEXTURE = new Identifier("textures/entity/projectiles/spectral_arrow.png");

   public SpectralArrowEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
   }

   public Identifier getTexture(SpectralArrowEntity arg) {
      return TEXTURE;
   }
}
