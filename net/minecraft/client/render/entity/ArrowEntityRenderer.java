package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ArrowEntityRenderer extends ProjectileEntityRenderer {
   public static final Identifier TEXTURE = new Identifier("textures/entity/projectiles/arrow.png");
   public static final Identifier TIPPED_TEXTURE = new Identifier("textures/entity/projectiles/tipped_arrow.png");

   public ArrowEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg);
   }

   public Identifier getTexture(ArrowEntity arg) {
      return arg.getColor() > 0 ? TIPPED_TEXTURE : TEXTURE;
   }
}
