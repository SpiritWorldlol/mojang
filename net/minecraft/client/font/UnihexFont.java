package net.minecraft.client.font;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.FixedBufferInputStream;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class UnihexFont implements Font {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_44764 = 16;
   private static final int field_44765 = 2;
   private static final int field_44766 = 32;
   private static final int field_44767 = 64;
   private static final int field_44768 = 96;
   private static final int field_44769 = 128;
   private final GlyphContainer glyphs;

   UnihexFont(GlyphContainer glyphs) {
      this.glyphs = glyphs;
   }

   @Nullable
   public Glyph getGlyph(int codePoint) {
      return (Glyph)this.glyphs.get(codePoint);
   }

   public IntSet getProvidedGlyphs() {
      return this.glyphs.getProvidedGlyphs();
   }

   @VisibleForTesting
   static void addRowPixels(IntBuffer pixelsOut, int row, int left, int right) {
      int l = 32 - left - 1;
      int m = 32 - right - 1;

      for(int n = l; n >= m; --n) {
         if (n < 32 && n >= 0) {
            boolean bl = (row >> n & 1) != 0;
            pixelsOut.put(bl ? -1 : 0);
         } else {
            pixelsOut.put(0);
         }
      }

   }

   static void addGlyphPixels(IntBuffer pixelsOut, BitmapGlyph glyph, int left, int right) {
      for(int k = 0; k < 16; ++k) {
         int l = glyph.getPixels(k);
         addRowPixels(pixelsOut, l, left, right);
      }

   }

   @VisibleForTesting
   static void readLines(InputStream stream, BitmapGlyphConsumer callback) throws IOException {
      int i = 0;
      ByteList byteList = new ByteArrayList(128);

      while(true) {
         boolean bl = readUntilDelimiter(stream, byteList, 58);
         int j = byteList.size();
         if (j == 0 && !bl) {
            return;
         }

         if (!bl || j != 4 && j != 5 && j != 6) {
            throw new IllegalArgumentException("Invalid entry at line " + i + ": expected 4, 5 or 6 hex digits followed by a colon");
         }

         int k = 0;

         int l;
         for(l = 0; l < j; ++l) {
            k = k << 4 | getHexDigitValue(i, byteList.getByte(l));
         }

         byteList.clear();
         readUntilDelimiter(stream, byteList, 10);
         l = byteList.size();
         BitmapGlyph var10000;
         switch (l) {
            case 32:
               var10000 = UnihexFont.FontImage8x16.read(i, byteList);
               break;
            case 64:
               var10000 = UnihexFont.FontImage16x16.read(i, byteList);
               break;
            case 96:
               var10000 = UnihexFont.FontImage32x16.read24x16(i, byteList);
               break;
            case 128:
               var10000 = UnihexFont.FontImage32x16.read32x16(i, byteList);
               break;
            default:
               throw new IllegalArgumentException("Invalid entry at line " + i + ": expected hex number describing (8,16,24,32) x 16 bitmap, followed by a new line");
         }

         BitmapGlyph lv = var10000;
         callback.accept(k, lv);
         ++i;
         byteList.clear();
      }
   }

   static int getHexDigitValue(int lineNum, ByteList bytes, int index) {
      return getHexDigitValue(lineNum, bytes.getByte(index));
   }

   private static int getHexDigitValue(int lineNum, byte digit) {
      byte var10000;
      switch (digit) {
         case 48:
            var10000 = 0;
            break;
         case 49:
            var10000 = 1;
            break;
         case 50:
            var10000 = 2;
            break;
         case 51:
            var10000 = 3;
            break;
         case 52:
            var10000 = 4;
            break;
         case 53:
            var10000 = 5;
            break;
         case 54:
            var10000 = 6;
            break;
         case 55:
            var10000 = 7;
            break;
         case 56:
            var10000 = 8;
            break;
         case 57:
            var10000 = 9;
            break;
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         default:
            throw new IllegalArgumentException("Invalid entry at line " + lineNum + ": expected hex digit, got " + (char)digit);
         case 65:
            var10000 = 10;
            break;
         case 66:
            var10000 = 11;
            break;
         case 67:
            var10000 = 12;
            break;
         case 68:
            var10000 = 13;
            break;
         case 69:
            var10000 = 14;
            break;
         case 70:
            var10000 = 15;
      }

      return var10000;
   }

   private static boolean readUntilDelimiter(InputStream stream, ByteList data, int delimiter) throws IOException {
      while(true) {
         int j = stream.read();
         if (j == -1) {
            return false;
         }

         if (j == delimiter) {
            return true;
         }

         data.add((byte)j);
      }
   }

   @Environment(EnvType.CLIENT)
   public interface BitmapGlyph {
      int getPixels(int y);

      int bitWidth();

      default int getNonemptyColumnBitmask() {
         int i = 0;

         for(int j = 0; j < 16; ++j) {
            i |= this.getPixels(j);
         }

         return i;
      }

      default int getPackedDimensions() {
         int i = this.getNonemptyColumnBitmask();
         int j = this.bitWidth();
         int k;
         int l;
         if (i == 0) {
            k = 0;
            l = j;
         } else {
            k = Integer.numberOfLeadingZeros(i);
            l = 32 - Integer.numberOfTrailingZeros(i) - 1;
         }

         return UnihexFont.Dimensions.pack(k, l);
      }
   }

   @Environment(EnvType.CLIENT)
   static record FontImage8x16(byte[] contents) implements BitmapGlyph {
      private FontImage8x16(byte[] sizes) {
         this.contents = sizes;
      }

      public int getPixels(int y) {
         return this.contents[y] << 24;
      }

      static BitmapGlyph read(int lineNum, ByteList data) {
         byte[] bs = new byte[16];
         int j = 0;

         for(int k = 0; k < 16; ++k) {
            int l = UnihexFont.getHexDigitValue(lineNum, data, j++);
            int m = UnihexFont.getHexDigitValue(lineNum, data, j++);
            byte b = (byte)(l << 4 | m);
            bs[k] = b;
         }

         return new FontImage8x16(bs);
      }

      public int bitWidth() {
         return 8;
      }

      public byte[] contents() {
         return this.contents;
      }
   }

   @Environment(EnvType.CLIENT)
   static record FontImage16x16(short[] contents) implements BitmapGlyph {
      private FontImage16x16(short[] ss) {
         this.contents = ss;
      }

      public int getPixels(int y) {
         return this.contents[y] << 16;
      }

      static BitmapGlyph read(int lineNum, ByteList data) {
         short[] ss = new short[16];
         int j = 0;

         for(int k = 0; k < 16; ++k) {
            int l = UnihexFont.getHexDigitValue(lineNum, data, j++);
            int m = UnihexFont.getHexDigitValue(lineNum, data, j++);
            int n = UnihexFont.getHexDigitValue(lineNum, data, j++);
            int o = UnihexFont.getHexDigitValue(lineNum, data, j++);
            short s = (short)(l << 12 | m << 8 | n << 4 | o);
            ss[k] = s;
         }

         return new FontImage16x16(ss);
      }

      public int bitWidth() {
         return 16;
      }

      public short[] contents() {
         return this.contents;
      }
   }

   @Environment(EnvType.CLIENT)
   static record FontImage32x16(int[] contents, int bitWidth) implements BitmapGlyph {
      private static final int field_44775 = 24;

      private FontImage32x16(int[] is, int i) {
         this.contents = is;
         this.bitWidth = i;
      }

      public int getPixels(int y) {
         return this.contents[y];
      }

      static BitmapGlyph read24x16(int lineNum, ByteList data) {
         int[] is = new int[16];
         int j = 0;
         int k = 0;

         for(int l = 0; l < 16; ++l) {
            int m = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int n = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int o = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int p = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int q = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int r = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int s = m << 20 | n << 16 | o << 12 | p << 8 | q << 4 | r;
            is[l] = s << 8;
            j |= s;
         }

         return new FontImage32x16(is, 24);
      }

      public static BitmapGlyph read32x16(int lineNum, ByteList data) {
         int[] is = new int[16];
         int j = 0;
         int k = 0;

         for(int l = 0; l < 16; ++l) {
            int m = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int n = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int o = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int p = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int q = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int r = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int s = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int t = UnihexFont.getHexDigitValue(lineNum, data, k++);
            int u = m << 28 | n << 24 | o << 20 | p << 16 | q << 12 | r << 8 | s << 4 | t;
            is[l] = u;
            j |= u;
         }

         return new FontImage32x16(is, 32);
      }

      public int[] contents() {
         return this.contents;
      }

      public int bitWidth() {
         return this.bitWidth;
      }
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface BitmapGlyphConsumer {
      void accept(int codePoint, BitmapGlyph glyph);
   }

   @Environment(EnvType.CLIENT)
   private static record UnicodeTextureGlyph(BitmapGlyph contents, int left, int right) implements Glyph {
      final BitmapGlyph contents;
      final int left;
      final int right;

      UnicodeTextureGlyph(BitmapGlyph arg, int i, int j) {
         this.contents = arg;
         this.left = i;
         this.right = j;
      }

      public int width() {
         return this.right - this.left + 1;
      }

      public float getAdvance() {
         return (float)(this.width() / 2 + 1);
      }

      public float getShadowOffset() {
         return 0.5F;
      }

      public float getBoldOffset() {
         return 0.5F;
      }

      public GlyphRenderer bake(Function function) {
         return (GlyphRenderer)function.apply(new RenderableGlyph() {
            public float getOversample() {
               return 2.0F;
            }

            public int getWidth() {
               return UnicodeTextureGlyph.this.width();
            }

            public int getHeight() {
               return 16;
            }

            public void upload(int x, int y) {
               MemoryStack memoryStack = MemoryStack.stackPush();

               try {
                  IntBuffer intBuffer = memoryStack.mallocInt(UnicodeTextureGlyph.this.width() * 16);
                  UnihexFont.addGlyphPixels(intBuffer, UnicodeTextureGlyph.this.contents, UnicodeTextureGlyph.this.left, UnicodeTextureGlyph.this.right);
                  intBuffer.rewind();
                  GlStateManager.upload(0, x, y, UnicodeTextureGlyph.this.width(), 16, NativeImage.Format.RGBA, intBuffer);
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

            public boolean hasColor() {
               return true;
            }
         });
      }

      public BitmapGlyph contents() {
         return this.contents;
      }

      public int left() {
         return this.left;
      }

      public int right() {
         return this.right;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Loader implements FontLoader {
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Identifier.CODEC.fieldOf("hex_file").forGetter((loader) -> {
            return loader.sizes;
         }), UnihexFont.DimensionOverride.CODEC.listOf().fieldOf("size_overrides").forGetter((loader) -> {
            return loader.overrides;
         })).apply(instance, Loader::new);
      });
      private final Identifier sizes;
      private final List overrides;

      private Loader(Identifier sizes, List overrides) {
         this.sizes = sizes;
         this.overrides = overrides;
      }

      public Either build() {
         return Either.left(this::load);
      }

      private Font load(ResourceManager resourceManager) throws IOException {
         InputStream inputStream = resourceManager.open(this.sizes);

         UnihexFont var3;
         try {
            var3 = this.loadHexFile(inputStream);
         } catch (Throwable var6) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return var3;
      }

      private UnihexFont loadHexFile(InputStream stream) throws IOException {
         GlyphContainer lv = new GlyphContainer((ix) -> {
            return new BitmapGlyph[ix];
         }, (ix) -> {
            return new BitmapGlyph[ix][];
         });
         Objects.requireNonNull(lv);
         BitmapGlyphConsumer lv2 = lv::put;
         ZipInputStream zipInputStream = new ZipInputStream(stream);

         UnihexFont var17;
         try {
            ZipEntry zipEntry;
            while((zipEntry = zipInputStream.getNextEntry()) != null) {
               String string = zipEntry.getName();
               if (string.endsWith(".hex")) {
                  UnihexFont.LOGGER.info("Found {}, loading", string);
                  UnihexFont.readLines(new FixedBufferInputStream(zipInputStream), lv2);
               }
            }

            GlyphContainer lv3 = new GlyphContainer((ix) -> {
               return new UnicodeTextureGlyph[ix];
            }, (ix) -> {
               return new UnicodeTextureGlyph[ix][];
            });
            Iterator var7 = this.overrides.iterator();

            label40:
            while(true) {
               if (var7.hasNext()) {
                  DimensionOverride lv4 = (DimensionOverride)var7.next();
                  int i = lv4.from;
                  int j = lv4.to;
                  Dimensions lv5 = lv4.dimensions;
                  int k = i;

                  while(true) {
                     if (k > j) {
                        continue label40;
                     }

                     BitmapGlyph lv6 = (BitmapGlyph)lv.remove(k);
                     if (lv6 != null) {
                        lv3.put(k, new UnicodeTextureGlyph(lv6, lv5.left, lv5.right));
                     }

                     ++k;
                  }
               }

               lv.forEachGlyph((codePoint, glyph) -> {
                  int j = glyph.getPackedDimensions();
                  int k = UnihexFont.Dimensions.getLeft(j);
                  int l = UnihexFont.Dimensions.getRight(j);
                  lv3.put(codePoint, new UnicodeTextureGlyph(glyph, k, l));
               });
               var17 = new UnihexFont(lv3);
               break;
            }
         } catch (Throwable var15) {
            try {
               zipInputStream.close();
            } catch (Throwable var14) {
               var15.addSuppressed(var14);
            }

            throw var15;
         }

         zipInputStream.close();
         return var17;
      }

      public static FontLoader fromJson(JsonObject json) {
         return (FontLoader)CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, (string) -> {
         });
      }
   }

   @Environment(EnvType.CLIENT)
   public static record Dimensions(int left, int right) {
      final int left;
      final int right;
      public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(Codec.INT.fieldOf("left").forGetter(Dimensions::left), Codec.INT.fieldOf("right").forGetter(Dimensions::right)).apply(instance, Dimensions::new);
      });
      public static final Codec CODEC;

      public Dimensions(int i, int j) {
         this.left = i;
         this.right = j;
      }

      public int packedValue() {
         return pack(this.left, this.right);
      }

      public static int pack(int left, int right) {
         return (left & 255) << 8 | right & 255;
      }

      public static int getLeft(int packed) {
         return (byte)(packed >> 8);
      }

      public static int getRight(int packed) {
         return (byte)packed;
      }

      public int left() {
         return this.left;
      }

      public int right() {
         return this.right;
      }

      static {
         CODEC = MAP_CODEC.codec();
      }
   }

   @Environment(EnvType.CLIENT)
   private static record DimensionOverride(int from, int to, Dimensions dimensions) {
      final int from;
      final int to;
      final Dimensions dimensions;
      private static final Codec NON_VALIDATED_CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codecs.CODEPOINT.fieldOf("from").forGetter(DimensionOverride::from), Codecs.CODEPOINT.fieldOf("to").forGetter(DimensionOverride::to), UnihexFont.Dimensions.MAP_CODEC.forGetter(DimensionOverride::dimensions)).apply(instance, DimensionOverride::new);
      });
      public static final Codec CODEC;

      private DimensionOverride(int i, int j, Dimensions arg) {
         this.from = i;
         this.to = j;
         this.dimensions = arg;
      }

      public int from() {
         return this.from;
      }

      public int to() {
         return this.to;
      }

      public Dimensions dimensions() {
         return this.dimensions;
      }

      static {
         CODEC = Codecs.validate(NON_VALIDATED_CODEC, (override) -> {
            return override.from >= override.to ? DataResult.error(() -> {
               return "Invalid range: [" + override.from + ";" + override.to + "]";
            }) : DataResult.success(override);
         });
      }
   }
}
