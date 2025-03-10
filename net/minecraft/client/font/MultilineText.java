package net.minecraft.client.font;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public interface MultilineText {
   MultilineText EMPTY = new MultilineText() {
      public int drawCenterWithShadow(DrawContext context, int x, int y) {
         return y;
      }

      public int drawCenterWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
         return y;
      }

      public int drawWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
         return y;
      }

      public int draw(DrawContext context, int x, int y, int lineHeight, int color) {
         return y;
      }

      public void fillBackground(DrawContext context, int centerX, int centerY, int lineHeight, int padding, int color) {
      }

      public int count() {
         return 0;
      }

      public int getMaxWidth() {
         return 0;
      }
   };

   static MultilineText create(TextRenderer renderer, StringVisitable text, int width) {
      return create(renderer, (List)renderer.wrapLines(text, width).stream().map((textx) -> {
         return new Line(textx, renderer.getWidth(textx));
      }).collect(ImmutableList.toImmutableList()));
   }

   static MultilineText create(TextRenderer renderer, StringVisitable text, int width, int maxLines) {
      return create(renderer, (List)renderer.wrapLines(text, width).stream().limit((long)maxLines).map((textx) -> {
         return new Line(textx, renderer.getWidth(textx));
      }).collect(ImmutableList.toImmutableList()));
   }

   static MultilineText create(TextRenderer renderer, Text... texts) {
      return create(renderer, (List)Arrays.stream(texts).map(Text::asOrderedText).map((text) -> {
         return new Line(text, renderer.getWidth(text));
      }).collect(ImmutableList.toImmutableList()));
   }

   static MultilineText createFromTexts(TextRenderer renderer, List texts) {
      return create(renderer, (List)texts.stream().map(Text::asOrderedText).map((text) -> {
         return new Line(text, renderer.getWidth(text));
      }).collect(ImmutableList.toImmutableList()));
   }

   static MultilineText create(final TextRenderer textRenderer, final List lines) {
      return lines.isEmpty() ? EMPTY : new MultilineText() {
         private final int maxWidth = lines.stream().mapToInt((line) -> {
            return line.width;
         }).max().orElse(0);

         public int drawCenterWithShadow(DrawContext context, int x, int y) {
            Objects.requireNonNull(textRenderer);
            return this.drawCenterWithShadow(context, x, y, 9, 16777215);
         }

         public int drawCenterWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
            int m = y;

            for(Iterator var7 = lines.iterator(); var7.hasNext(); m += lineHeight) {
               Line lv = (Line)var7.next();
               context.drawTextWithShadow(textRenderer, lv.text, x - lv.width / 2, m, color);
            }

            return m;
         }

         public int drawWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
            int m = y;

            for(Iterator var7 = lines.iterator(); var7.hasNext(); m += lineHeight) {
               Line lv = (Line)var7.next();
               context.drawTextWithShadow(textRenderer, lv.text, x, m, color);
            }

            return m;
         }

         public int draw(DrawContext context, int x, int y, int lineHeight, int color) {
            int m = y;

            for(Iterator var7 = lines.iterator(); var7.hasNext(); m += lineHeight) {
               Line lv = (Line)var7.next();
               context.drawText(textRenderer, lv.text, x, m, color, false);
            }

            return m;
         }

         public void fillBackground(DrawContext context, int centerX, int centerY, int lineHeight, int padding, int color) {
            int n = lines.stream().mapToInt((line) -> {
               return line.width;
            }).max().orElse(0);
            if (n > 0) {
               context.fill(centerX - n / 2 - padding, centerY - padding, centerX + n / 2 + padding, centerY + lines.size() * lineHeight + padding, color);
            }

         }

         public int count() {
            return lines.size();
         }

         public int getMaxWidth() {
            return this.maxWidth;
         }
      };
   }

   int drawCenterWithShadow(DrawContext context, int x, int y);

   int drawCenterWithShadow(DrawContext context, int x, int y, int lineHeight, int color);

   int drawWithShadow(DrawContext context, int x, int y, int lineHeight, int color);

   int draw(DrawContext context, int x, int y, int lineHeight, int color);

   void fillBackground(DrawContext context, int centerX, int centerY, int lineHeight, int padding, int color);

   int count();

   int getMaxWidth();

   @Environment(EnvType.CLIENT)
   public static class Line {
      final OrderedText text;
      final int width;

      Line(OrderedText text, int width) {
         this.text = text;
         this.width = width;
      }
   }
}
