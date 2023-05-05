package net.minecraft.client.font;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BitmapFont implements Font {
   static final Logger LOGGER = LogUtils.getLogger();
   private final NativeImage image;
   private final GlyphContainer glyphs;

   BitmapFont(NativeImage image, GlyphContainer glyphs) {
      this.image = image;
      this.glyphs = glyphs;
   }

   public void close() {
      this.image.close();
   }

   @Nullable
   public Glyph getGlyph(int codePoint) {
      return (Glyph)this.glyphs.get(codePoint);
   }

   public IntSet getProvidedGlyphs() {
      return IntSets.unmodifiable(this.glyphs.getProvidedGlyphs());
   }

   @Environment(EnvType.CLIENT)
   private static record BitmapFontGlyph(float scaleFactor, NativeImage image, int x, int y, int width, int height, int advance, int ascent) implements Glyph {
      final float scaleFactor;
      final NativeImage image;
      final int x;
      final int y;
      final int width;
      final int height;
      final int ascent;

      BitmapFontGlyph(float scaleFactor, NativeImage image, int x, int y, int width, int height, int advance, int ascent) {
         this.scaleFactor = scaleFactor;
         this.image = image;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.advance = advance;
         this.ascent = ascent;
      }

      public float getAdvance() {
         return (float)this.advance;
      }

      public GlyphRenderer bake(Function function) {
         return (GlyphRenderer)function.apply(new RenderableGlyph() {
            public float getOversample() {
               return 1.0F / BitmapFontGlyph.this.scaleFactor;
            }

            public int getWidth() {
               return BitmapFontGlyph.this.width;
            }

            public int getHeight() {
               return BitmapFontGlyph.this.height;
            }

            public float getAscent() {
               return RenderableGlyph.super.getAscent() + 7.0F - (float)BitmapFontGlyph.this.ascent;
            }

            public void upload(int x, int y) {
               BitmapFontGlyph.this.image.upload(0, x, y, BitmapFontGlyph.this.x, BitmapFontGlyph.this.y, BitmapFontGlyph.this.width, BitmapFontGlyph.this.height, false, false);
            }

            public boolean hasColor() {
               return BitmapFontGlyph.this.image.getFormat().getChannelCount() > 1;
            }
         });
      }

      public float scaleFactor() {
         return this.scaleFactor;
      }

      public NativeImage image() {
         return this.image;
      }

      public int x() {
         return this.x;
      }

      public int y() {
         return this.y;
      }

      public int width() {
         return this.width;
      }

      public int height() {
         return this.height;
      }

      public int advance() {
         return this.advance;
      }

      public int ascent() {
         return this.ascent;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record Loader(Identifier filename, int height, int ascent, int[][] chars) implements FontLoader {
      private static final Codec STRINGS_CODEPOINT_GRID_CODEC;
      public static final MapCodec CODEC;

      public Loader(Identifier id, int height, int ascent, int[][] is) {
         this.filename = id;
         this.height = height;
         this.ascent = ascent;
         this.chars = is;
      }

      private static DataResult validate(int[][] codePointGrid) {
         int i = codePointGrid.length;
         if (i == 0) {
            return DataResult.error(() -> {
               return "Expected to find data in codepoint grid";
            });
         } else {
            int[] js = codePointGrid[0];
            int j = js.length;
            if (j == 0) {
               return DataResult.error(() -> {
                  return "Expected to find data in codepoint grid";
               });
            } else {
               for(int k = 1; k < i; ++k) {
                  int[] ks = codePointGrid[k];
                  if (ks.length != j) {
                     return DataResult.error(() -> {
                        return "Lines in codepoint grid have to be the same length (found: " + ks.length + " codepoints, expected: " + j + "), pad with \\u0000";
                     });
                  }
               }

               return DataResult.success(codePointGrid);
            }
         }
      }

      private static DataResult validate(Loader loader) {
         return loader.ascent > loader.height ? DataResult.error(() -> {
            return "Ascent " + loader.ascent + " higher than height " + loader.height;
         }) : DataResult.success(loader);
      }

      public FontType getType() {
         return FontType.BITMAP;
      }

      public Either build() {
         return Either.left(this::load);
      }

      private Font load(ResourceManager resourceManager) throws IOException {
         Identifier lv = this.filename.withPrefixedPath("textures/");
         InputStream inputStream = resourceManager.open(lv);

         BitmapFont var22;
         try {
            NativeImage lv2 = NativeImage.read(NativeImage.Format.RGBA, inputStream);
            int i = lv2.getWidth();
            int j = lv2.getHeight();
            int k = i / this.chars[0].length;
            int l = j / this.chars.length;
            float f = (float)this.height / (float)l;
            GlyphContainer lv3 = new GlyphContainer((ix) -> {
               return new BitmapFontGlyph[ix];
            }, (ix) -> {
               return new BitmapFontGlyph[ix][];
            });
            int m = 0;

            while(true) {
               if (m >= this.chars.length) {
                  var22 = new BitmapFont(lv2, lv3);
                  break;
               }

               int n = 0;
               int[] var13 = this.chars[m];
               int var14 = var13.length;

               for(int var15 = 0; var15 < var14; ++var15) {
                  int o = var13[var15];
                  int p = n++;
                  if (o != 0) {
                     int q = this.findCharacterStartX(lv2, k, l, p, m);
                     BitmapFontGlyph lv4 = (BitmapFontGlyph)lv3.put(o, new BitmapFontGlyph(f, lv2, p * k, m * l, k, l, (int)(0.5 + (double)((float)q * f)) + 1, this.ascent));
                     if (lv4 != null) {
                        BitmapFont.LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(o), lv);
                     }
                  }
               }

               ++m;
            }
         } catch (Throwable var21) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var20) {
                  var21.addSuppressed(var20);
               }
            }

            throw var21;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return var22;
      }

      private int findCharacterStartX(NativeImage image, int characterWidth, int characterHeight, int charPosX, int charPosY) {
         int m;
         for(m = characterWidth - 1; m >= 0; --m) {
            int n = charPosX * characterWidth + m;

            for(int o = 0; o < characterHeight; ++o) {
               int p = charPosY * characterHeight + o;
               if (image.getOpacity(n, p) != 0) {
                  return m + 1;
               }
            }
         }

         return m + 1;
      }

      public Identifier filename() {
         return this.filename;
      }

      public int height() {
         return this.height;
      }

      public int ascent() {
         return this.ascent;
      }

      public int[][] chars() {
         return this.chars;
      }

      static {
         STRINGS_CODEPOINT_GRID_CODEC = Codecs.validate(Codec.STRING.listOf().xmap((strings) -> {
            int i = strings.size();
            int[][] is = new int[i][];

            for(int j = 0; j < i; ++j) {
               is[j] = ((String)strings.get(j)).codePoints().toArray();
            }

            return is;
         }, (codePointGrid) -> {
            List list = new ArrayList(codePointGrid.length);
            int[][] var2 = codePointGrid;
            int var3 = codePointGrid.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               int[] js = var2[var4];
               list.add(new String(js, 0, js.length));
            }

            return list;
         }), Loader::validate);
         CODEC = Codecs.validateMap(RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Identifier.CODEC.fieldOf("file").forGetter(Loader::filename), Codec.INT.optionalFieldOf("height", 8).forGetter(Loader::height), Codec.INT.fieldOf("ascent").forGetter(Loader::ascent), STRINGS_CODEPOINT_GRID_CODEC.fieldOf("chars").forGetter(Loader::chars)).apply(instance, Loader::new);
         }), Loader::validate);
      }
   }
}
