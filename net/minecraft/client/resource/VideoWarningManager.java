package net.minecraft.client.resource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class VideoWarningManager extends SinglePreparationResourceReloader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Identifier GPU_WARNLIST_ID = new Identifier("gpu_warnlist.json");
   private ImmutableMap warnings = ImmutableMap.of();
   private boolean warningScheduled;
   private boolean warned;
   private boolean cancelledAfterWarning;

   public boolean hasWarning() {
      return !this.warnings.isEmpty();
   }

   public boolean canWarn() {
      return this.hasWarning() && !this.warned;
   }

   public void scheduleWarning() {
      this.warningScheduled = true;
   }

   public void acceptAfterWarnings() {
      this.warned = true;
   }

   public void cancelAfterWarnings() {
      this.warned = true;
      this.cancelledAfterWarning = true;
   }

   public boolean shouldWarn() {
      return this.warningScheduled && !this.warned;
   }

   public boolean hasCancelledAfterWarning() {
      return this.cancelledAfterWarning;
   }

   public void reset() {
      this.warningScheduled = false;
      this.warned = false;
      this.cancelledAfterWarning = false;
   }

   @Nullable
   public String getRendererWarning() {
      return (String)this.warnings.get("renderer");
   }

   @Nullable
   public String getVersionWarning() {
      return (String)this.warnings.get("version");
   }

   @Nullable
   public String getVendorWarning() {
      return (String)this.warnings.get("vendor");
   }

   @Nullable
   public String getWarningsAsString() {
      StringBuilder stringBuilder = new StringBuilder();
      this.warnings.forEach((key, value) -> {
         stringBuilder.append(key).append(": ").append(value);
      });
      return stringBuilder.length() == 0 ? null : stringBuilder.toString();
   }

   protected WarningPatternLoader prepare(ResourceManager arg, Profiler arg2) {
      List list = Lists.newArrayList();
      List list2 = Lists.newArrayList();
      List list3 = Lists.newArrayList();
      arg2.startTick();
      JsonObject jsonObject = loadWarnlist(arg, arg2);
      if (jsonObject != null) {
         arg2.push("compile_regex");
         compilePatterns(jsonObject.getAsJsonArray("renderer"), list);
         compilePatterns(jsonObject.getAsJsonArray("version"), list2);
         compilePatterns(jsonObject.getAsJsonArray("vendor"), list3);
         arg2.pop();
      }

      arg2.endTick();
      return new WarningPatternLoader(list, list2, list3);
   }

   protected void apply(WarningPatternLoader arg, ResourceManager arg2, Profiler arg3) {
      this.warnings = arg.buildWarnings();
   }

   private static void compilePatterns(JsonArray array, List patterns) {
      array.forEach((json) -> {
         patterns.add(Pattern.compile(json.getAsString(), 2));
      });
   }

   @Nullable
   private static JsonObject loadWarnlist(ResourceManager resourceManager, Profiler profiler) {
      profiler.push("parse_json");
      JsonObject jsonObject = null;

      try {
         Reader reader = resourceManager.openAsReader(GPU_WARNLIST_ID);

         try {
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
         } catch (Throwable var7) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (reader != null) {
            reader.close();
         }
      } catch (JsonSyntaxException | IOException var8) {
         LOGGER.warn("Failed to load GPU warnlist");
      }

      profiler.pop();
      return jsonObject;
   }

   // $FF: synthetic method
   protected Object prepare(ResourceManager manager, Profiler profiler) {
      return this.prepare(manager, profiler);
   }

   @Environment(EnvType.CLIENT)
   protected static final class WarningPatternLoader {
      private final List rendererPatterns;
      private final List versionPatterns;
      private final List vendorPatterns;

      WarningPatternLoader(List rendererPatterns, List versionPatterns, List vendorPatterns) {
         this.rendererPatterns = rendererPatterns;
         this.versionPatterns = versionPatterns;
         this.vendorPatterns = vendorPatterns;
      }

      private static String buildWarning(List warningPattern, String info) {
         List list2 = Lists.newArrayList();
         Iterator var3 = warningPattern.iterator();

         while(var3.hasNext()) {
            Pattern pattern = (Pattern)var3.next();
            Matcher matcher = pattern.matcher(info);

            while(matcher.find()) {
               list2.add(matcher.group());
            }
         }

         return String.join(", ", list2);
      }

      ImmutableMap buildWarnings() {
         ImmutableMap.Builder builder = new ImmutableMap.Builder();
         String string = buildWarning(this.rendererPatterns, GlDebugInfo.getRenderer());
         if (!string.isEmpty()) {
            builder.put("renderer", string);
         }

         String string2 = buildWarning(this.versionPatterns, GlDebugInfo.getVersion());
         if (!string2.isEmpty()) {
            builder.put("version", string2);
         }

         String string3 = buildWarning(this.vendorPatterns, GlDebugInfo.getVendor());
         if (!string3.isEmpty()) {
            builder.put("vendor", string3);
         }

         return builder.build();
      }
   }
}
