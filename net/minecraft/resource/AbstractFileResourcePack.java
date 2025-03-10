package net.minecraft.resource;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractFileResourcePack implements ResourcePack {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String name;
   private final boolean alwaysStable;

   protected AbstractFileResourcePack(String name, boolean alwaysStable) {
      this.name = name;
      this.alwaysStable = alwaysStable;
   }

   @Nullable
   public Object parseMetadata(ResourceMetadataReader metaReader) throws IOException {
      InputSupplier lv = this.openRoot(new String[]{"pack.mcmeta"});
      if (lv == null) {
         return null;
      } else {
         InputStream inputStream = (InputStream)lv.get();

         Object var4;
         try {
            var4 = parseMetadata(metaReader, inputStream);
         } catch (Throwable var7) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return var4;
      }
   }

   @Nullable
   public static Object parseMetadata(ResourceMetadataReader metaReader, InputStream inputStream) {
      JsonObject jsonObject;
      try {
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

         try {
            jsonObject = JsonHelper.deserialize((Reader)bufferedReader);
         } catch (Throwable var8) {
            try {
               bufferedReader.close();
            } catch (Throwable var6) {
               var8.addSuppressed(var6);
            }

            throw var8;
         }

         bufferedReader.close();
      } catch (Exception var9) {
         LOGGER.error("Couldn't load {} metadata", metaReader.getKey(), var9);
         return null;
      }

      if (!jsonObject.has(metaReader.getKey())) {
         return null;
      } else {
         try {
            return metaReader.fromJson(JsonHelper.getObject(jsonObject, metaReader.getKey()));
         } catch (Exception var7) {
            LOGGER.error("Couldn't load {} metadata", metaReader.getKey(), var7);
            return null;
         }
      }
   }

   public String getName() {
      return this.name;
   }

   public boolean isAlwaysStable() {
      return this.alwaysStable;
   }
}
