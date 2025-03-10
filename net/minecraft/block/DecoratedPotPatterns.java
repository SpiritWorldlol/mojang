package net.minecraft.block;

import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class DecoratedPotPatterns {
   private static final String DECORATED_POT_BASE = "decorated_pot_base";
   public static final RegistryKey DECORATED_POT_BASE_KEY = of("decorated_pot_base");
   private static final String DECORATED_POT_SIDE = "decorated_pot_side";
   private static final String ANGLER_POTTERY_PATTERN = "angler_pottery_pattern";
   private static final String ARCHER_POTTERY_PATTERN = "archer_pottery_pattern";
   private static final String ARMS_UP_POTTERY_PATTERN = "arms_up_pottery_pattern";
   private static final String BLADE_POTTERY_PATTERN = "blade_pottery_pattern";
   private static final String BREWER_POTTERY_PATTERN = "brewer_pottery_pattern";
   private static final String BURN_POTTERY_PATTERN = "burn_pottery_pattern";
   private static final String DANGER_POTTERY_PATTERN = "danger_pottery_pattern";
   private static final String EXPLORER_POTTERY_PATTERN = "explorer_pottery_pattern";
   private static final String FRIEND_POTTERY_PATTERN = "friend_pottery_pattern";
   private static final String HEART_POTTERY_PATTERN = "heart_pottery_pattern";
   private static final String HEARTBREAK_POTTERY_PATTERN = "heartbreak_pottery_pattern";
   private static final String HOWL_POTTERY_PATTERN = "howl_pottery_pattern";
   private static final String MINER_POTTERY_PATTERN = "miner_pottery_pattern";
   private static final String MOURNER_POTTERY_PATTERN = "mourner_pottery_pattern";
   private static final String PLENTY_POTTERY_PATTERN = "plenty_pottery_pattern";
   private static final String PRIZE_POTTERY_PATTERN = "prize_pottery_pattern";
   private static final String SHEAF_POTTERY_PATTERN = "sheaf_pottery_pattern";
   private static final String SHELTER_POTTERY_PATTERN = "shelter_pottery_pattern";
   private static final String SKULL_POTTERY_PATTERN = "skull_pottery_pattern";
   private static final String SNORT_POTTERY_PATTERN = "snort_pottery_pattern";
   private static final RegistryKey DECORATED_POT_SIDE_KEY = of("decorated_pot_side");
   private static final RegistryKey ANGLER_POTTERY_PATTERN_KEY = of("angler_pottery_pattern");
   private static final RegistryKey ARCHER_POTTERY_PATTERN_KEY = of("archer_pottery_pattern");
   private static final RegistryKey ARMS_UP_POTTERY_PATTERN_KEY = of("arms_up_pottery_pattern");
   private static final RegistryKey BLADE_POTTERY_PATTERN_KEY = of("blade_pottery_pattern");
   private static final RegistryKey BREWER_POTTERY_PATTERN_KEY = of("brewer_pottery_pattern");
   private static final RegistryKey BURN_POTTERY_PATTERN_KEY = of("burn_pottery_pattern");
   private static final RegistryKey DANGER_POTTERY_PATTERN_KEY = of("danger_pottery_pattern");
   private static final RegistryKey EXPLORER_POTTERY_PATTERN_KEY = of("explorer_pottery_pattern");
   private static final RegistryKey FRIEND_POTTERY_PATTERN_KEY = of("friend_pottery_pattern");
   private static final RegistryKey HEART_POTTERY_PATTERN_KEY = of("heart_pottery_pattern");
   private static final RegistryKey HEARTBREAK_POTTERY_PATTERN_KEY = of("heartbreak_pottery_pattern");
   private static final RegistryKey HOWL_POTTERY_PATTERN_KEY = of("howl_pottery_pattern");
   private static final RegistryKey MINER_POTTERY_PATTERN_KEY = of("miner_pottery_pattern");
   private static final RegistryKey MOURNER_POTTERY_PATTERN_KEY = of("mourner_pottery_pattern");
   private static final RegistryKey PLENTY_POTTERY_PATTERN_KEY = of("plenty_pottery_pattern");
   private static final RegistryKey POTTERY_PATTERN_PRIZE_KEY = of("prize_pottery_pattern");
   private static final RegistryKey SHEAF_POTTERY_PATTERN_KEY = of("sheaf_pottery_pattern");
   private static final RegistryKey SHELTER_POTTERY_PATTERN_KEY = of("shelter_pottery_pattern");
   private static final RegistryKey SKULL_POTTERY_PATTERN_KEY = of("skull_pottery_pattern");
   private static final RegistryKey SNORT_POTTERY_PATTERN_KEY = of("snort_pottery_pattern");
   private static final Map SHERD_TO_PATTERN;

   private static RegistryKey of(String path) {
      return RegistryKey.of(RegistryKeys.DECORATED_POT_PATTERN, new Identifier(path));
   }

   public static Identifier getTextureId(RegistryKey key) {
      return key.getValue().withPrefixedPath("entity/decorated_pot/");
   }

   @Nullable
   public static RegistryKey fromSherd(Item sherd) {
      return (RegistryKey)SHERD_TO_PATTERN.get(sherd);
   }

   public static String registerAndGetDefault(Registry registry) {
      Registry.register(registry, (RegistryKey)DECORATED_POT_SIDE_KEY, "decorated_pot_side");
      Registry.register(registry, (RegistryKey)ANGLER_POTTERY_PATTERN_KEY, "angler_pottery_pattern");
      Registry.register(registry, (RegistryKey)ARCHER_POTTERY_PATTERN_KEY, "archer_pottery_pattern");
      Registry.register(registry, (RegistryKey)ARMS_UP_POTTERY_PATTERN_KEY, "arms_up_pottery_pattern");
      Registry.register(registry, (RegistryKey)BLADE_POTTERY_PATTERN_KEY, "blade_pottery_pattern");
      Registry.register(registry, (RegistryKey)BREWER_POTTERY_PATTERN_KEY, "brewer_pottery_pattern");
      Registry.register(registry, (RegistryKey)BURN_POTTERY_PATTERN_KEY, "burn_pottery_pattern");
      Registry.register(registry, (RegistryKey)DANGER_POTTERY_PATTERN_KEY, "danger_pottery_pattern");
      Registry.register(registry, (RegistryKey)EXPLORER_POTTERY_PATTERN_KEY, "explorer_pottery_pattern");
      Registry.register(registry, (RegistryKey)FRIEND_POTTERY_PATTERN_KEY, "friend_pottery_pattern");
      Registry.register(registry, (RegistryKey)HEART_POTTERY_PATTERN_KEY, "heart_pottery_pattern");
      Registry.register(registry, (RegistryKey)HEARTBREAK_POTTERY_PATTERN_KEY, "heartbreak_pottery_pattern");
      Registry.register(registry, (RegistryKey)HOWL_POTTERY_PATTERN_KEY, "howl_pottery_pattern");
      Registry.register(registry, (RegistryKey)MINER_POTTERY_PATTERN_KEY, "miner_pottery_pattern");
      Registry.register(registry, (RegistryKey)MOURNER_POTTERY_PATTERN_KEY, "mourner_pottery_pattern");
      Registry.register(registry, (RegistryKey)PLENTY_POTTERY_PATTERN_KEY, "plenty_pottery_pattern");
      Registry.register(registry, (RegistryKey)POTTERY_PATTERN_PRIZE_KEY, "prize_pottery_pattern");
      Registry.register(registry, (RegistryKey)SHEAF_POTTERY_PATTERN_KEY, "sheaf_pottery_pattern");
      Registry.register(registry, (RegistryKey)SHELTER_POTTERY_PATTERN_KEY, "shelter_pottery_pattern");
      Registry.register(registry, (RegistryKey)SKULL_POTTERY_PATTERN_KEY, "skull_pottery_pattern");
      Registry.register(registry, (RegistryKey)SNORT_POTTERY_PATTERN_KEY, "snort_pottery_pattern");
      return (String)Registry.register(registry, (RegistryKey)DECORATED_POT_BASE_KEY, "decorated_pot_base");
   }

   static {
      SHERD_TO_PATTERN = Map.ofEntries(Map.entry(Items.BRICK, DECORATED_POT_SIDE_KEY), Map.entry(Items.ANGLER_POTTERY_SHERD, ANGLER_POTTERY_PATTERN_KEY), Map.entry(Items.ARCHER_POTTERY_SHERD, ARCHER_POTTERY_PATTERN_KEY), Map.entry(Items.ARMS_UP_POTTERY_SHERD, ARMS_UP_POTTERY_PATTERN_KEY), Map.entry(Items.BLADE_POTTERY_SHERD, BLADE_POTTERY_PATTERN_KEY), Map.entry(Items.BREWER_POTTERY_SHERD, BREWER_POTTERY_PATTERN_KEY), Map.entry(Items.BURN_POTTERY_SHERD, BURN_POTTERY_PATTERN_KEY), Map.entry(Items.DANGER_POTTERY_SHERD, DANGER_POTTERY_PATTERN_KEY), Map.entry(Items.EXPLORER_POTTERY_SHERD, EXPLORER_POTTERY_PATTERN_KEY), Map.entry(Items.FRIEND_POTTERY_SHERD, FRIEND_POTTERY_PATTERN_KEY), Map.entry(Items.HEART_POTTERY_SHERD, HEART_POTTERY_PATTERN_KEY), Map.entry(Items.HEARTBREAK_POTTERY_SHERD, HEARTBREAK_POTTERY_PATTERN_KEY), Map.entry(Items.HOWL_POTTERY_SHERD, HOWL_POTTERY_PATTERN_KEY), Map.entry(Items.MINER_POTTERY_SHERD, MINER_POTTERY_PATTERN_KEY), Map.entry(Items.MOURNER_POTTERY_SHERD, MOURNER_POTTERY_PATTERN_KEY), Map.entry(Items.PLENTY_POTTERY_SHERD, PLENTY_POTTERY_PATTERN_KEY), Map.entry(Items.PRIZE_POTTERY_SHERD, POTTERY_PATTERN_PRIZE_KEY), Map.entry(Items.SHEAF_POTTERY_SHERD, SHEAF_POTTERY_PATTERN_KEY), Map.entry(Items.SHELTER_POTTERY_SHERD, SHELTER_POTTERY_PATTERN_KEY), Map.entry(Items.SKULL_POTTERY_SHERD, SKULL_POTTERY_PATTERN_KEY), Map.entry(Items.SNORT_POTTERY_SHERD, SNORT_POTTERY_PATTERN_KEY));
   }
}
