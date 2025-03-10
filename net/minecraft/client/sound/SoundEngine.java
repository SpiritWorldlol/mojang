package net.minecraft.client.sound;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.openal.SOFTHRTF;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SoundEngine {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_34945 = 0;
   private static final int field_31897 = 30;
   private long devicePointer;
   private long contextPointer;
   private boolean disconnectExtensionPresent;
   @Nullable
   private String deviceSpecifier;
   private static final SourceSet EMPTY_SOURCE_SET = new SourceSet() {
      @Nullable
      public Source createSource() {
         return null;
      }

      public boolean release(Source source) {
         return false;
      }

      public void close() {
      }

      public int getMaxSourceCount() {
         return 0;
      }

      public int getSourceCount() {
         return 0;
      }
   };
   private SourceSet streamingSources;
   private SourceSet staticSources;
   private final SoundListener listener;

   public SoundEngine() {
      this.streamingSources = EMPTY_SOURCE_SET;
      this.staticSources = EMPTY_SOURCE_SET;
      this.listener = new SoundListener();
      this.deviceSpecifier = findAvailableDeviceSpecifier();
   }

   public void init(@Nullable String deviceSpecifier, boolean directionalAudio) {
      this.devicePointer = openDeviceOrFallback(deviceSpecifier);
      this.disconnectExtensionPresent = ALC10.alcIsExtensionPresent(this.devicePointer, "ALC_EXT_disconnect");
      ALCCapabilities aLCCapabilities = ALC.createCapabilities(this.devicePointer);
      if (AlUtil.checkAlcErrors(this.devicePointer, "Get capabilities")) {
         throw new IllegalStateException("Failed to get OpenAL capabilities");
      } else if (!aLCCapabilities.OpenALC11) {
         throw new IllegalStateException("OpenAL 1.1 not supported");
      } else {
         this.setDirectionalAudio(aLCCapabilities.ALC_SOFT_HRTF && directionalAudio);
         this.contextPointer = ALC10.alcCreateContext(this.devicePointer, (IntBuffer)null);
         ALC10.alcMakeContextCurrent(this.contextPointer);
         int i = this.getMonoSourceCount();
         int j = MathHelper.clamp((int)MathHelper.sqrt((float)i), 2, 8);
         int k = MathHelper.clamp(i - j, 8, 255);
         this.streamingSources = new SourceSetImpl(k);
         this.staticSources = new SourceSetImpl(j);
         ALCapabilities aLCapabilities = AL.createCapabilities(aLCCapabilities);
         AlUtil.checkErrors("Initialization");
         if (!aLCapabilities.AL_EXT_source_distance_model) {
            throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
         } else {
            AL10.alEnable(512);
            if (!aLCapabilities.AL_EXT_LINEAR_DISTANCE) {
               throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
            } else {
               AlUtil.checkErrors("Enable per-source distance models");
               LOGGER.info("OpenAL initialized on device {}", this.getCurrentDeviceName());
            }
         }
      }
   }

   private void setDirectionalAudio(boolean enabled) {
      int i = ALC10.alcGetInteger(this.devicePointer, 6548);
      if (i > 0) {
         MemoryStack memoryStack = MemoryStack.stackPush();

         try {
            IntBuffer intBuffer = memoryStack.callocInt(10).put(6546).put(enabled ? 1 : 0).put(6550).put(0).put(0).flip();
            if (!SOFTHRTF.alcResetDeviceSOFT(this.devicePointer, intBuffer)) {
               LOGGER.warn("Failed to reset device: {}", ALC10.alcGetString(this.devicePointer, ALC10.alcGetError(this.devicePointer)));
            }
         } catch (Throwable var7) {
            if (memoryStack != null) {
               try {
                  memoryStack.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (memoryStack != null) {
            memoryStack.close();
         }
      }

   }

   private int getMonoSourceCount() {
      MemoryStack memoryStack = MemoryStack.stackPush();

      int var7;
      label58: {
         try {
            int i = ALC10.alcGetInteger(this.devicePointer, 4098);
            if (AlUtil.checkAlcErrors(this.devicePointer, "Get attributes size")) {
               throw new IllegalStateException("Failed to get OpenAL attributes");
            }

            IntBuffer intBuffer = memoryStack.mallocInt(i);
            ALC10.alcGetIntegerv(this.devicePointer, 4099, intBuffer);
            if (AlUtil.checkAlcErrors(this.devicePointer, "Get attributes")) {
               throw new IllegalStateException("Failed to get OpenAL attributes");
            }

            int j = 0;

            while(j < i) {
               int k = intBuffer.get(j++);
               if (k == 0) {
                  break;
               }

               int l = intBuffer.get(j++);
               if (k == 4112) {
                  var7 = l;
                  break label58;
               }
            }
         } catch (Throwable var9) {
            if (memoryStack != null) {
               try {
                  memoryStack.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (memoryStack != null) {
            memoryStack.close();
         }

         return 30;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }

      return var7;
   }

   @Nullable
   public static String findAvailableDeviceSpecifier() {
      if (!ALC10.alcIsExtensionPresent(0L, "ALC_ENUMERATE_ALL_EXT")) {
         return null;
      } else {
         ALUtil.getStringList(0L, 4115);
         return ALC10.alcGetString(0L, 4114);
      }
   }

   public String getCurrentDeviceName() {
      String string = ALC10.alcGetString(this.devicePointer, 4115);
      if (string == null) {
         string = ALC10.alcGetString(this.devicePointer, 4101);
      }

      if (string == null) {
         string = "Unknown";
      }

      return string;
   }

   public synchronized boolean updateDeviceSpecifier() {
      String string = findAvailableDeviceSpecifier();
      if (Objects.equals(this.deviceSpecifier, string)) {
         return false;
      } else {
         this.deviceSpecifier = string;
         return true;
      }
   }

   private static long openDeviceOrFallback(@Nullable String deviceSpecifier) {
      OptionalLong optionalLong = OptionalLong.empty();
      if (deviceSpecifier != null) {
         optionalLong = openDevice(deviceSpecifier);
      }

      if (optionalLong.isEmpty()) {
         optionalLong = openDevice(findAvailableDeviceSpecifier());
      }

      if (optionalLong.isEmpty()) {
         optionalLong = openDevice((String)null);
      }

      if (optionalLong.isEmpty()) {
         throw new IllegalStateException("Failed to open OpenAL device");
      } else {
         return optionalLong.getAsLong();
      }
   }

   private static OptionalLong openDevice(@Nullable String deviceSpecifier) {
      long l = ALC10.alcOpenDevice(deviceSpecifier);
      return l != 0L && !AlUtil.checkAlcErrors(l, "Open device") ? OptionalLong.of(l) : OptionalLong.empty();
   }

   public void close() {
      this.streamingSources.close();
      this.staticSources.close();
      ALC10.alcDestroyContext(this.contextPointer);
      if (this.devicePointer != 0L) {
         ALC10.alcCloseDevice(this.devicePointer);
      }

   }

   public SoundListener getListener() {
      return this.listener;
   }

   @Nullable
   public Source createSource(RunMode mode) {
      return (mode == SoundEngine.RunMode.STREAMING ? this.staticSources : this.streamingSources).createSource();
   }

   public void release(Source source) {
      if (!this.streamingSources.release(source) && !this.staticSources.release(source)) {
         throw new IllegalStateException("Tried to release unknown channel");
      }
   }

   public String getDebugString() {
      return String.format(Locale.ROOT, "Sounds: %d/%d + %d/%d", this.streamingSources.getSourceCount(), this.streamingSources.getMaxSourceCount(), this.staticSources.getSourceCount(), this.staticSources.getMaxSourceCount());
   }

   public List getSoundDevices() {
      List list = ALUtil.getStringList(0L, 4115);
      return list == null ? Collections.emptyList() : list;
   }

   public boolean isDeviceUnavailable() {
      return this.disconnectExtensionPresent && ALC11.alcGetInteger(this.devicePointer, 787) == 0;
   }

   @Environment(EnvType.CLIENT)
   interface SourceSet {
      @Nullable
      Source createSource();

      boolean release(Source source);

      void close();

      int getMaxSourceCount();

      int getSourceCount();
   }

   @Environment(EnvType.CLIENT)
   private static class SourceSetImpl implements SourceSet {
      private final int maxSourceCount;
      private final Set sources = Sets.newIdentityHashSet();

      public SourceSetImpl(int maxSourceCount) {
         this.maxSourceCount = maxSourceCount;
      }

      @Nullable
      public Source createSource() {
         if (this.sources.size() >= this.maxSourceCount) {
            if (SharedConstants.isDevelopment) {
               SoundEngine.LOGGER.warn("Maximum sound pool size {} reached", this.maxSourceCount);
            }

            return null;
         } else {
            Source lv = Source.create();
            if (lv != null) {
               this.sources.add(lv);
            }

            return lv;
         }
      }

      public boolean release(Source source) {
         if (!this.sources.remove(source)) {
            return false;
         } else {
            source.close();
            return true;
         }
      }

      public void close() {
         this.sources.forEach(Source::close);
         this.sources.clear();
      }

      public int getMaxSourceCount() {
         return this.maxSourceCount;
      }

      public int getSourceCount() {
         return this.sources.size();
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum RunMode {
      STATIC,
      STREAMING;

      // $FF: synthetic method
      private static RunMode[] method_36800() {
         return new RunMode[]{STATIC, STREAMING};
      }
   }
}
