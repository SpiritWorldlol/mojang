package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class TurtleEntityModel extends QuadrupedEntityModel {
   private static final String EGG_BELLY = "egg_belly";
   private final ModelPart plastron;

   public TurtleEntityModel(ModelPart root) {
      super(root, true, 120.0F, 0.0F, 9.0F, 6.0F, 120);
      this.plastron = root.getChild("egg_belly");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(3, 0).cuboid(-3.0F, -1.0F, -3.0F, 6.0F, 5.0F, 6.0F), ModelTransform.pivot(0.0F, 19.0F, -10.0F));
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(7, 37).cuboid("shell", -9.5F, 3.0F, -10.0F, 19.0F, 20.0F, 6.0F).uv(31, 1).cuboid("belly", -5.5F, 3.0F, -13.0F, 11.0F, 18.0F, 3.0F), ModelTransform.of(0.0F, 11.0F, -10.0F, 1.5707964F, 0.0F, 0.0F));
      lv2.addChild("egg_belly", ModelPartBuilder.create().uv(70, 33).cuboid(-4.5F, 3.0F, -14.0F, 9.0F, 18.0F, 1.0F), ModelTransform.of(0.0F, 11.0F, -10.0F, 1.5707964F, 0.0F, 0.0F));
      int i = true;
      lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(1, 23).cuboid(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F), ModelTransform.pivot(-3.5F, 22.0F, 11.0F));
      lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(1, 12).cuboid(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F), ModelTransform.pivot(3.5F, 22.0F, 11.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(27, 30).cuboid(-13.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F), ModelTransform.pivot(-5.0F, 21.0F, -4.0F));
      lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(27, 24).cuboid(0.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F), ModelTransform.pivot(5.0F, 21.0F, -4.0F));
      return TexturedModelData.of(lv, 128, 64);
   }

   protected Iterable getBodyParts() {
      return Iterables.concat(super.getBodyParts(), ImmutableList.of(this.plastron));
   }

   public void setAngles(TurtleEntity arg, float f, float g, float h, float i, float j) {
      super.setAngles(arg, f, g, h, i, j);
      this.rightHindLeg.pitch = MathHelper.cos(f * 0.6662F * 0.6F) * 0.5F * g;
      this.leftHindLeg.pitch = MathHelper.cos(f * 0.6662F * 0.6F + 3.1415927F) * 0.5F * g;
      this.rightFrontLeg.roll = MathHelper.cos(f * 0.6662F * 0.6F + 3.1415927F) * 0.5F * g;
      this.leftFrontLeg.roll = MathHelper.cos(f * 0.6662F * 0.6F) * 0.5F * g;
      this.rightFrontLeg.pitch = 0.0F;
      this.leftFrontLeg.pitch = 0.0F;
      this.rightFrontLeg.yaw = 0.0F;
      this.leftFrontLeg.yaw = 0.0F;
      this.rightHindLeg.yaw = 0.0F;
      this.leftHindLeg.yaw = 0.0F;
      if (!arg.isTouchingWater() && arg.isOnGround()) {
         float k = arg.isDiggingSand() ? 4.0F : 1.0F;
         float l = arg.isDiggingSand() ? 2.0F : 1.0F;
         float m = 5.0F;
         this.rightFrontLeg.yaw = MathHelper.cos(k * f * 5.0F + 3.1415927F) * 8.0F * g * l;
         this.rightFrontLeg.roll = 0.0F;
         this.leftFrontLeg.yaw = MathHelper.cos(k * f * 5.0F) * 8.0F * g * l;
         this.leftFrontLeg.roll = 0.0F;
         this.rightHindLeg.yaw = MathHelper.cos(f * 5.0F + 3.1415927F) * 3.0F * g;
         this.rightHindLeg.pitch = 0.0F;
         this.leftHindLeg.yaw = MathHelper.cos(f * 5.0F) * 3.0F * g;
         this.leftHindLeg.pitch = 0.0F;
      }

      this.plastron.visible = !this.child && arg.hasEgg();
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      boolean bl = this.plastron.visible;
      if (bl) {
         matrices.push();
         matrices.translate(0.0F, -0.08F, 0.0F);
      }

      super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
      if (bl) {
         matrices.pop();
      }

   }
}
