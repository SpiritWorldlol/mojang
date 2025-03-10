package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class BannedPlayerEntry extends BanEntry {
   public BannedPlayerEntry(GameProfile profile) {
      this(profile, (Date)null, (String)null, (Date)null, (String)null);
   }

   public BannedPlayerEntry(GameProfile profile, @Nullable Date created, @Nullable String source, @Nullable Date expiry, @Nullable String reason) {
      super(profile, created, source, expiry, reason);
   }

   public BannedPlayerEntry(JsonObject json) {
      super(profileFromJson(json), json);
   }

   protected void write(JsonObject json) {
      if (this.getKey() != null) {
         json.addProperty("uuid", ((GameProfile)this.getKey()).getId() == null ? "" : ((GameProfile)this.getKey()).getId().toString());
         json.addProperty("name", ((GameProfile)this.getKey()).getName());
         super.write(json);
      }
   }

   public Text toText() {
      GameProfile gameProfile = (GameProfile)this.getKey();
      return Text.literal(gameProfile.getName() != null ? gameProfile.getName() : Objects.toString(gameProfile.getId(), "(Unknown)"));
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
