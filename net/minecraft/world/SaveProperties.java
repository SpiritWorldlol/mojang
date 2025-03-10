package net.minecraft.world;

import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Set;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.ServerWorldProperties;
import org.jetbrains.annotations.Nullable;

public interface SaveProperties {
   int ANVIL_FORMAT_ID = 19133;
   int MCREGION_FORMAT_ID = 19132;

   DataConfiguration getDataConfiguration();

   void updateLevelInfo(DataConfiguration dataConfiguration);

   boolean isModded();

   Set getServerBrands();

   Set getRemovedFeatures();

   void addServerBrand(String brand, boolean modded);

   default void populateCrashReport(CrashReportSection section) {
      section.add("Known server brands", () -> {
         return String.join(", ", this.getServerBrands());
      });
      section.add("Removed feature flags", () -> {
         return String.join(", ", this.getRemovedFeatures());
      });
      section.add("Level was modded", () -> {
         return Boolean.toString(this.isModded());
      });
      section.add("Level storage version", () -> {
         int i = this.getVersion();
         return String.format(Locale.ROOT, "0x%05X - %s", i, this.getFormatName(i));
      });
   }

   default String getFormatName(int id) {
      switch (id) {
         case 19132:
            return "McRegion";
         case 19133:
            return "Anvil";
         default:
            return "Unknown?";
      }
   }

   @Nullable
   NbtCompound getCustomBossEvents();

   void setCustomBossEvents(@Nullable NbtCompound customBossEvents);

   ServerWorldProperties getMainWorldProperties();

   LevelInfo getLevelInfo();

   NbtCompound cloneWorldNbt(DynamicRegistryManager registryManager, @Nullable NbtCompound playerNbt);

   boolean isHardcore();

   int getVersion();

   String getLevelName();

   GameMode getGameMode();

   void setGameMode(GameMode gameMode);

   boolean areCommandsAllowed();

   Difficulty getDifficulty();

   void setDifficulty(Difficulty difficulty);

   boolean isDifficultyLocked();

   void setDifficultyLocked(boolean difficultyLocked);

   GameRules getGameRules();

   @Nullable
   NbtCompound getPlayerData();

   NbtCompound getDragonFight();

   void setDragonFight(NbtCompound dragonFight);

   GeneratorOptions getGeneratorOptions();

   boolean isFlatWorld();

   boolean isDebugWorld();

   Lifecycle getLifecycle();

   default FeatureSet getEnabledFeatures() {
      return this.getDataConfiguration().enabledFeatures();
   }
}
