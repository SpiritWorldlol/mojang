package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

public class WhitelistEntry extends ServerConfigEntry {
   public WhitelistEntry(GameProfile profile) {
      super(profile);
   }

   public WhitelistEntry(JsonObject json) {
      super(profileFromJson(json));
   }

   protected void write(JsonObject json) {
      if (this.getKey() != null) {
         json.addProperty("uuid", ((GameProfile)this.getKey()).getId() == null ? "" : ((GameProfile)this.getKey()).getId().toString());
         json.addProperty("name", ((GameProfile)this.getKey()).getName());
      }
   }

   private static GameProfile profileFromJson(JsonObject json) {
      if (json.has("uuid") && json.has("name")) {
         String string = json.get("uuid").getAsString();

         UUID uUID;
         try {
            uUID = UUID.fromString(string);
         } catch (Throwable var4) {
            return null;
         }

         return new GameProfile(uUID, json.get("name").getAsString());
      } else {
         return null;
      }
   }
}
