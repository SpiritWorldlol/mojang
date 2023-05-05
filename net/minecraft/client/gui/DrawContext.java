package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Divider;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2ic;

@Environment(EnvType.CLIENT)
public class DrawContext {
   private static final int field_44655 = 2;
   private final MinecraftClient client;
   private final MatrixStack matrices;
   private final VertexConsumerProvider.Immediate vertexConsumers;
   private final ScissorStack scissorStack;
   private boolean field_44797;

   private DrawContext(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers) {
      this.scissorStack = new ScissorStack();
      this.client = client;
      this.matrices = matrices;
      this.vertexConsumers = vertexConsumers;
   }

   public DrawContext(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers) {
      this(client, new MatrixStack(), vertexConsumers);
   }

   /** @deprecated */
   @Deprecated
   public void method_51741(Runnable runnable) {
      this.draw();
      this.field_44797 = true;
      runnable.run();
      this.field_44797 = false;
      this.draw();
   }

   /** @deprecated */
   @Deprecated
   private void method_51744() {
      if (!this.field_44797) {
         this.draw();
      }

   }

   public int getScaledWindowWidth() {
      return this.client.getWindow().getScaledWidth();
   }

   public int getScaledWindowHeight() {
      return this.client.getWindow().getScaledHeight();
   }

   public MatrixStack getMatrices() {
      return this.matrices;
   }

   public VertexConsumerProvider.Immediate getVertexConsumers() {
      return this.vertexConsumers;
   }

   public void draw() {
      RenderSystem.disableDepthTest();
      this.vertexConsumers.draw();
      RenderSystem.enableDepthTest();
   }

   public void drawHorizontalLine(int x1, int x2, int y, int color) {
      this.method_51738(RenderLayer.method_51784(), x1, x2, y, color);
   }

   public void method_51738(RenderLayer arg, int i, int j, int k, int l) {
      if (j < i) {
         int m = i;
         i = j;
         j = m;
      }

      this.method_51739(arg, i, k, j + 1, k + 1, l);
   }

   public void drawVerticalLine(int i, int j, int y2, int color) {
      this.method_51742(RenderLayer.method_51784(), i, j, y2, color);
   }

   public void method_51742(RenderLayer arg, int i, int j, int k, int l) {
      if (k < j) {
         int m = j;
         j = k;
         k = m;
      }

      this.method_51739(arg, i, j + 1, i + 1, k, l);
   }

   public void enableScissor(int x1, int y1, int x2, int y2) {
      this.setScissor(this.scissorStack.push(new ScreenRect(x1, y1, x2 - x1, y2 - y1)));
   }

   public void disableScissor() {
      this.setScissor(this.scissorStack.pop());
   }

   private void setScissor(@Nullable ScreenRect arg) {
      this.draw();
      if (arg != null) {
         Window lv = MinecraftClient.getInstance().getWindow();
         int i = lv.getFramebufferHeight();
         double d = lv.getScaleFactor();
         double e = (double)arg.getLeft() * d;
         double f = (double)i - (double)arg.getBottom() * d;
         double g = (double)arg.width() * d;
         double h = (double)arg.height() * d;
         RenderSystem.enableScissor((int)e, (int)f, Math.max(0, (int)g), Math.max(0, (int)h));
      } else {
         RenderSystem.disableScissor();
      }

   }

   public void setShaderColor(float red, float green, float blue, float alpha) {
      this.draw();
      RenderSystem.setShaderColor(red, green, blue, alpha);
   }

   public void fill(int x1, int y1, int x2, int y2, int color) {
      this.method_51737(x1, y1, x2, y2, 0, color);
   }

   public void method_51737(int i, int j, int k, int l, int m, int n) {
      this.fill(RenderLayer.method_51784(), i, j, k, l, m, n);
   }

   public void method_51739(RenderLayer arg, int i, int j, int k, int l, int m) {
      this.fill(arg, i, j, k, l, 0, m);
   }

