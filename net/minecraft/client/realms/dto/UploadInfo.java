package net.minecraft.client.realms.dto;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.util.JsonUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class UploadInfo extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String HTTP_PROTOCOL = "http://";
   private static final int PORT = 8080;
   private static final Pattern PROTOCOL_PATTERN = Pattern.compile("^[a-zA-Z][-a-zA-Z0-9+.]+:");
   private final boolean worldClosed;
   @Nullable
   private final String token;
   private final URI uploadEndpoint;

   private UploadInfo(boolean worldClosed, @Nullable String token, URI uploadEndpoint) {
      this.worldClosed = worldClosed;
      this.token = token;
      this.uploadEndpoint = uploadEndpoint;
   }

   @Nullable
   public static UploadInfo parse(String json) {
      try {
         JsonParser jsonParser = new JsonParser();
         JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
         String string2 = JsonUtils.getStringOr("uploadEndpoint", jsonObject, (String)null);
         if (string2 != null) {
            int i = JsonUtils.getIntOr("port", jsonObject, -1);
            URI uRI = getUrl(string2, i);
            if (uRI != null) {
               boolean bl = JsonUtils.getBooleanOr("worldClosed", jsonObject, false);
               String string3 = JsonUtils.getStringOr("token", jsonObject, (String)null);
               return new UploadInfo(bl, string3, uRI);
            }
         }
      } catch (Exception var8) {
         LOGGER.error("Could not parse UploadInfo: {}", var8.getMessage());
      }

      return null;
   }

   @Nullable
   @VisibleForTesting
   public static URI getUrl(String url, int port) {
      Matcher matcher = PROTOCOL_PATTERN.matcher(url);
      String string2 = getUrlWithProtocol(url, matcher);

      try {
         URI uRI = new URI(string2);
         int j = getPort(port, uRI.getPort());
         return j != uRI.getPort() ? new URI(uRI.getScheme(), uRI.getUserInfo(), uRI.getHost(), j, uRI.getPath(), uRI.getQuery(), uRI.getFragment()) : uRI;
      } catch (URISyntaxException var6) {
         LOGGER.warn("Failed to parse URI {}", string2, var6);
         return null;
      }
   }

   private static int getPort(int port, int urlPort) {
      if (port != -1) {
         return port;
      } else {
         return urlPort != -1 ? urlPort : 8080;
      }
   }

   private static String getUrlWithProtocol(String url, Matcher matcher) {
      return matcher.find() ? url : "http://" + url;
   }

   public static String createRequestContent(@Nullable String token) {
      JsonObject jsonObject = new JsonObject();
      if (token != null) {
         jsonObject.addProperty("token", token);
      }

      return jsonObject.toString();
   }

   @Nullable
   public String getToken() {
      return this.token;
   }

   public URI getUploadEndpoint() {
      return this.uploadEndpoint;
   }

   public boolean isWorldClosed() {
      return this.worldClosed;
   }
}
