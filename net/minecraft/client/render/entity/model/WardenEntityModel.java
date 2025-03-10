package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.animation.WardenAnimations;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class WardenEntityModel extends SinglePartEntityModel {
   private static final float field_38324 = 13.0F;
   private static final float field_38325 = 1.0F;
   private final ModelPart root;
   protected final ModelPart bone;
   protected final ModelPart body;
   protected final ModelPart head;
   protected final ModelPart rightTendril;
   protected final ModelPart leftTendril;
   protected final ModelPart leftLeg;
   protected final ModelPart leftArm;
   protected final ModelPart leftRibcage;
   protected final ModelPart rightArm;
   protected final ModelPart rightLeg;
   protected final ModelPart rightRibcage;
   private final List tendrils;
   private final List justBody;
   private final List headAndLimbs;
   private final List bodyHeadAndLimbs;

   public WardenEntityModel(ModelPart root) {
      super(RenderLayer::getEntityCutoutNoCull);
      this.root = root;
      this.bone = root.getChild(EntityModelPartNames.BONE);
      this.body = this.bone.getChild(EntityModelPartNames.BODY);
      this.head = this.body.getChild(EntityModelPartNames.HEAD);
      this.rightLeg = this.bone.getChild(EntityModelPartNames.RIGHT_LEG);
      this.leftLeg = this.bone.getChild(EntityModelPartNames.LEFT_LEG);
      this.rightArm = this.body.getChild(EntityModelPartNames.RIGHT_ARM);
      this.leftArm = this.body.getChild(EntityModelPartNames.LEFT_ARM);
      this.rightTendril = this.head.getChild(EntityModelPartNames.RIGHT_TENDRIL);
      this.leftTendril = this.head.getChild(EntityModelPartNames.LEFT_TENDRIL);
      this.rightRibcage = this.body.getChild(EntityModelPartNames.RIGHT_RIBCAGE);
      this.leftRibcage = this.body.getChild(EntityModelPartNames.LEFT_RIBCAGE);
      this.tendrils = ImmutableList.of(this.leftTendril, this.rightTendril);
      this.justBody = ImmutableList.of(this.body);
      this.headAndLimbs = ImmutableList.of(this.head, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg);
      this.bodyHeadAndLimbs = ImmutableList.of(this.body, this.head, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BONE, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
      ModelPartData lv4 = lv3.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-9.0F, -13.0F, -4.0F, 18.0F, 21.0F, 11.0F), ModelTransform.pivot(0.0F, -21.0F, 0.0F));
      lv4.addChild(EntityModelPartNames.RIGHT_RIBCAGE, ModelPartBuilder.create().uv(90, 11).cuboid(-2.0F, -11.0F, -0.1F, 9.0F, 21.0F, 0.0F), ModelTransform.pivot(-7.0F, -2.0F, -4.0F));
      lv4.addChild(EntityModelPartNames.LEFT_RIBCAGE, ModelPartBuilder.create().uv(90, 11).mirrored().cuboid(-7.0F, -11.0F, -0.1F, 9.0F, 21.0F, 0.0F).mirrored(false), ModelTransform.pivot(7.0F, -2.0F, -4.0F));
      ModelPartData lv5 = lv4.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 32).cuboid(-8.0F, -16.0F, -5.0F, 16.0F, 16.0F, 10.0F), ModelTransform.pivot(0.0F, -13.0F, 0.0F));
      lv5.addChild(EntityModelPartNames.RIGHT_TENDRIL, ModelPartBuilder.create().uv(52, 32).cuboid(-16.0F, -13.0F, 0.0F, 16.0F, 16.0F, 0.0F), ModelTransform.pivot(-8.0F, -12.0F, 0.0F));
      lv5.addChild(EntityModelPartNames.LEFT_TENDRIL, ModelPartBuilder.create().uv(58, 0).cuboid(0.0F, -13.0F, 0.0F, 16.0F, 16.0F, 0.0F), ModelTransform.pivot(8.0F, -12.0F, 0.0F));
      lv4.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(44, 50).cuboid(-4.0F, 0.0F, -4.0F, 8.0F, 28.0F, 8.0F), ModelTransform.pivot(-13.0F, -13.0F, 1.0F));
      lv4.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(0, 58).cuboid(-4.0F, 0.0F, -4.0F, 8.0F, 28.0F, 8.0F), ModelTransform.pivot(13.0F, -13.0F, 1.0F));
      lv3.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(76, 48).cuboid(-3.1F, 0.0F, -3.0F, 6.0F, 13.0F, 6.0F), ModelTransform.pivot(-5.9F, -13.0F, 0.0F));
      lv3.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(76, 76).cuboid(-2.9F, 0.0F, -3.0F, 6.0F, 13.0F, 6.0F), ModelTransform.pivot(5.9F, -13.0F, 0.0F));
      return TexturedModelData.of(lv, 128, 128);
   }

   public void setAngles(WardenEntity arg, float f, float g, float h, float i, float j) {
      this.getPart().traverse().forEach(ModelPart::resetTransform);
      float k = h - (float)arg.age;
      this.setHeadAngle(i, j);
      this.setLimbAngles(f, g);
      this.setHeadAndBodyAngles(h);
      this.setTendrilPitches(arg, h, k);
      this.updateAnimation(arg.attackingAnimationState, WardenAnimations.ATTACKING, h);
      this.updateAnimation(arg.chargingSonicBoomAnimationState, WardenAnimations.CHARGING_SONIC_BOOM, h);
      this.updateAnimation(arg.diggingAnimationState, WardenAnimations.DIGGING, h);
      this.updateAnimation(arg.emergingAnimationState, WardenAnimations.EMERGING, h);
      this.updateAnimation(arg.roaringAnimationState, WardenAnimations.ROARING, h);
      this.updateAnimation(arg.sniffingAnimationState, WardenAnimations.SNIFFING, h);
   }

   private void setHeadAngle(float yaw, float pitch) {
      this.head.pitch = pitch * 0.017453292F;
      this.head.yaw = yaw * 0.017453292F;
   }

   private void setHeadAndBodyAngles(float animationProgress) {
      float g = animationProgress * 0.1F;
      float h = MathHelper.cos(g);
      float i = MathHelper.sin(g);
      ModelPart var10000 = this.head;
      var10000.roll += 0.06F * h;
      var10000 = this.head;
      var10000.pitch += 0.06F * i;
      var10000 = this.body;
      var10000.roll += 0.025F * i;
      var10000 = this.body;
      var10000.pitch += 0.025F * h;
   }

   private void setLimbAngles(float angle, float distance) {
      float h = Math.min(0.5F, 3.0F * distance);
      float i = angle * 0.8662F;
      float j = MathHelper.cos(i);
      float k = MathHelper.sin(i);
      float l = Math.min(0.35F, h);
      ModelPart var10000 = this.head;
      var10000.roll += 0.3F * k * h;
      var10000 = this.head;
      var10000.pitch += 1.2F * MathHelper.cos(i + 1.5707964F) * l;
      this.body.roll = 0.1F * k * h;
      this.body.pitch = 1.0F * j * l;
      this.leftLeg.pitch = 1.0F * j * h;
      this.rightLeg.pitch = 1.0F * MathHelper.cos(i + 3.1415927F) * h;
      this.leftArm.pitch = -(0.8F * j * h);
      this.leftArm.roll = 0.0F;
      this.rightArm.pitch = -(0.8F * k * h);
      this.rightArm.roll = 0.0F;
      this.setArmPivots();
   }

   private void setArmPivots() {
      this.leftArm.yaw = 0.0F;
      this.leftArm.pivotZ = 1.0F;
      this.leftArm.pivotX = 13.0F;
      this.leftArm.pivotY = -13.0F;
      this.rightArm.yaw = 0.0F;
      this.rightArm.pivotZ = 1.0F;
      this.rightArm.pivotX = -13.0F;
      this.rightArm.pivotY = -13.0F;
   }

   private void setTendrilPitches(WardenEntity warden, float animationProgress, float tickDelta) {
      float h = warden.getTendrilPitch(tickDelta) * (float)(Math.cos((double)animationProgress * 2.25) * Math.PI * 0.10000000149011612);
      this.leftTendril.pitch = h;
      this.rightTendril.pitch = -h;
   }

   public ModelPart getPart() {
      return this.root;
   }

   public List getTendrils() {
      return this.tendrils;
   }

   public List getBody() {
      return this.justBody;
   }

   public List getHeadAndLimbs() {
      return this.headAndLimbs;
   }

   public List getBodyHeadAndLimbs() {
      return this.bodyHeadAndLimbs;
   }
}
