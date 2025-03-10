package net.minecraft.client.realms.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsServerPlayerLists extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public List servers;

   public static RealmsServerPlayerLists parse(String json) {
      RealmsServerPlayerLists lv = new RealmsServerPlayerLists();
      lv.servers = Lists.newArrayList();

      try {
         JsonParser jsonParser = new JsonParser();
         JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
         if (jsonObject.get("lists").isJsonArray()) {
            JsonArray jsonArray = jsonObject.get("lists").getAsJsonArray();
            Iterator iterator = jsonArray.iterator();

            while(iterator.hasNext()) {
               lv.servers.add(RealmsServerPlayerList.parse(((JsonElement)iterator.next()).getAsJsonObject()));
            }
         }
      } catch (Exception var6) {
         LOGGER.error("Could not parse RealmsServerPlayerLists: {}", var6.getMessage());
      }

      return lv;
   }
}
