package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class IllagerEntityModel extends SinglePartEntityModel implements ModelWithArms, ModelWithHead {
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart hat;
   private final ModelPart arms;
   private final ModelPart leftLeg;
   private final ModelPart rightLeg;
   private final ModelPart rightArm;
   private final ModelPart leftArm;

   public IllagerEntityModel(ModelPart root) {
      this.root = root;
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.hat = this.head.getChild(EntityModelPartNames.HAT);
      this.hat.visible = false;
      this.arms = root.getChild(EntityModelPartNames.ARMS);
      this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
      this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
      this.leftArm = root.getChild(EntityModelPartNames.LEFT_ARM);
      this.rightArm = root.getChild(EntityModelPartNames.RIGHT_ARM);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
      lv3.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 12.0F, 8.0F, new Dilation(0.45F)), ModelTransform.NONE);
      lv3.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(24, 0).cuboid(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F), ModelTransform.pivot(0.0F, -2.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 20).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F).uv(0, 38).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 20.0F, 6.0F, new Dilation(0.5F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
      ModelPartData lv4 = lv2.addChild(EntityModelPartNames.ARMS, ModelPartBuilder.create().uv(44, 22).cuboid(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F).uv(40, 38).cuboid(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F), ModelTransform.of(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));
      lv4.addChild("left_shoulder", ModelPartBuilder.create().uv(44, 22).mirrored().cuboid(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F), ModelTransform.NONE);
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 22).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), ModelTransform.pivot(-2.0F, 12.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 22).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), ModelTransform.pivot(2.0F, 12.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(40, 46).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), ModelTransform.pivot(-5.0F, 2.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(40, 46).mirrored().cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), ModelTransform.pivot(5.0F, 2.0F, 0.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(IllagerEntity arg, float f, float g, float h, float i, float j) {
      this.head.yaw = i * 0.017453292F;
      this.head.pitch = j * 0.017453292F;
      if (this.riding) {
         this.rightArm.pitch = -0.62831855F;
         this.rightArm.yaw = 0.0F;
         this.rightArm.roll = 0.0F;
         this.leftArm.pitch = -0.62831855F;
         this.leftArm.yaw = 0.0F;
         this.leftArm.roll = 0.0F;
         this.rightLeg.pitch = -1.4137167F;
         this.rightLeg.yaw = 0.31415927F;
         this.rightLeg.roll = 0.07853982F;
         this.leftLeg.pitch = -1.4137167F;
         this.leftLeg.yaw = -0.31415927F;
         this.leftLeg.roll = -0.07853982F;
      } else {
         this.rightArm.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 2.0F * g * 0.5F;
         this.rightArm.yaw = 0.0F;
         this.rightArm.roll = 0.0F;
         this.leftArm.pitch = MathHelper.cos(f * 0.6662F) * 2.0F * g * 0.5F;
         this.leftArm.yaw = 0.0F;
         this.leftArm.roll = 0.0F;
         this.rightLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g * 0.5F;
         this.rightLeg.yaw = 0.0F;
         this.rightLeg.roll = 0.0F;
         this.leftLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g * 0.5F;
         this.leftLeg.yaw = 0.0F;
         this.leftLeg.roll = 0.0F;
      }

      IllagerEntity.State lv = arg.getState();
      if (lv == IllagerEntity.State.ATTACKING) {
         if (arg.getMainHandStack().isEmpty()) {
            CrossbowPosing.meleeAttack(this.leftArm, this.rightArm, true, this.handSwingProgress, h);
         } else {
            CrossbowPosing.meleeAttack(this.rightArm, this.leftArm, arg, this.handSwingProgress, h);
         }
      } else if (lv == IllagerEntity.State.SPELLCASTING) {
         this.rightArm.pivotZ = 0.0F;
         this.rightArm.pivotX = -5.0F;
         this.leftArm.pivotZ = 0.0F;
         this.leftArm.pivotX = 5.0F;
         this.rightArm.pitch = MathHelper.cos(h * 0.6662F) * 0.25F;
         this.leftArm.pitch = MathHelper.cos(h * 0.6662F) * 0.25F;
         this.rightArm.roll = 2.3561945F;
         this.leftArm.roll = -2.3561945F;
         this.rightArm.yaw = 0.0F;
         this.leftArm.yaw = 0.0F;
      } else if (lv == IllagerEntity.State.BOW_AND_ARROW) {
         this.rightArm.yaw = -0.1F + this.head.yaw;
         this.rightArm.pitch = -1.5707964F + this.head.pitch;
         this.leftArm.pitch = -0.9424779F + this.head.pitch;
         this.leftArm.yaw = this.head.yaw - 0.4F;
         this.leftArm.roll = 1.5707964F;
      } else if (lv == IllagerEntity.State.CROSSBOW_HOLD) {
         CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, true);
      } else if (lv == IllagerEntity.State.CROSSBOW_CHARGE) {
         CrossbowPosing.charge(this.rightArm, this.leftArm, arg, true);
      } else if (lv == IllagerEntity.State.CELEBRATING) {
         this.rightArm.pivotZ = 0.0F;
         this.rightArm.pivotX = -5.0F;
         this.rightArm.pitch = MathHelper.cos(h * 0.6662F) * 0.05F;
         this.rightArm.roll = 2.670354F;
         this.rightArm.yaw = 0.0F;
         this.leftArm.pivotZ = 0.0F;
         this.leftArm.pivotX = 5.0F;
         this.leftArm.pitch = MathHelper.cos(h * 0.6662F) * 0.05F;
         this.leftArm.roll = -2.3561945F;
         this.leftArm.yaw = 0.0F;
      }

      boolean bl = lv == IllagerEntity.State.CROSSED;
      this.arms.visible = bl;
      this.leftArm.visible = !bl;
      this.rightArm.visible = !bl;
   }

   private ModelPart getAttackingArm(Arm arm) {
      return arm == Arm.LEFT ? this.leftArm : this.rightArm;
   }

   public ModelPart getHat() {
      return this.hat;
   }

   public ModelPart getHead() {
      return this.head;
   }

   public void setArmAngle(Arm arm, MatrixStack matrices) {
      this.getAttackingArm(arm).rotate(matrices);
   }
}
