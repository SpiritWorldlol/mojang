package net.minecraft.client.resource.metadata;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.JsonHelper;

@Environment(EnvType.CLIENT)
public class TextureResourceMetadataReader implements ResourceMetadataReader {
   public TextureResourceMetadata fromJson(JsonObject jsonObject) {
      boolean bl = JsonHelper.getBoolean(jsonObject, "blur", false);
      boolean bl2 = JsonHelper.getBoolean(jsonObject, "clamp", false);
      return new TextureResourceMetadata(bl, bl2);
   }

   public String getKey() {
      return "texture";
   }

   // $FF: synthetic method
   public Object fromJson(JsonObject json) {
      return this.fromJson(json);
   }
}
