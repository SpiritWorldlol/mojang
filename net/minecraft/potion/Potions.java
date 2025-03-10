package net.minecraft.potion;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class Potions {
   public static RegistryKey EMPTY_KEY;
   public static final Potion EMPTY;
   public static final Potion WATER;
   public static final Potion MUNDANE;
   public static final Potion THICK;
   public static final Potion AWKWARD;
   public static final Potion NIGHT_VISION;
   public static final Potion LONG_NIGHT_VISION;
   public static final Potion INVISIBILITY;
   public static final Potion LONG_INVISIBILITY;
   public static final Potion LEAPING;
   public static final Potion LONG_LEAPING;
   public static final Potion STRONG_LEAPING;
   public static final Potion FIRE_RESISTANCE;
   public static final Potion LONG_FIRE_RESISTANCE;
   public static final Potion SWIFTNESS;
   public static final Potion LONG_SWIFTNESS;
   public static final Potion STRONG_SWIFTNESS;
   public static final Potion SLOWNESS;
   public static final Potion LONG_SLOWNESS;
   public static final Potion STRONG_SLOWNESS;
   public static final Potion TURTLE_MASTER;
   public static final Potion LONG_TURTLE_MASTER;
   public static final Potion STRONG_TURTLE_MASTER;
   public static final Potion WATER_BREATHING;
   public static final Potion LONG_WATER_BREATHING;
   public static final Potion HEALING;
   public static final Potion STRONG_HEALING;
   public static final Potion HARMING;
   public static final Potion STRONG_HARMING;
   public static final Potion POISON;
   public static final Potion LONG_POISON;
   public static final Potion STRONG_POISON;
   public static final Potion REGENERATION;
   public static final Potion LONG_REGENERATION;
   public static final Potion STRONG_REGENERATION;
   public static final Potion STRENGTH;
   public static final Potion LONG_STRENGTH;
   public static final Potion STRONG_STRENGTH;
   public static final Potion WEAKNESS;
   public static final Potion LONG_WEAKNESS;
   public static final Potion LUCK;
   public static final Potion SLOW_FALLING;
   public static final Potion LONG_SLOW_FALLING;

   private static Potion register(String name, Potion potion) {
      return (Potion)Registry.register(Registries.POTION, (String)name, potion);
   }

   private static Potion register(RegistryKey key, Potion potion) {
      return (Potion)Registry.register(Registries.POTION, (RegistryKey)key, potion);
   }

   static {
      EMPTY_KEY = RegistryKey.of(RegistryKeys.POTION, new Identifier("empty"));
      EMPTY = register(EMPTY_KEY, new Potion(new StatusEffectInstance[0]));
      WATER = register("water", new Potion(new StatusEffectInstance[0]));
      MUNDANE = register("mundane", new Potion(new StatusEffectInstance[0]));
      THICK = register("thick", new Potion(new StatusEffectInstance[0]));
      AWKWARD = register("awkward", new Potion(new StatusEffectInstance[0]));
      NIGHT_VISION = register("night_vision", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.NIGHT_VISION, 3600)}));
      LONG_NIGHT_VISION = register("long_night_vision", new Potion("night_vision", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.NIGHT_VISION, 9600)}));
      INVISIBILITY = register("invisibility", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.INVISIBILITY, 3600)}));
      LONG_INVISIBILITY = register("long_invisibility", new Potion("invisibility", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.INVISIBILITY, 9600)}));
      LEAPING = register("leaping", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.JUMP_BOOST, 3600)}));
      LONG_LEAPING = register("long_leaping", new Potion("leaping", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.JUMP_BOOST, 9600)}));
      STRONG_LEAPING = register("strong_leaping", new Potion("leaping", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1800, 1)}));
      FIRE_RESISTANCE = register("fire_resistance", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 3600)}));
      LONG_FIRE_RESISTANCE = register("long_fire_resistance", new Potion("fire_resistance", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 9600)}));
      SWIFTNESS = register("swiftness", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SPEED, 3600)}));
      LONG_SWIFTNESS = register("long_swiftness", new Potion("swiftness", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SPEED, 9600)}));
      STRONG_SWIFTNESS = register("strong_swiftness", new Potion("swiftness", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SPEED, 1800, 1)}));
      SLOWNESS = register("slowness", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOWNESS, 1800)}));
      LONG_SLOWNESS = register("long_slowness", new Potion("slowness", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOWNESS, 4800)}));
      STRONG_SLOWNESS = register("strong_slowness", new Potion("slowness", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 3)}));
      TURTLE_MASTER = register("turtle_master", new Potion("turtle_master", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 3), new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 2)}));
      LONG_TURTLE_MASTER = register("long_turtle_master", new Potion("turtle_master", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOWNESS, 800, 3), new StatusEffectInstance(StatusEffects.RESISTANCE, 800, 2)}));
      STRONG_TURTLE_MASTER = register("strong_turtle_master", new Potion("turtle_master", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 5), new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 3)}));
      WATER_BREATHING = register("water_breathing", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.WATER_BREATHING, 3600)}));
      LONG_WATER_BREATHING = register("long_water_breathing", new Potion("water_breathing", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.WATER_BREATHING, 9600)}));
      HEALING = register("healing", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1)}));
      STRONG_HEALING = register("strong_healing", new Potion("healing", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 1)}));
      HARMING = register("harming", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1)}));
      STRONG_HARMING = register("strong_harming", new Potion("harming", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1)}));
      POISON = register("poison", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.POISON, 900)}));
      LONG_POISON = register("long_poison", new Potion("poison", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.POISON, 1800)}));
      STRONG_POISON = register("strong_poison", new Potion("poison", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.POISON, 432, 1)}));
      REGENERATION = register("regeneration", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.REGENERATION, 900)}));
      LONG_REGENERATION = register("long_regeneration", new Potion("regeneration", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.REGENERATION, 1800)}));
      STRONG_REGENERATION = register("strong_regeneration", new Potion("regeneration", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.REGENERATION, 450, 1)}));
      STRENGTH = register("strength", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.STRENGTH, 3600)}));
      LONG_STRENGTH = register("long_strength", new Potion("strength", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.STRENGTH, 9600)}));
      STRONG_STRENGTH = register("strong_strength", new Potion("strength", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.STRENGTH, 1800, 1)}));
      WEAKNESS = register("weakness", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.WEAKNESS, 1800)}));
      LONG_WEAKNESS = register("long_weakness", new Potion("weakness", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.WEAKNESS, 4800)}));
      LUCK = register("luck", new Potion("luck", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.LUCK, 6000)}));
      SLOW_FALLING = register("slow_falling", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOW_FALLING, 1800)}));
      LONG_SLOW_FALLING = register("long_slow_falling", new Potion("slow_falling", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOW_FALLING, 4800)}));
   }
}
