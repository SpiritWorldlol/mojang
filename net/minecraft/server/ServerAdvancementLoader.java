package net.minecraft.server;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.loot.LootManager;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerAdvancementLoader extends JsonDataLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = (new GsonBuilder()).create();
   private AdvancementManager manager = new AdvancementManager();
   private final LootManager conditionManager;

   public ServerAdvancementLoader(LootManager conditionManager) {
      super(GSON, "advancements");
      this.conditionManager = conditionManager;
   }

   protected void apply(Map map, ResourceManager arg, Profiler arg2) {
      Map map2 = Maps.newHashMap();
      map.forEach((id, json) -> {
         try {
            JsonObject jsonObject = JsonHelper.asObject(json, "advancement");
            Advancement.Builder lv = Advancement.Builder.fromJson(jsonObject, new AdvancementEntityPredicateDeserializer(id, this.conditionManager));
            map2.put(id, lv);
         } catch (Exception var6) {
            LOGGER.error("Parsing error loading custom advancement {}: {}", id, var6.getMessage());
         }

      });
      AdvancementManager lv = new AdvancementManager();
      lv.load(map2);
      Iterator var6 = lv.getRoots().iterator();

      while(var6.hasNext()) {
         Advancement lv2 = (Advancement)var6.next();
         if (lv2.getDisplay() != null) {
            AdvancementPositioner.arrangeForTree(lv2);
         }
      }

      this.manager = lv;
   }

   @Nullable
   public Advancement get(Identifier id) {
      return this.manager.get(id);
   }

   public Collection getAdvancements() {
      return this.manager.getAdvancements();
   }
}
