package net.minecraft.client.font;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.DependencyTracker;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class FontManager implements ResourceReloader, AutoCloseable {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final String FONTS_JSON = "fonts.json";
   public static final Identifier MISSING_STORAGE_ID = new Identifier("minecraft", "missing");
   private static final ResourceFinder FINDER = ResourceFinder.json("font");
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private final FontStorage missingStorage;
   private final List fonts = new ArrayList();
   private final Map fontStorages = new HashMap();
   private final TextureManager textureManager;
   private Map idOverrides = ImmutableMap.of();

   public FontManager(TextureManager manager) {
      this.textureManager = manager;
      this.missingStorage = (FontStorage)Util.make(new FontStorage(manager, MISSING_STORAGE_ID), (fontStorage) -> {
         fontStorage.setFonts(Lists.newArrayList(new Font[]{new BlankFont()}));
      });
   }

   public CompletableFuture reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      prepareProfiler.startTick();
      prepareProfiler.endTick();
      CompletableFuture var10000 = this.loadIndex(manager, prepareExecutor);
      Objects.requireNonNull(synchronizer);
      return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((index) -> {
         this.reload(index, applyProfiler);
      }, applyExecutor);
   }

   private CompletableFuture loadIndex(ResourceManager resourceManager, Executor executor) {
      List list = new ArrayList();
      Iterator var4 = FINDER.findAllResources(resourceManager).entrySet().iterator();

      while(var4.hasNext()) {
         Map.Entry entry = (Map.Entry)var4.next();
         Identifier lv = FINDER.toResourceId((Identifier)entry.getKey());
         list.add(CompletableFuture.supplyAsync(() -> {
            List list = loadFontProviders((List)entry.getValue(), lv);
            FontEntry lvx = new FontEntry(lv);
            Iterator var7 = list.iterator();

            while(var7.hasNext()) {
               Pair pair = (Pair)var7.next();
               FontKey lv2 = (FontKey)pair.getFirst();
               ((FontLoader)pair.getSecond()).build().ifLeft((loadable) -> {
                  CompletableFuture completableFuture = this.load(lv2, loadable, resourceManager, executor);
                  lvx.addBuilder(lv2, completableFuture);
               }).ifRight((reference) -> {
                  lvx.addReferenceBuilder(lv2, reference);
               });
            }

            return lvx;
         }, executor));
      }

      return Util.combineSafe(list).thenCompose((entries) -> {
         List list2 = (List)entries.stream().flatMap(FontEntry::getImmediateProviders).collect(Collectors.toCollection(ArrayList::new));
         Font lv = new BlankFont();
         list2.add(CompletableFuture.completedFuture(Optional.of(lv)));
         return Util.combineSafe(list2).thenCompose((providers) -> {
            Map map = this.getRequiredFontProviders(entries);
            CompletableFuture[] completableFutures = (CompletableFuture[])map.values().stream().map((dest) -> {
               return CompletableFuture.runAsync(() -> {
                  this.insertFont(dest, lv);
               }, executor);
            }).toArray((i) -> {
               return new CompletableFuture[i];
            });
            return CompletableFuture.allOf(completableFutures).thenApply((ignored) -> {
               List list2 = providers.stream().flatMap(Optional::stream).toList();
               return new ProviderIndex(map, list2);
            });
         });
      });
   }

   private CompletableFuture load(FontKey key, FontLoader.Loadable loadable, ResourceManager resourceManager, Executor executor) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            return Optional.of(loadable.load(resourceManager));
         } catch (Exception var4) {
            LOGGER.warn("Failed to load builder {}, rejecting", key, var4);
            return Optional.empty();
         }
      }, executor);
   }

   private Map getRequiredFontProviders(List entries) {
      Map map = new HashMap();
      DependencyTracker lv = new DependencyTracker();
      entries.forEach((entry) -> {
         lv.add(entry.fontId, entry);
      });
      lv.traverse((dependent, fontEntry) -> {
         Objects.requireNonNull(map);
         fontEntry.getRequiredFontProviders(map::get).ifPresent((fonts) -> {
            map.put(dependent, fonts);
         });
      });
      return map;
   }

   private void insertFont(List fonts, Font font) {
      fonts.add(0, font);
      IntSet intSet = new IntOpenHashSet();
      Iterator var4 = fonts.iterator();

      while(var4.hasNext()) {
         Font lv = (Font)var4.next();
         intSet.addAll(lv.getProvidedGlyphs());
      }

      intSet.forEach((codePoint) -> {
         if (codePoint != 32) {
            Iterator var2 = Lists.reverse(fonts).iterator();

            while(var2.hasNext()) {
               Font lv = (Font)var2.next();
               if (lv.getGlyph(codePoint) != null) {
                  break;
               }
            }

         }
      });
   }

   private void reload(ProviderIndex index, Profiler profiler) {
      profiler.startTick();
      profiler.push("closing");
      this.fontStorages.values().forEach(FontStorage::close);
      this.fontStorages.clear();
      this.fonts.forEach(Font::close);
      this.fonts.clear();
      profiler.swap("reloading");
      index.providers().forEach((fontId, providers) -> {
         FontStorage lv = new FontStorage(this.textureManager, fontId);
         lv.setFonts(Lists.reverse(providers));
         this.fontStorages.put(fontId, lv);
      });
      this.fonts.addAll(index.allProviders);
      profiler.pop();
      profiler.endTick();
      if (!this.fontStorages.containsKey(this.getEffectiveId(MinecraftClient.DEFAULT_FONT_ID))) {
         throw new IllegalStateException("Default font failed to load");
      }
   }

   private static List loadFontProviders(List fontResources, Identifier id) {
      List list2 = new ArrayList();
      Iterator var3 = fontResources.iterator();

      while(var3.hasNext()) {
         Resource lv = (Resource)var3.next();

         try {
            Reader reader = lv.getReader();

            try {
               JsonArray jsonArray = JsonHelper.getArray((JsonObject)JsonHelper.deserialize(GSON, (Reader)reader, (Class)JsonObject.class), "providers");

               for(int i = jsonArray.size() - 1; i >= 0; --i) {
                  JsonObject jsonObject = JsonHelper.asObject(jsonArray.get(i), "providers[" + i + "]");
                  String string = JsonHelper.getString(jsonObject, "type");
                  FontType lv2 = FontType.byId(string);
                  FontKey lv3 = new FontKey(id, lv.getResourcePackName(), i);
                  list2.add(Pair.of(lv3, lv2.createLoader(jsonObject)));
               }
            } catch (Throwable var13) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (reader != null) {
               reader.close();
            }
         } catch (Exception var14) {
            LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", new Object[]{id, "fonts.json", lv.getResourcePackName(), var14});
         }
      }

      return list2;
   }

   public void setIdOverrides(Map idOverrides) {
      this.idOverrides = idOverrides;
   }

   private Identifier getEffectiveId(Identifier id) {
      return (Identifier)this.idOverrides.getOrDefault(id, id);
   }

   public TextRenderer createTextRenderer() {
      return new TextRenderer((id) -> {
         return (FontStorage)this.fontStorages.getOrDefault(this.getEffectiveId(id), this.missingStorage);
      }, false);
   }

   public TextRenderer createAdvanceValidatingTextRenderer() {
      return new TextRenderer((id) -> {
         return (FontStorage)this.fontStorages.getOrDefault(this.getEffectiveId(id), this.missingStorage);
      }, true);
   }

   public void close() {
      this.fontStorages.values().forEach(FontStorage::close);
      this.fonts.forEach(Font::close);
      this.missingStorage.close();
   }

   @Environment(EnvType.CLIENT)
   private static record FontKey(Identifier fontId, String pack, int index) {
      FontKey(Identifier arg, String string, int i) {
         this.fontId = arg;
         this.pack = string;
         this.index = i;
      }

      public String toString() {
         return "(" + this.fontId + ": builder #" + this.index + " from pack " + this.pack + ")";
      }

      public Identifier fontId() {
         return this.fontId;
      }

      public String pack() {
         return this.pack;
      }

      public int index() {
         return this.index;
      }
   }

   @Environment(EnvType.CLIENT)
   private static record ProviderIndex(Map providers, List allProviders) {
      final List allProviders;

      ProviderIndex(Map map, List list) {
         this.providers = map;
         this.allProviders = list;
      }

      public Map providers() {
         return this.providers;
      }

      public List allProviders() {
         return this.allProviders;
      }
   }

   @Environment(EnvType.CLIENT)
   private static record FontEntry(Identifier fontId, List builders, Set dependencies) implements DependencyTracker.Dependencies {
      final Identifier fontId;

      public FontEntry(Identifier fontId) {
         this(fontId, new ArrayList(), new HashSet());
      }

      private FontEntry(Identifier arg, List list, Set set) {
         this.fontId = arg;
         this.builders = list;
         this.dependencies = set;
      }

      public void addReferenceBuilder(FontKey key, FontLoader.Reference reference) {
         this.builders.add(new Builder(key, Either.right(reference.id())));
         this.dependencies.add(reference.id());
      }

      public void addBuilder(FontKey key, CompletableFuture provider) {
         this.builders.add(new Builder(key, Either.left(provider)));
      }

      private Stream getImmediateProviders() {
         return this.builders.stream().flatMap((builder) -> {
            return builder.result.left().stream();
         });
      }

      public Optional getRequiredFontProviders(Function fontRetriever) {
         List list = new ArrayList();
         Iterator var3 = this.builders.iterator();

         while(var3.hasNext()) {
            Builder lv = (Builder)var3.next();
            Optional optional = lv.build(fontRetriever);
            if (!optional.isPresent()) {
               return Optional.empty();
            }

            list.addAll((Collection)optional.get());
         }

         return Optional.of(list);
      }

      public void forDependencies(Consumer callback) {
         this.dependencies.forEach(callback);
      }

      public void forOptionalDependencies(Consumer callback) {
      }

      public Identifier fontId() {
         return this.fontId;
      }

      public List builders() {
         return this.builders;
      }

      public Set dependencies() {
         return this.dependencies;
      }
   }

   @Environment(EnvType.CLIENT)
   static record Builder(FontKey id, Either result) {
      final Either result;

      Builder(FontKey arg, Either either) {
         this.id = arg;
         this.result = either;
      }

      public Optional build(Function fontRetriever) {
         return (Optional)this.result.map((future) -> {
            return ((Optional)future.join()).map(List::of);
         }, (referee) -> {
            List list = (List)fontRetriever.apply(referee);
            if (list == null) {
               FontManager.LOGGER.warn("Can't find font {} referenced by builder {}, either because it's missing, failed to load or is part of loading cycle", referee, this.id);
               return Optional.empty();
            } else {
               return Optional.of(list);
            }
         });
      }

      public FontKey id() {
         return this.id;
      }

      public Either result() {
         return this.result;
      }
   }
}
