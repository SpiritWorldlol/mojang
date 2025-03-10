package net.minecraft.resource;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class VanillaResourcePackProvider implements ResourcePackProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String VANILLA_KEY = "vanilla";
   private final ResourceType type;
   private final DefaultResourcePack resourcePack;
   private final Identifier id;

   public VanillaResourcePackProvider(ResourceType type, DefaultResourcePack resourcePack, Identifier id) {
      this.type = type;
      this.resourcePack = resourcePack;
      this.id = id;
   }

   public void register(Consumer profileAdder) {
      ResourcePackProfile lv = this.createDefault(this.resourcePack);
      if (lv != null) {
         profileAdder.accept(lv);
      }

      this.forEachProfile(profileAdder);
   }

   @Nullable
   protected abstract ResourcePackProfile createDefault(ResourcePack pack);

   protected abstract Text getProfileName(String id);

   public DefaultResourcePack getResourcePack() {
      return this.resourcePack;
   }

   private void forEachProfile(Consumer consumer) {
      Map map = new HashMap();
      Objects.requireNonNull(map);
      this.forEachProfile(map::put);
      map.forEach((fileName, packFactory) -> {
         ResourcePackProfile lv = (ResourcePackProfile)packFactory.apply(fileName);
         if (lv != null) {
            consumer.accept(lv);
         }

      });
   }

   protected void forEachProfile(BiConsumer consumer) {
      this.resourcePack.forEachNamespacedPath(this.type, this.id, (namespacedPath) -> {
         this.forEachProfile(namespacedPath, consumer);
      });
   }

   protected void forEachProfile(@Nullable Path namespacedPath, BiConsumer consumer) {
      if (namespacedPath != null && Files.isDirectory(namespacedPath, new LinkOption[0])) {
         try {
            FileResourcePackProvider.forEachProfile(namespacedPath, true, (profilePath, factory) -> {
               consumer.accept(getFileName(profilePath), (name) -> {
                  return this.create(name, factory, this.getProfileName(name));
               });
            });
         } catch (IOException var4) {
            LOGGER.warn("Failed to discover packs in {}", namespacedPath, var4);
         }
      }

   }

   private static String getFileName(Path path) {
      return StringUtils.removeEnd(path.getFileName().toString(), ".zip");
   }

   @Nullable
   protected abstract ResourcePackProfile create(String name, ResourcePackProfile.PackFactory packFactory, Text displayName);
}
