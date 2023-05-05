package net.minecraft.client.font;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class GlyphContainer {
   private static final int ROW_SHIFT = 8;
   private static final int ENTRIES_PER_ROW = 256;
   private static final int LAST_ENTRY_NUM_IN_ROW = 255;
   private static final int LAST_ROW_NUM = 4351;
   private static final int NUM_ROWS = 4352;
   private final Object[] defaultRow;
   private final Object[][] rows;
   private final IntFunction makeRow;

   public GlyphContainer(IntFunction makeRow, IntFunction makeScroll) {
      this.defaultRow = (Object[])makeRow.apply(256);
      this.rows = (Object[][])makeScroll.apply(4352);
      Arrays.fill(this.rows, this.defaultRow);
      this.makeRow = makeRow;
   }

   public void clear() {
      Arrays.fill(this.rows, this.defaultRow);
   }

   @Nullable
   public Object get(int codePoint) {
      int j = codePoint >> 8;
      int k = codePoint & 255;
      return this.rows[j][k];
   }

   @Nullable
   public Object put(int codePoint, Object glyph) {
      int j = codePoint >> 8;
      int k = codePoint & 255;
      Object[] objects = this.rows[j];
      if (objects == this.defaultRow) {
         objects = (Object[])this.makeRow.apply(256);
         this.rows[j] = objects;
         objects[k] = glyph;
         return null;
      } else {
         Object object2 = objects[k];
         objects[k] = glyph;
         return object2;
      }
   }

   public Object computeIfAbsent(int codePoint, IntFunction ifAbsent) {
      int j = codePoint >> 8;
      int k = codePoint & 255;
      Object[] objects = this.rows[j];
      Object object = objects[k];
      if (object != null) {
         return object;
      } else {
         if (objects == this.defaultRow) {
            objects = (Object[])this.makeRow.apply(256);
            this.rows[j] = objects;
         }

         Object object2 = ifAbsent.apply(codePoint);
         objects[k] = object2;
         return object2;
      }
   }

   @Nullable
   public Object remove(int codePoint) {
      int j = codePoint >> 8;
      int k = codePoint & 255;
      Object[] objects = this.rows[j];
      if (objects == this.defaultRow) {
         return null;
      } else {
         Object object = objects[k];
         objects[k] = null;
         return object;
      }
   }

   public void forEachGlyph(GlyphConsumer glyphConsumer) {
      for(int i = 0; i < this.rows.length; ++i) {
         Object[] objects = this.rows[i];
         if (objects != this.defaultRow) {
            for(int j = 0; j < objects.length; ++j) {
               Object object = objects[j];
               if (object != null) {
                  int k = i << 8 | j;
                  glyphConsumer.accept(k, object);
               }
            }
         }
      }

   }

   public IntSet getProvidedGlyphs() {
      IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
      this.forEachGlyph((codePoint, glyph) -> {
         intOpenHashSet.add(codePoint);
      });
      return intOpenHashSet;
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface GlyphConsumer {
      void accept(int codePoint, Object glyph);
   }
}
