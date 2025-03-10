package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.ModelUtil;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PandaEntityModel extends QuadrupedEntityModel {
   private float sittingAnimationProgress;
   private float lieOnBackAnimationProgress;
   private float playAnimationProgress;

   public PandaEntityModel(ModelPart root) {
      super(root, true, 23.0F, 4.8F, 2.7F, 3.0F, 49);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 6).cuboid(-6.5F, -5.0F, -4.0F, 13.0F, 10.0F, 9.0F).uv(45, 16).cuboid(EntityModelPartNames.NOSE, -3.5F, 0.0F, -6.0F, 7.0F, 5.0F, 2.0F).uv(52, 25).cuboid(EntityModelPartNames.LEFT_EAR, 3.5F, -8.0F, -1.0F, 5.0F, 4.0F, 1.0F).uv(52, 25).cuboid(EntityModelPartNames.RIGHT_EAR, -8.5F, -8.0F, -1.0F, 5.0F, 4.0F, 1.0F), ModelTransform.pivot(0.0F, 11.5F, -17.0F));
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 25).cuboid(-9.5F, -13.0F, -6.5F, 19.0F, 26.0F, 13.0F), ModelTransform.of(0.0F, 10.0F, 0.0F, 1.5707964F, 0.0F, 0.0F));
      int i = true;
      int j = true;
      ModelPartBuilder lv3 = ModelPartBuilder.create().uv(40, 0).cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F);
      lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv3, ModelTransform.pivot(-5.5F, 15.0F, 9.0F));
      lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv3, ModelTransform.pivot(5.5F, 15.0F, 9.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv3, ModelTransform.pivot(-5.5F, 15.0F, -9.0F));
      lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv3, ModelTransform.pivot(5.5F, 15.0F, -9.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public void animateModel(PandaEntity arg, float f, float g, float h) {
      super.animateModel(arg, f, g, h);
      this.sittingAnimationProgress = arg.getSittingAnimationProgress(h);
      this.lieOnBackAnimationProgress = arg.getLieOnBackAnimationProgress(h);
      this.playAnimationProgress = arg.isBaby() ? 0.0F : arg.getRollOverAnimationProgress(h);
   }

   public void setAngles(PandaEntity arg, float f, float g, float h, float i, float j) {
      super.setAngles(arg, f, g, h, i, j);
      boolean bl = arg.getAskForBambooTicks() > 0;
      boolean bl2 = arg.isSneezing();
      int k = arg.getSneezeProgress();
      boolean bl3 = arg.isEating();
      boolean bl4 = arg.isScaredByThunderstorm();
      if (bl) {
         this.head.yaw = 0.35F * MathHelper.sin(0.6F * h);
         this.head.roll = 0.35F * MathHelper.sin(0.6F * h);
         this.rightFrontLeg.pitch = -0.75F * MathHelper.sin(0.3F * h);
         this.leftFrontLeg.pitch = 0.75F * MathHelper.sin(0.3F * h);
      } else {
         this.head.roll = 0.0F;
      }

      if (bl2) {
         if (k < 15) {
            this.head.pitch = -0.7853982F * (float)k / 14.0F;
         } else if (k < 20) {
            float l = (float)((k - 15) / 5);
            this.head.pitch = -0.7853982F + 0.7853982F * l;
         }
      }

      if (this.sittingAnimationProgress > 0.0F) {
         this.body.pitch = ModelUtil.interpolateAngle(this.body.pitch, 1.7407963F, this.sittingAnimationProgress);
         this.head.pitch = ModelUtil.interpolateAngle(this.head.pitch, 1.5707964F, this.sittingAnimationProgress);
         this.rightFrontLeg.roll = -0.27079642F;
         this.leftFrontLeg.roll = 0.27079642F;
         this.rightHindLeg.roll = 0.5707964F;
         this.leftHindLeg.roll = -0.5707964F;
         if (bl3) {
            this.head.pitch = 1.5707964F + 0.2F * MathHelper.sin(h * 0.6F);
            this.rightFrontLeg.pitch = -0.4F - 0.2F * MathHelper.sin(h * 0.6F);
            this.leftFrontLeg.pitch = -0.4F - 0.2F * MathHelper.sin(h * 0.6F);
         }

         if (bl4) {
            this.head.pitch = 2.1707964F;
            this.rightFrontLeg.pitch = -0.9F;
            this.leftFrontLeg.pitch = -0.9F;
         }
      } else {
         this.rightHindLeg.roll = 0.0F;
         this.leftHindLeg.roll = 0.0F;
         this.rightFrontLeg.roll = 0.0F;
         this.leftFrontLeg.roll = 0.0F;
      }

      if (this.lieOnBackAnimationProgress > 0.0F) {
         this.rightHindLeg.pitch = -0.6F * MathHelper.sin(h * 0.15F);
         this.leftHindLeg.pitch = 0.6F * MathHelper.sin(h * 0.15F);
         this.rightFrontLeg.pitch = 0.3F * MathHelper.sin(h * 0.25F);
         this.leftFrontLeg.pitch = -0.3F * MathHelper.sin(h * 0.25F);
         this.head.pitch = ModelUtil.interpolateAngle(this.head.pitch, 1.5707964F, this.lieOnBackAnimationProgress);
      }

      if (this.playAnimationProgress > 0.0F) {
         this.head.pitch = ModelUtil.interpolateAngle(this.head.pitch, 2.0561945F, this.playAnimationProgress);
         this.rightHindLeg.pitch = -0.5F * MathHelper.sin(h * 0.5F);
         this.leftHindLeg.pitch = 0.5F * MathHelper.sin(h * 0.5F);
         this.rightFrontLeg.pitch = 0.5F * MathHelper.sin(h * 0.5F);
         this.leftFrontLeg.pitch = -0.5F * MathHelper.sin(h * 0.5F);
      }

   }
}
