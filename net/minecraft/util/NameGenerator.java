package net.minecraft.util;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

public class NameGenerator {
   private static final String[] PREFIX = new String[]{"Slim", "Far", "River", "Silly", "Fat", "Thin", "Fish", "Bat", "Dark", "Oak", "Sly", "Bush", "Zen", "Bark", "Cry", "Slack", "Soup", "Grim", "Hook", "Dirt", "Mud", "Sad", "Hard", "Crook", "Sneak", "Stink", "Weird", "Fire", "Soot", "Soft", "Rough", "Cling", "Scar"};
   private static final String[] SUFFIX = new String[]{"Fox", "Tail", "Jaw", "Whisper", "Twig", "Root", "Finder", "Nose", "Brow", "Blade", "Fry", "Seek", "Wart", "Tooth", "Foot", "Leaf", "Stone", "Fall", "Face", "Tongue", "Voice", "Lip", "Mouth", "Snail", "Toe", "Ear", "Hair", "Beard", "Shirt", "Fist"};

   public static String name(Entity entity) {
      if (entity instanceof PlayerEntity) {
         return entity.getName().getString();
      } else {
         Text lv = entity.getCustomName();
         return lv != null ? lv.getString() : name(entity.getUuid());
      }
   }

   public static String name(UUID uuid) {
      Random lv = randomFromUuid(uuid);
      String var10000 = getRandom(lv, PREFIX);
      return var10000 + getRandom(lv, SUFFIX);
   }

   private static String getRandom(Random random, String[] options) {
      return (String)Util.getRandom((Object[])options, random);
   }

   private static Random randomFromUuid(UUID uuid) {
      return Random.create((long)(uuid.hashCode() >> 2));
   }
}
