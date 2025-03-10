package net.minecraft.client.render.model.json;

import java.util.function.IntFunction;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum ModelTransformationMode implements StringIdentifiable {
   NONE(0, "none"),
   THIRD_PERSON_LEFT_HAND(1, "thirdperson_lefthand"),
   THIRD_PERSON_RIGHT_HAND(2, "thirdperson_righthand"),
   FIRST_PERSON_LEFT_HAND(3, "firstperson_lefthand"),
   FIRST_PERSON_RIGHT_HAND(4, "firstperson_righthand"),
   HEAD(5, "head"),
   GUI(6, "gui"),
   GROUND(7, "ground"),
   FIXED(8, "fixed");

   public static final com.mojang.serialization.Codec CODEC = StringIdentifiable.createCodec(ModelTransformationMode::values);
   public static final IntFunction FROM_INDEX = ValueLists.createIdToValueFunction(ModelTransformationMode::getIndex, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.ZERO);
   private final byte index;
   private final String name;

   private ModelTransformationMode(int index, String name) {
      this.name = name;
      this.index = (byte)index;
   }

   public String asString() {
      return this.name;
   }

   public byte getIndex() {
      return this.index;
   }

   public boolean isFirstPerson() {
      return this == FIRST_PERSON_LEFT_HAND || this == FIRST_PERSON_RIGHT_HAND;
   }

   // $FF: synthetic method
   private static ModelTransformationMode[] method_36922() {
      return new ModelTransformationMode[]{NONE, THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, HEAD, GUI, GROUND, FIXED};
   }
}
