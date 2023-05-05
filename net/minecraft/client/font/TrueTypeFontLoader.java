package net.minecraft.client.font;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public record TrueTypeFontLoader(Identifier location, float size, float oversample, Shift shift, String skip) implements FontLoader {
   private static final Codec SKIP_CODEC;
   public static final MapCodec CODEC;

   public TrueTypeFontLoader(Identifier arg, float f, float g, Shift arg2, String string) {
      this.location = arg;
      this.size = f;
      this.oversample = g;
      this.shift = arg2;
      this.skip = string;
   }

   public FontType getType() {
      return FontType.TTF;
   }

   public Either build() {
      return Either.left(this::load);
   }

   private Font load(ResourceManager resourceManager) throws IOException {
      STBTTFontinfo sTBTTFontinfo = null;
      ByteBuffer byteBuffer = null;

      try {
         InputStream inputStream = resourceManager.open(this.location.withPrefixedPath("font/"));

         TrueTypeFont var5;
         try {
            sTBTTFontinfo = STBTTFontinfo.malloc();
            byteBuffer = TextureUtil.readResource(inputStream);
            byteBuffer.flip();
            if (!STBTruetype.stbtt_InitFont(sTBTTFontinfo, byteBuffer)) {
               throw new IOException("Invalid ttf");
            }

            var5 = new TrueTypeFont(byteBuffer, sTBTTFontinfo, this.size, this.oversample, this.shift.x, this.shift.y, this.skip);
         } catch (Throwable var8) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return var5;
      } catch (Exception var9) {
         if (sTBTTFontinfo != null) {
            sTBTTFontinfo.free();
         }

         MemoryUtil.memFree(byteBuffer);
         throw var9;
      }
   }

   public Identifier location() {
      return this.location;
   }

   public float size() {
      return this.size;
   }

   public float oversample() {
      return this.oversample;
   }

   public Shift shift() {
      return this.shift;
   }

   public String skip() {
      return this.skip;
   }

   static {
      SKIP_CODEC = Codec.either(Codec.STRING, Codec.STRING.listOf()).xmap((either) -> {
         return (String)either.map((string) -> {
            return string;
         }, (list) -> {
            return String.join("", list);
         });
      }, Either::left);
      CODEC = RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(Identifier.CODEC.fieldOf("file").forGetter(TrueTypeFontLoader::location), Codec.FLOAT.optionalFieldOf("size", 11.0F).forGetter(TrueTypeFontLoader::size), Codec.FLOAT.optionalFieldOf("oversample", 1.0F).forGetter(TrueTypeFontLoader::oversample), TrueTypeFontLoader.Shift.CODEC.optionalFieldOf("shift", TrueTypeFontLoader.Shift.NONE).forGetter(TrueTypeFontLoader::shift), SKIP_CODEC.optionalFieldOf("skip", "").forGetter(TrueTypeFontLoader::skip)).apply(instance, TrueTypeFontLoader::new);
      });
   }

   @Environment(EnvType.CLIENT)
   public static record Shift(float x, float y) {
      final float x;
      final float y;
      public static final Shift NONE = new Shift(0.0F, 0.0F);
      public static final Codec CODEC;

      public Shift(float f, float g) {
         this.x = f;
         this.y = g;
      }

      public float x() {
         return this.x;
      }

      public float y() {
         return this.y;
      }

      static {
         CODEC = Codec.FLOAT.listOf().comapFlatMap((floatList) -> {
            return Util.decodeFixedLengthList(floatList, 2).map((floatListx) -> {
               return new Shift((Float)floatListx.get(0), (Float)floatListx.get(1));
            });
         }, (shift) -> {
            return List.of(shift.x, shift.y);
         });
      }
   }
}
