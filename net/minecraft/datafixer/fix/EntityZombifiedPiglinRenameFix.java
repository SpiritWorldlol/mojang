package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import java.util.Objects;

public class EntityZombifiedPiglinRenameFix extends EntityRenameFix {
   public static final Map RENAMES = ImmutableMap.builder().put("minecraft:zombie_pigman_spawn_egg", "minecraft:zombified_piglin_spawn_egg").build();

   public EntityZombifiedPiglinRenameFix(Schema outputSchema) {
      super("EntityZombifiedPiglinRenameFix", outputSchema, true);
   }

   protected String rename(String oldName) {
      return Objects.equals("minecraft:zombie_pigman", oldName) ? "minecraft:zombified_piglin" : oldName;
   }
}
