package net.minecraft.registry.tag;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resource.DependencyTracker;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TagGroupLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   final Function registryGetter;
   private final String dataType;

   public TagGroupLoader(Function registryGetter, String dataType) {
      this.registryGetter = registryGetter;
      this.dataType = dataType;
   }

   public Map loadTags(ResourceManager resourceManager) {
      Map map = Maps.newHashMap();
      ResourceFinder lv = ResourceFinder.json(this.dataType);
      Iterator var4 = lv.findAllResources(resourceManager).entrySet().iterator();

      while(var4.hasNext()) {
         Map.Entry entry = (Map.Entry)var4.next();
         Identifier lv2 = (Identifier)entry.getKey();
         Identifier lv3 = lv.toResourceId(lv2);
         Iterator var8 = ((List)entry.getValue()).iterator();

         while(var8.hasNext()) {
            Resource lv4 = (Resource)var8.next();

            try {
               Reader reader = lv4.getReader();

               try {
                  JsonElement jsonElement = JsonParser.parseReader(reader);
                  List list = (List)map.computeIfAbsent(lv3, (id) -> {
                     return new ArrayList();
                  });
                  DataResult var10000 = TagFile.CODEC.parse(new Dynamic(JsonOps.INSTANCE, jsonElement));
                  Logger var10002 = LOGGER;
                  Objects.requireNonNull(var10002);
                  TagFile lv5 = (TagFile)var10000.getOrThrow(false, var10002::error);
                  if (lv5.replace()) {
                     list.clear();
                  }

                  String string = lv4.getResourcePackName();
                  lv5.entries().forEach((entryx) -> {
                     list.add(new TrackedEntry(entryx, string));
                  });
               } catch (Throwable var16) {
                  if (reader != null) {
                     try {
                        reader.close();
                     } catch (Throwable var15) {
                        var16.addSuppressed(var15);
                     }
                  }

                  throw var16;
               }

               if (reader != null) {
                  reader.close();
               }
            } catch (Exception var17) {
               LOGGER.error("Couldn't read tag list {} from {} in data pack {}", new Object[]{lv3, lv2, lv4.getResourcePackName(), var17});
            }
         }
      }

      return map;
   }

   private Either resolveAll(TagEntry.ValueGetter valueGetter, List entries) {
      ImmutableSet.Builder builder = ImmutableSet.builder();
      List list2 = new ArrayList();
      Iterator var5 = entries.iterator();

      while(var5.hasNext()) {
         TrackedEntry lv = (TrackedEntry)var5.next();
         TagEntry var10000 = lv.entry();
         Objects.requireNonNull(builder);
         if (!var10000.resolve(valueGetter, builder::add)) {
            list2.add(lv);
         }
      }

      return list2.isEmpty() ? Either.right(builder.build()) : Either.left(list2);
   }

   public Map buildGroup(Map tags) {
      final Map map2 = Maps.newHashMap();
      TagEntry.ValueGetter lv = new TagEntry.ValueGetter() {
         @Nullable
         public Object direct(Identifier id) {
            return ((Optional)TagGroupLoader.this.registryGetter.apply(id)).orElse((Object)null);
         }

         @Nullable
         public Collection tag(Identifier id) {
            return (Collection)map2.get(id);
         }
      };
      DependencyTracker lv2 = new DependencyTracker();
      tags.forEach((id, entries) -> {
         lv2.add(id, new TagDependencies(entries));
      });
      lv2.traverse((id, dependencies) -> {
         this.resolveAll(lv, dependencies.entries).ifLeft((missingReferences) -> {
            LOGGER.error("Couldn't load tag {} as it is missing following references: {}", id, missingReferences.stream().map(Objects::toString).collect(Collectors.joining(", ")));
         }).ifRight((resolvedEntries) -> {
            map2.put(id, resolvedEntries);
         });
      });
      return map2;
   }

   public Map load(ResourceManager manager) {
      return this.buildGroup(this.loadTags(manager));
   }

   public static record TrackedEntry(TagEntry entry, String source) {
      final TagEntry entry;

      public TrackedEntry(TagEntry arg, String source) {
         this.entry = arg;
         this.source = source;
      }

      public String toString() {
         return this.entry + " (from " + this.source + ")";
      }

      public TagEntry entry() {
         return this.entry;
      }

      public String source() {
         return this.source;
      }
   }

   static record TagDependencies(List entries) implements DependencyTracker.Dependencies {
      final List entries;

      TagDependencies(List list) {
         this.entries = list;
      }

      public void forDependencies(Consumer callback) {
         this.entries.forEach((entry) -> {
            entry.entry.forEachRequiredTagId(callback);
         });
      }

      public void forOptionalDependencies(Consumer callback) {
         this.entries.forEach((entry) -> {
            entry.entry.forEachOptionalTagId(callback);
         });
      }

      public List entries() {
         return this.entries;
      }
   }
}
