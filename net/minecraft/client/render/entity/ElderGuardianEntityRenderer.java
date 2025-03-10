package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ElderGuardianEntityRenderer extends GuardianEntityRenderer {
   public static final Identifier TEXTURE = new Identifier("textures/entity/guardian_elder.png");

   public ElderGuardianEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, 1.2F, EntityModelLayers.ELDER_GUARDIAN);
   }

   protected void scale(GuardianEntity arg, MatrixStack arg2, float f) {
      arg2.scale(ElderGuardianEntity.SCALE, ElderGuardianEntity.SCALE, ElderGuardianEntity.SCALE);
   }

   public Identifier getTexture(GuardianEntity arg) {
      return TEXTURE;
   }
}
