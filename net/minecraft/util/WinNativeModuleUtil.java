package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.util.crash.CrashReportSection;
import org.slf4j.Logger;

public class WinNativeModuleUtil {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int CODE_PAGE_MASK = 65535;
   private static final int EN_US_CODE_PAGE = 1033;
   private static final int LANGUAGE_ID_MASK = -65536;
   private static final int LANGUAGE_ID = 78643200;

   public static List collectNativeModules() {
      if (!Platform.isWindows()) {
         return ImmutableList.of();
      } else {
         int i = Kernel32.INSTANCE.GetCurrentProcessId();
         ImmutableList.Builder builder = ImmutableList.builder();
         List list = Kernel32Util.getModules(i);
         Iterator var3 = list.iterator();

         while(var3.hasNext()) {
            Tlhelp32.MODULEENTRY32W mODULEENTRY32W = (Tlhelp32.MODULEENTRY32W)var3.next();
            String string = mODULEENTRY32W.szModule();
            Optional optional = createNativeModuleInfo(mODULEENTRY32W.szExePath());
            builder.add(new NativeModule(string, optional));
         }

         return builder.build();
      }
   }

   private static Optional createNativeModuleInfo(String path) {
      try {
         IntByReference intByReference = new IntByReference();
         int i = Version.INSTANCE.GetFileVersionInfoSize(path, intByReference);
         if (i == 0) {
            int j = Native.getLastError();
            if (j != 1813 && j != 1812) {
               throw new Win32Exception(j);
            } else {
               return Optional.empty();
            }
         } else {
            Pointer pointer = new Memory((long)i);
            if (!Version.INSTANCE.GetFileVersionInfo(path, 0, i, pointer)) {
               throw new Win32Exception(Native.getLastError());
            } else {
               IntByReference intByReference2 = new IntByReference();
               Pointer pointer2 = query(pointer, "\\VarFileInfo\\Translation", intByReference2);
               int[] is = pointer2.getIntArray(0L, intByReference2.getValue() / 4);
               OptionalInt optionalInt = getEnglishTranslationIndex(is);
               if (!optionalInt.isPresent()) {
                  return Optional.empty();
               } else {
                  int k = optionalInt.getAsInt();
                  int l = k & '\uffff';
                  int m = (k & -65536) >> 16;
                  String string2 = queryString(pointer, getStringFileInfoPath("FileDescription", l, m), intByReference2);
                  String string3 = queryString(pointer, getStringFileInfoPath("CompanyName", l, m), intByReference2);
                  String string4 = queryString(pointer, getStringFileInfoPath("FileVersion", l, m), intByReference2);
                  return Optional.of(new NativeModuleInfo(string2, string4, string3));
               }
            }
         }
      } catch (Exception var14) {
         LOGGER.info("Failed to find module info for {}", path, var14);
         return Optional.empty();
      }
   }

   private static String getStringFileInfoPath(String key, int languageId, int codePage) {
      return String.format(Locale.ROOT, "\\StringFileInfo\\%04x%04x\\%s", languageId, codePage, key);
   }

   private static OptionalInt getEnglishTranslationIndex(int[] indices) {
      OptionalInt optionalInt = OptionalInt.empty();
      int[] var2 = indices;
      int var3 = indices.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int i = var2[var4];
         if ((i & -65536) == 78643200 && (i & '\uffff') == 1033) {
            return OptionalInt.of(i);
         }

         optionalInt = OptionalInt.of(i);
      }

      return optionalInt;
   }

   private static Pointer query(Pointer pointer, String path, IntByReference lengthPointer) {
      PointerByReference pointerByReference = new PointerByReference();
      if (!Version.INSTANCE.VerQueryValue(pointer, path, pointerByReference, lengthPointer)) {
         throw new UnsupportedOperationException("Can't get version value " + path);
      } else {
         return pointerByReference.getValue();
      }
   }

   private static String queryString(Pointer pointer, String path, IntByReference lengthPointer) {
      try {
         Pointer pointer2 = query(pointer, path, lengthPointer);
         byte[] bs = pointer2.getByteArray(0L, (lengthPointer.getValue() - 1) * 2);
         return new String(bs, StandardCharsets.UTF_16LE);
      } catch (Exception var5) {
         return "";
      }
   }

   public static void addDetailTo(CrashReportSection section) {
      section.add("Modules", () -> {
         return (String)collectNativeModules().stream().sorted(Comparator.comparing((module) -> {
            return module.path;
         })).map((moduleName) -> {
            return "\n\t\t" + moduleName;
         }).collect(Collectors.joining());
      });
   }

   public static class NativeModule {
      public final String path;
      public final Optional info;

      public NativeModule(String path, Optional info) {
         this.path = path;
         this.info = info;
      }

      public String toString() {
         return (String)this.info.map((info) -> {
            return this.path + ":" + info;
         }).orElse(this.path);
      }
   }

   public static class NativeModuleInfo {
      public final String fileDescription;
      public final String fileVersion;
      public final String companyName;

      public NativeModuleInfo(String fileDescription, String fileVersion, String companyName) {
         this.fileDescription = fileDescription;
         this.fileVersion = fileVersion;
         this.companyName = companyName;
      }

      public String toString() {
         return this.fileDescription + ":" + this.fileVersion + ":" + this.companyName;
      }
   }
}