   public void fill(RenderLayer arg, int i, int j, int k, int l, int m, int n) {
      Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
      int o;
      if (i < k) {
         o = i;
         i = k;
         k = o;
      }

      if (j < l) {
         o = j;
         j = l;
         l = o;
      }

      float f = (float)ColorHelper.Argb.getAlpha(n) / 255.0F;
      float g = (float)ColorHelper.Argb.getRed(n) / 255.0F;
      float h = (float)ColorHelper.Argb.getGreen(n) / 255.0F;
      float p = (float)ColorHelper.Argb.getBlue(n) / 255.0F;
      VertexConsumer lv = this.vertexConsumers.getBuffer(arg);
      lv.vertex(matrix4f, (float)i, (float)j, (float)m).color(g, h, p, f).next();
      lv.vertex(matrix4f, (float)i, (float)l, (float)m).color(g, h, p, f).next();
      lv.vertex(matrix4f, (float)k, (float)l, (float)m).color(g, h, p, f).next();
      lv.vertex(matrix4f, (float)k, (float)j, (float)m).color(g, h, p, f).next();
      this.method_51744();
   }

   public void fillGradient(int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
      this.fillGradient(startX, startY, endX, endY, 0, colorStart, colorEnd);
   }

   public void fillGradient(int startX, int startY, int endX, int endY, int m, int n, int o) {
      this.method_51740(RenderLayer.method_51784(), startX, startY, endX, endY, n, o, m);
   }

   public void method_51740(RenderLayer arg, int i, int j, int k, int l, int m, int n, int o) {
      VertexConsumer lv = this.vertexConsumers.getBuffer(arg);
      this.fillGradient(lv, i, j, k, l, o, m, n);
      this.method_51744();
   }

   private void fillGradient(VertexConsumer arg, int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd) {
      float f = (float)ColorHelper.Argb.getAlpha(colorStart) / 255.0F;
      float g = (float)ColorHelper.Argb.getRed(colorStart) / 255.0F;
      float h = (float)ColorHelper.Argb.getGreen(colorStart) / 255.0F;
      float p = (float)ColorHelper.Argb.getBlue(colorStart) / 255.0F;
      float q = (float)ColorHelper.Argb.getAlpha(colorEnd) / 255.0F;
      float r = (float)ColorHelper.Argb.getRed(colorEnd) / 255.0F;
      float s = (float)ColorHelper.Argb.getGreen(colorEnd) / 255.0F;
      float t = (float)ColorHelper.Argb.getBlue(colorEnd) / 255.0F;
      Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
      arg.vertex(matrix4f, (float)startX, (float)startY, (float)z).color(g, h, p, f).next();
      arg.vertex(matrix4f, (float)startX, (float)endY, (float)z).color(r, s, t, q).next();
      arg.vertex(matrix4f, (float)endX, (float)endY, (float)z).color(r, s, t, q).next();
      arg.vertex(matrix4f, (float)endX, (float)startY, (float)z).color(g, h, p, f).next();
   }

   public void drawCenteredTextWithShadow(TextRenderer textRenderer, String text, int centerX, int y, int color) {
      this.drawTextWithShadow(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color);
   }

   public void drawCenteredTextWithShadow(TextRenderer textRenderer, Text text, int centerX, int y, int color) {
      OrderedText lv = text.asOrderedText();
      this.drawTextWithShadow(textRenderer, lv, centerX - textRenderer.getWidth(lv) / 2, y, color);
   }

   public void drawCenteredTextWithShadow(TextRenderer textRenderer, OrderedText text, int centerX, int y, int color) {
      this.drawTextWithShadow(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color);
   }

   public int drawTextWithShadow(TextRenderer textRenderer, @Nullable String text, int x, int y, int color) {
      return this.drawText(textRenderer, text, x, y, color, true);
   }

