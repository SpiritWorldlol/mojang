package net.minecraft.client.util;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFWVidMode;

@Environment(EnvType.CLIENT)
public final class VideoMode {
   private final int width;
   private final int height;
   private final int redBits;
   private final int greenBits;
   private final int blueBits;
   private final int refreshRate;
   private static final Pattern PATTERN = Pattern.compile("(\\d+)x(\\d+)(?:@(\\d+)(?::(\\d+))?)?");

   public VideoMode(int width, int height, int redBits, int greenBits, int blueBits, int refreshRate) {
      this.width = width;
      this.height = height;
      this.redBits = redBits;
      this.greenBits = greenBits;
      this.blueBits = blueBits;
      this.refreshRate = refreshRate;
   }

   public VideoMode(GLFWVidMode.Buffer buffer) {
      this.width = buffer.width();
      this.height = buffer.height();
      this.redBits = buffer.redBits();
      this.greenBits = buffer.greenBits();
      this.blueBits = buffer.blueBits();
      this.refreshRate = buffer.refreshRate();
   }

   public VideoMode(GLFWVidMode vidMode) {
      this.width = vidMode.width();
      this.height = vidMode.height();
      this.redBits = vidMode.redBits();
      this.greenBits = vidMode.greenBits();
      this.blueBits = vidMode.blueBits();
      this.refreshRate = vidMode.refreshRate();
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int getRedBits() {
      return this.redBits;
   }

   public int getGreenBits() {
      return this.greenBits;
   }

   public int getBlueBits() {
      return this.blueBits;
   }

   public int getRefreshRate() {
      return this.refreshRate;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         VideoMode lv = (VideoMode)o;
         return this.width == lv.width && this.height == lv.height && this.redBits == lv.redBits && this.greenBits == lv.greenBits && this.blueBits == lv.blueBits && this.refreshRate == lv.refreshRate;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.width, this.height, this.redBits, this.greenBits, this.blueBits, this.refreshRate});
   }

   public String toString() {
      return String.format(Locale.ROOT, "%sx%s@%s (%sbit)", this.width, this.height, this.refreshRate, this.redBits + this.greenBits + this.blueBits);
   }

   public static Optional fromString(@Nullable String string) {
      if (string == null) {
         return Optional.empty();
      } else {
         try {
            Matcher matcher = PATTERN.matcher(string);
            if (matcher.matches()) {
               int i = Integer.parseInt(matcher.group(1));
               int j = Integer.parseInt(matcher.group(2));
               String string2 = matcher.group(3);
               int k;
               if (string2 == null) {
                  k = 60;
               } else {
                  k = Integer.parseInt(string2);
               }

               String string3 = matcher.group(4);
               int l;
               if (string3 == null) {
                  l = 24;
               } else {
                  l = Integer.parseInt(string3);
               }

               int m = l / 3;
               return Optional.of(new VideoMode(i, j, m, m, m, k));
            }
         } catch (Exception var9) {
         }

         return Optional.empty();
      }
   }

   public String asString() {
      return String.format(Locale.ROOT, "%sx%s@%s:%s", this.width, this.height, this.refreshRate, this.redBits + this.greenBits + this.blueBits);
   }
}
