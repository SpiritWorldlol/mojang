package net.minecraft.client.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum Perspective {
   FIRST_PERSON(true, false),
   THIRD_PERSON_BACK(false, false),
   THIRD_PERSON_FRONT(false, true);

   private static final Perspective[] VALUES = values();
   private final boolean firstPerson;
   private final boolean frontView;

   private Perspective(boolean firstPerson, boolean frontView) {
      this.firstPerson = firstPerson;
      this.frontView = frontView;
   }

   public boolean isFirstPerson() {
      return this.firstPerson;
   }

   public boolean isFrontView() {
      return this.frontView;
   }

   public Perspective next() {
      return VALUES[(this.ordinal() + 1) % VALUES.length];
   }

   // $FF: synthetic method
   private static Perspective[] method_36859() {
      return new Perspective[]{FIRST_PERSON, THIRD_PERSON_BACK, THIRD_PERSON_FRONT};
   }
}