   public int drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow) {
      if (text == null) {
         return 0;
      } else {
         int l = textRenderer.draw(text, (float)x, (float)y, color, shadow, this.matrices.peek().getPositionMatrix(), this.vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880, textRenderer.isRightToLeft());
         this.method_51744();
         return l;
      }
   }

   public int drawTextWithShadow(TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
      return this.drawText(textRenderer, text, x, y, color, true);
   }

   public int drawText(TextRenderer textRenderer, OrderedText text, int x, int y, int color, boolean shadow) {
      int l = textRenderer.draw((OrderedText)text, (float)x, (float)y, color, shadow, this.matrices.peek().getPositionMatrix(), this.vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
      this.method_51744();
      return l;
   }

   public int drawTextWithShadow(TextRenderer textRenderer, Text text, int x, int y, int color) {
      return this.drawText(textRenderer, text, x, y, color, true);
   }

   public int drawText(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
      return this.drawText(textRenderer, text.asOrderedText(), x, y, color, shadow);
   }

   public void drawTextWrapped(TextRenderer textRenderer, StringVisitable text, int x, int y, int width, int color) {
      for(Iterator var7 = textRenderer.wrapLines(text, width).iterator(); var7.hasNext(); y += 9) {
         OrderedText lv = (OrderedText)var7.next();
         this.drawText(textRenderer, lv, x, y, color, false);
         Objects.requireNonNull(textRenderer);
      }

   }

   public void drawSprite(int x, int y, int z, int width, int height, Sprite sprite) {
      this.drawTexturedQuad(sprite.getAtlasId(), x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
   }

   public void drawSprite(int x, int y, int z, int width, int height, Sprite sprite, float red, float green, float blue, float alpha) {
      this.drawTexturedQuad(sprite.getAtlasId(), x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), red, green, blue, alpha);
   }

   public void drawBorder(int x, int y, int width, int height, int color) {
      this.fill(x, y, x + width, y + 1, color);
      this.fill(x, y + height - 1, x + width, y + height, color);
      this.fill(x, y + 1, x + 1, y + height - 1, color);
      this.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
   }

   public void drawTexture(Identifier texture, int x, int y, int u, int v, int width, int height) {
      this.drawTexture(texture, x, y, 0, (float)u, (float)v, width, height, 256, 256);
   }

   public void drawTexture(Identifier texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
      this.drawTexture(texture, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
   }

   public void drawTexture(Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
      this.drawTexture(texture, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
   }

   public void drawTexture(Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
      this.drawTexture(texture, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
   }

   void drawTexture(Identifier texture, int x1, int x2, int y1, int y2, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
      this.drawTexturedQuad(texture, x1, x2, y1, y2, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
   }

   void drawTexturedQuad(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
      RenderSystem.setShaderTexture(0, texture);
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
      BufferBuilder lv = Tessellator.getInstance().getBuffer();
      lv.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      lv.vertex(matrix4f, (float)x1, (float)y1, (float)z).texture(u1, v1).next();
      lv.vertex(matrix4f, (float)x1, (float)y2, (float)z).texture(u1, v2).next();
      lv.vertex(matrix4f, (float)x2, (float)y2, (float)z).texture(u2, v2).next();
      lv.vertex(matrix4f, (float)x2, (float)y1, (float)z).texture(u2, v1).next();
      BufferRenderer.drawWithGlobalProgram(lv.end());
   }

   void drawTexturedQuad(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha) {
      RenderSystem.setShaderTexture(0, texture);
      RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
      RenderSystem.enableBlend();
      Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
      BufferBuilder lv = Tessellator.getInstance().getBuffer();
      lv.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
      lv.vertex(matrix4f, (float)x1, (float)y1, (float)z).color(red, green, blue, alpha).texture(u1, v1).next();
      lv.vertex(matrix4f, (float)x1, (float)y2, (float)z).color(red, green, blue, alpha).texture(u1, v2).next();
      lv.vertex(matrix4f, (float)x2, (float)y2, (float)z).color(red, green, blue, alpha).texture(u2, v2).next();
      lv.vertex(matrix4f, (float)x2, (float)y1, (float)z).color(red, green, blue, alpha).texture(u2, v1).next();
      BufferRenderer.drawWithGlobalProgram(lv.end());
      RenderSystem.disableBlend();
   }

   public void drawNineSlicedTexture(Identifier texture, int x, int y, int width, int height, int outerSliceSize, int centerSliceWidth, int centerSliceHeight, int u, int v) {
      this.drawNineSlicedTexture(texture, x, y, width, height, outerSliceSize, outerSliceSize, outerSliceSize, outerSliceSize, centerSliceWidth, centerSliceHeight, u, v);
   }

   public void drawNineSlicedTexture(Identifier texture, int x, int y, int width, int height, int outerSliceWidth, int outerSliceHeight, int centerSliceWidth, int centerSliceHeight, int u, int v) {
      this.drawNineSlicedTexture(texture, x, y, width, height, outerSliceWidth, outerSliceHeight, outerSliceWidth, outerSliceHeight, centerSliceWidth, centerSliceHeight, u, v);
   }

   public void drawNineSlicedTexture(Identifier texture, int x, int y, int width, int height, int leftSliceWidth, int topSliceHeight, int rightSliceWidth, int bottomSliceHeight, int centerSliceWidth, int centerSliceHeight, int u, int v) {
      leftSliceWidth = Math.min(leftSliceWidth, width / 2);
      rightSliceWidth = Math.min(rightSliceWidth, width / 2);
      topSliceHeight = Math.min(topSliceHeight, height / 2);
      bottomSliceHeight = Math.min(bottomSliceHeight, height / 2);
      if (width == centerSliceWidth && height == centerSliceHeight) {
         this.drawTexture(texture, x, y, u, v, width, height);
      } else if (height == centerSliceHeight) {
         this.drawTexture(texture, x, y, u, v, leftSliceWidth, height);
         this.drawRepeatingTexture(texture, x + leftSliceWidth, y, width - rightSliceWidth - leftSliceWidth, height, u + leftSliceWidth, v, centerSliceWidth - rightSliceWidth - leftSliceWidth, centerSliceHeight);
         this.drawTexture(texture, x + width - rightSliceWidth, y, u + centerSliceWidth - rightSliceWidth, v, rightSliceWidth, height);
      } else if (width == centerSliceWidth) {
         this.drawTexture(texture, x, y, u, v, width, topSliceHeight);
         this.drawRepeatingTexture(texture, x, y + topSliceHeight, width, height - bottomSliceHeight - topSliceHeight, u, v + topSliceHeight, centerSliceWidth, centerSliceHeight - bottomSliceHeight - topSliceHeight);
         this.drawTexture(texture, x, y + height - bottomSliceHeight, u, v + centerSliceHeight - bottomSliceHeight, width, bottomSliceHeight);
      } else {
         this.drawTexture(texture, x, y, u, v, leftSliceWidth, topSliceHeight);
         this.drawRepeatingTexture(texture, x + leftSliceWidth, y, width - rightSliceWidth - leftSliceWidth, topSliceHeight, u + leftSliceWidth, v, centerSliceWidth - rightSliceWidth - leftSliceWidth, topSliceHeight);
         this.drawTexture(texture, x + width - rightSliceWidth, y, u + centerSliceWidth - rightSliceWidth, v, rightSliceWidth, topSliceHeight);
         this.drawTexture(texture, x, y + height - bottomSliceHeight, u, v + centerSliceHeight - bottomSliceHeight, leftSliceWidth, bottomSliceHeight);
         this.drawRepeatingTexture(texture, x + leftSliceWidth, y + height - bottomSliceHeight, width - rightSliceWidth - leftSliceWidth, bottomSliceHeight, u + leftSliceWidth, v + centerSliceHeight - bottomSliceHeight, centerSliceWidth - rightSliceWidth - leftSliceWidth, bottomSliceHeight);
         this.drawTexture(texture, x + width - rightSliceWidth, y + height - bottomSliceHeight, u + centerSliceWidth - rightSliceWidth, v + centerSliceHeight - bottomSliceHeight, rightSliceWidth, bottomSliceHeight);
         this.drawRepeatingTexture(texture, x, y + topSliceHeight, leftSliceWidth, height - bottomSliceHeight - topSliceHeight, u, v + topSliceHeight, leftSliceWidth, centerSliceHeight - bottomSliceHeight - topSliceHeight);
         this.drawRepeatingTexture(texture, x + leftSliceWidth, y + topSliceHeight, width - rightSliceWidth - leftSliceWidth, height - bottomSliceHeight - topSliceHeight, u + leftSliceWidth, v + topSliceHeight, centerSliceWidth - rightSliceWidth - leftSliceWidth, centerSliceHeight - bottomSliceHeight - topSliceHeight);
         this.drawRepeatingTexture(texture, x + width - rightSliceWidth, y + topSliceHeight, leftSliceWidth, height - bottomSliceHeight - topSliceHeight, u + centerSliceWidth - rightSliceWidth, v + topSliceHeight, rightSliceWidth, centerSliceHeight - bottomSliceHeight - topSliceHeight);
      }
   }

   public void drawRepeatingTexture(Identifier texture, int x, int y, int width, int height, int u, int v, int textureWidth, int textureHeight) {
      int q = x;

      int r;
      for(IntIterator intIterator = createDivider(width, textureWidth); intIterator.hasNext(); q += r) {
         r = intIterator.nextInt();
         int s = (textureWidth - r) / 2;
         int t = y;

         int u;
         for(IntIterator intIterator2 = createDivider(height, textureHeight); intIterator2.hasNext(); t += u) {
            u = intIterator2.nextInt();
            int v = (textureHeight - u) / 2;
            this.drawTexture(texture, q, t, u + s, v + v, r, u);
         }
      }

   }

   private static IntIterator createDivider(int sideLength, int textureSideLength) {
      int k = MathHelper.ceilDiv(sideLength, textureSideLength);
      return new Divider(sideLength, k);
   }

   public void drawItem(ItemStack item, int x, int y) {
      this.drawItem(this.client.player, this.client.world, item, x, y, 0);
   }

   public void drawItem(ItemStack stack, int x, int y, int seed) {
      this.drawItem(this.client.player, this.client.world, stack, x, y, seed);
   }

   public void drawItem(ItemStack stack, int x, int y, int seed, int z) {
      this.drawItem(this.client.player, this.client.world, stack, x, y, seed, z);
   }

   public void drawItemWithoutEntity(ItemStack stack, int x, int y) {
      this.drawItem((LivingEntity)null, this.client.world, stack, x, y, 0);
   }

   public void drawItem(LivingEntity entity, ItemStack stack, int x, int y, int seed) {
      this.drawItem(entity, entity.getWorld(), stack, x, y, seed);
   }

   private void drawItem(@Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed) {
      this.drawItem(entity, world, stack, x, y, seed, 0);
   }

   private void drawItem(@Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int z) {
      if (!stack.isEmpty()) {
         BakedModel lv = this.client.getItemRenderer().getModel(stack, world, entity, seed);
         this.matrices.push();
         this.matrices.translate((float)(x + 8), (float)(y + 8), (float)(150 + (lv.hasDepth() ? z : 0)));

         try {
            this.matrices.multiplyPositionMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
            this.matrices.scale(16.0F, 16.0F, 16.0F);
            boolean bl = !lv.isSideLit();
            if (bl) {
               DiffuseLighting.disableGuiDepthLighting();
            }

            this.client.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI, false, this.matrices, this.getVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, lv);
            this.draw();
            if (bl) {
               DiffuseLighting.enableGuiDepthLighting();
            }
         } catch (Throwable var12) {
            CrashReport lv2 = CrashReport.create(var12, "Rendering item");
            CrashReportSection lv3 = lv2.addElement("Item being rendered");
            lv3.add("Item Type", () -> {
               return String.valueOf(stack.getItem());
            });
            lv3.add("Item Damage", () -> {
               return String.valueOf(stack.getDamage());
            });
            lv3.add("Item NBT", () -> {
               return String.valueOf(stack.getNbt());
            });
            lv3.add("Item Foil", () -> {
               return String.valueOf(stack.hasGlint());
            });
            throw new CrashException(lv2);
         }

         this.matrices.pop();
      }
   }

   public void drawItemInSlot(TextRenderer textRenderer, ItemStack stack, int x, int y) {
      this.drawItemInSlot(textRenderer, stack, x, y, (String)null);
   }

   public void drawItemInSlot(TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride) {
      if (!stack.isEmpty()) {
         this.matrices.push();
         if (stack.getCount() != 1 || countOverride != null) {
            String string2 = countOverride == null ? String.valueOf(stack.getCount()) : countOverride;
            this.matrices.translate(0.0F, 0.0F, 200.0F);
            this.drawText(textRenderer, string2, x + 19 - 2 - textRenderer.getWidth(string2), y + 6 + 3, 16777215, true);
         }

         int m;
         int n;
         if (stack.isItemBarVisible()) {
            int k = stack.getItemBarStep();
            int l = stack.getItemBarColor();
            m = x + 2;
            n = y + 13;
            this.method_51739(RenderLayer.method_51785(), m, n, m + 13, n + 2, -16777216);
            this.method_51739(RenderLayer.method_51785(), m, n, m + k, n + 1, l | -16777216);
         }

         ClientPlayerEntity lv = this.client.player;
         float f = lv == null ? 0.0F : lv.getItemCooldownManager().getCooldownProgress(stack.getItem(), this.client.getTickDelta());
         if (f > 0.0F) {
            m = y + MathHelper.floor(16.0F * (1.0F - f));
            n = m + MathHelper.ceil(16.0F * f);
            this.method_51739(RenderLayer.method_51785(), x, m, x + 16, n, Integer.MAX_VALUE);
         }

         this.matrices.pop();
      }
   }

   public void drawItemTooltip(TextRenderer textRenderer, ItemStack stack, int x, int y) {
      this.drawTooltip(textRenderer, Screen.getTooltipFromItem(this.client, stack), stack.getTooltipData(), x, y);
   }

   public void drawTooltip(TextRenderer textRenderer, List text, Optional data, int x, int y) {
      List list2 = (List)text.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
      data.ifPresent((datax) -> {
         list2.add(1, TooltipComponent.of(datax));
      });
      this.drawTooltip(textRenderer, list2, x, y, HoveredTooltipPositioner.INSTANCE);
   }

   public void drawTooltip(TextRenderer textRenderer, Text text, int x, int y) {
      this.drawOrderedTooltip(textRenderer, List.of(text.asOrderedText()), x, y);
   }

   public void drawTooltip(TextRenderer textRenderer, List text, int x, int y) {
      this.drawOrderedTooltip(textRenderer, Lists.transform(text, Text::asOrderedText), x, y);
   }

   public void drawOrderedTooltip(TextRenderer textRenderer, List text, int x, int y) {
      this.drawTooltip(textRenderer, (List)text.stream().map(TooltipComponent::of).collect(Collectors.toList()), x, y, HoveredTooltipPositioner.INSTANCE);
   }

   public void drawTooltip(TextRenderer textRenderer, List text, TooltipPositioner positioner, int x, int y) {
      this.drawTooltip(textRenderer, (List)text.stream().map(TooltipComponent::of).collect(Collectors.toList()), x, y, positioner);
   }

   private void drawTooltip(TextRenderer textRenderer, List components, int x, int y, TooltipPositioner positioner) {
      if (!components.isEmpty()) {
         int k = 0;
         int l = components.size() == 1 ? -2 : 0;

         TooltipComponent lv;
         for(Iterator var8 = components.iterator(); var8.hasNext(); l += lv.getHeight()) {
            lv = (TooltipComponent)var8.next();
            int m = lv.getWidth(textRenderer);
            if (m > k) {
               k = m;
            }
         }

         Vector2ic vector2ic = positioner.getPosition(this.getScaledWindowWidth(), this.getScaledWindowHeight(), x, y, k, l);
         int p = vector2ic.x();
         int q = vector2ic.y();
         this.matrices.push();
         int r = true;
         this.method_51741(() -> {
            TooltipBackgroundRenderer.render(this, p, q, k, l, 400);
         });
         this.matrices.translate(0.0F, 0.0F, 400.0F);
         int s = q;

         int t;
         TooltipComponent lv2;
         for(t = 0; t < components.size(); ++t) {
            lv2 = (TooltipComponent)components.get(t);
            lv2.drawText(textRenderer, p, s, this.matrices.peek().getPositionMatrix(), this.vertexConsumers);
            s += lv2.getHeight() + (t == 0 ? 2 : 0);
         }

         s = q;

         for(t = 0; t < components.size(); ++t) {
            lv2 = (TooltipComponent)components.get(t);
            lv2.drawItems(textRenderer, p, s, this);
            s += lv2.getHeight() + (t == 0 ? 2 : 0);
         }

         this.matrices.pop();
      }
   }

   public void drawHoverEvent(TextRenderer textRenderer, @Nullable Style style, int x, int y) {
      if (style != null && style.getHoverEvent() != null) {
         HoverEvent lv = style.getHoverEvent();
         HoverEvent.ItemStackContent lv2 = (HoverEvent.ItemStackContent)lv.getValue(HoverEvent.Action.SHOW_ITEM);
         if (lv2 != null) {
            this.drawItemTooltip(textRenderer, lv2.asStack(), x, y);
         } else {
            HoverEvent.EntityContent lv3 = (HoverEvent.EntityContent)lv.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (lv3 != null) {
               if (this.client.options.advancedItemTooltips) {
                  this.drawTooltip(textRenderer, lv3.asTooltip(), x, y);
               }
            } else {
               Text lv4 = (Text)lv.getValue(HoverEvent.Action.SHOW_TEXT);
               if (lv4 != null) {
                  this.drawOrderedTooltip(textRenderer, textRenderer.wrapLines(lv4, Math.max(this.getScaledWindowWidth() / 2, 200)), x, y);
               }
            }
         }

      }
   }

   @Environment(EnvType.CLIENT)
   static class ScissorStack {
      private final Deque stack = new ArrayDeque();

      public ScreenRect push(ScreenRect rect) {
         ScreenRect lv = (ScreenRect)this.stack.peekLast();
         if (lv != null) {
            ScreenRect lv2 = (ScreenRect)Objects.requireNonNullElse(rect.intersection(lv), ScreenRect.empty());
            this.stack.addLast(lv2);
            return lv2;
         } else {
            this.stack.addLast(rect);
            return rect;
         }
      }

      public @Nullable ScreenRect pop() {
         if (this.stack.isEmpty()) {
            throw new IllegalStateException("Scissor stack underflow");
         } else {
            this.stack.removeLast();
            return (ScreenRect)this.stack.peekLast();
         }
      }
   }
}
