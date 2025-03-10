package net.minecraft.datafixer.mapping;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class LegacyCoralFanBlockMapping {
   public static final Map MAP = ImmutableMap.builder().put("minecraft:tube_coral_fan", "minecraft:tube_coral_wall_fan").put("minecraft:brain_coral_fan", "minecraft:brain_coral_wall_fan").put("minecraft:bubble_coral_fan", "minecraft:bubble_coral_wall_fan").put("minecraft:fire_coral_fan", "minecraft:fire_coral_wall_fan").put("minecraft:horn_coral_fan", "minecraft:horn_coral_wall_fan").build();
}
