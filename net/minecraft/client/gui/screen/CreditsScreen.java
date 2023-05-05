package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.sound.MusicSound;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class CreditsScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Identifier VIGNETTE_TEXTURE = new Identifier("textures/misc/vignette.png");
   private static final Text SEPARATOR_LINE;
   private static final String CENTERED_LINE_PREFIX = "           ";
   private static final String OBFUSCATION_PLACEHOLDER;
   private static final float SPACE_BAR_SPEED_MULTIPLIER = 5.0F;
   private static final float CTRL_KEY_SPEED_MULTIPLIER = 15.0F;
   private final boolean endCredits;
   private final Runnable finishAction;
   private float time;
   private List credits;
   private IntSet centeredLines;
   private int creditsHeight;
   private boolean spaceKeyPressed;
   private final IntSet pressedCtrlKeys = new IntOpenHashSet();
   private float speed;
   private final float baseSpeed;
   private int speedMultiplier;
   private final LogoDrawer logoDrawer = new LogoDrawer(false);

   public CreditsScreen(boolean endCredits, Runnable finishAction) {
      super(NarratorManager.EMPTY);
      this.endCredits = endCredits;
      this.finishAction = finishAction;
      if (!endCredits) {
         this.baseSpeed = 0.75F;
      } else {
         this.baseSpeed = 0.5F;
      }

      this.speedMultiplier = 1;
      this.speed = this.baseSpeed;
   }

   private float getSpeed() {
      return this.spaceKeyPressed ? this.baseSpeed * (5.0F + (float)this.pressedCtrlKeys.size() * 15.0F) * (float)this.speedMultiplier : this.baseSpeed * (float)this.speedMultiplier;
   }

   public void tick() {
      this.client.getMusicTracker().tick();
      this.client.getSoundManager().tick(false);
      float f = (float)(this.creditsHeight + this.height + this.height + 24);
      if (this.time > f) {
         this.closeScreen();
      }

   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_UP) {
         this.speedMultiplier = -1;
      } else if (keyCode != GLFW.GLFW_KEY_LEFT_CONTROL && keyCode != GLFW.GLFW_KEY_RIGHT_CONTROL) {
         if (keyCode == GLFW.GLFW_KEY_SPACE) {
            this.spaceKeyPressed = true;
         }
      } else {
         this.pressedCtrlKeys.add(keyCode);
      }

      this.speed = this.getSpeed();
      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_UP) {
         this.speedMultiplier = 1;
      }

      if (keyCode == GLFW.GLFW_KEY_SPACE) {
         this.spaceKeyPressed = false;
      } else if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
         this.pressedCtrlKeys.remove(keyCode);
      }

      this.speed = this.getSpeed();
      return super.keyReleased(keyCode, scanCode, modifiers);
   }

   public void close() {
      this.closeScreen();
   }

   private void closeScreen() {
      this.finishAction.run();
   }

   protected void init() {
      if (this.credits == null) {
         this.credits = Lists.newArrayList();
         this.centeredLines = new IntOpenHashSet();
         if (this.endCredits) {
            this.load("texts/end.txt", this::readPoem);
         }

         this.load("texts/credits.json", this::readCredits);
         if (this.endCredits) {
            this.load("texts/postcredits.txt", this::readPoem);
         }

         this.creditsHeight = this.credits.size() * 12;
      }
   }

   private void load(String id, CreditsReader reader) {
      try {
         Reader reader = this.client.getResourceManager().openAsReader(new Identifier(id));

         try {
            reader.read(reader);
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
      } catch (Exception var8) {
         LOGGER.error("Couldn't load credits", var8);
      }

   }

   private void readPoem(Reader reader) throws IOException {
      BufferedReader bufferedReader = new BufferedReader(reader);
      Random lv = Random.create(8124371L);

      String string;
      int i;
      while((string = bufferedReader.readLine()) != null) {
         String string2;
         String string3;
         for(string = string.replaceAll("PLAYERNAME", this.client.getSession().getUsername()); (i = string.indexOf(OBFUSCATION_PLACEHOLDER)) != -1; string = string2 + Formatting.WHITE + Formatting.OBFUSCATED + "XXXXXXXX".substring(0, lv.nextInt(4) + 3) + string3) {
            string2 = string.substring(0, i);
            string3 = string.substring(i + OBFUSCATION_PLACEHOLDER.length());
         }

         this.addText(string);
         this.addEmptyLine();
      }

      for(i = 0; i < 8; ++i) {
         this.addEmptyLine();
      }

   }

   private void readCredits(Reader reader) {
      JsonArray jsonArray = JsonHelper.deserializeArray(reader);
      Iterator var3 = jsonArray.iterator();

      while(var3.hasNext()) {
         JsonElement jsonElement = (JsonElement)var3.next();
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         String string = jsonObject.get("section").getAsString();
         this.addText(SEPARATOR_LINE, true);
         this.addText(Text.literal(string).formatted(Formatting.YELLOW), true);
         this.addText(SEPARATOR_LINE, true);
         this.addEmptyLine();
         this.addEmptyLine();
         JsonArray jsonArray2 = jsonObject.getAsJsonArray("disciplines");
         Iterator var8 = jsonArray2.iterator();

         while(var8.hasNext()) {
            JsonElement jsonElement2 = (JsonElement)var8.next();
            JsonObject jsonObject2 = jsonElement2.getAsJsonObject();
            String string2 = jsonObject2.get("discipline").getAsString();
            if (StringUtils.isNotEmpty(string2)) {
               this.addText(Text.literal(string2).formatted(Formatting.YELLOW), true);
               this.addEmptyLine();
               this.addEmptyLine();
            }

            JsonArray jsonArray3 = jsonObject2.getAsJsonArray("titles");
            Iterator var13 = jsonArray3.iterator();

            while(var13.hasNext()) {
               JsonElement jsonElement3 = (JsonElement)var13.next();
               JsonObject jsonObject3 = jsonElement3.getAsJsonObject();
               String string3 = jsonObject3.get("title").getAsString();
               JsonArray jsonArray4 = jsonObject3.getAsJsonArray("names");
               this.addText(Text.literal(string3).formatted(Formatting.GRAY), false);
               Iterator var18 = jsonArray4.iterator();

               while(var18.hasNext()) {
                  JsonElement jsonElement4 = (JsonElement)var18.next();
                  String string4 = jsonElement4.getAsString();
                  this.addText(Text.literal("           ").append(string4).formatted(Formatting.WHITE), false);
               }

               this.addEmptyLine();
               this.addEmptyLine();
            }
         }
      }

   }

   private void addEmptyLine() {
      this.credits.add(OrderedText.EMPTY);
   }

   private void addText(String text) {
      this.credits.addAll(this.client.textRenderer.wrapLines(Text.literal(text), 256));
   }

   private void addText(Text text, boolean centered) {
      if (centered) {
         this.centeredLines.add(this.credits.size());
      }

      this.credits.add(text.asOrderedText());
   }

   private void renderBackground(DrawContext context) {
      int i = this.width;
      float f = this.time * 0.5F;
      int j = true;
      float g = this.time / this.baseSpeed;
      float h = g * 0.02F;
      float k = (float)(this.creditsHeight + this.height + this.height + 24) / this.baseSpeed;
      float l = (k - 20.0F - g) * 0.005F;
      if (l < h) {
         h = l;
      }

      if (h > 1.0F) {
         h = 1.0F;
      }

      h *= h;
      h = h * 96.0F / 255.0F;
      context.setShaderColor(h, h, h, 1.0F);
      context.drawTexture(OPTIONS_BACKGROUND_TEXTURE, 0, 0, 0, 0.0F, f, i, this.height, 64, 64);
      context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.time = Math.max(0.0F, this.time + delta * this.speed);
      this.renderBackground(context);
      int k = this.width / 2 - 128;
      int l = this.height + 50;
      float g = -this.time;
      context.getMatrices().push();
      context.getMatrices().translate(0.0F, g, 0.0F);
      this.logoDrawer.draw(context, this.width, 1.0F, l);
      int m = l + 100;

      for(int n = 0; n < this.credits.size(); ++n) {
         if (n == this.credits.size() - 1) {
            float h = (float)m + g - (float)(this.height / 2 - 6);
            if (h < 0.0F) {
               context.getMatrices().translate(0.0F, -h, 0.0F);
            }
         }

         if ((float)m + g + 12.0F + 8.0F > 0.0F && (float)m + g < (float)this.height) {
            OrderedText lv = (OrderedText)this.credits.get(n);
            if (this.centeredLines.contains(n)) {
               context.drawCenteredTextWithShadow(this.textRenderer, lv, k + 128, m, 16777215);
            } else {
               context.drawTextWithShadow(this.textRenderer, lv, k, m, 16777215);
            }
         }

         m += 12;
      }

      context.getMatrices().pop();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR);
      context.drawTexture(VIGNETTE_TEXTURE, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
      super.render(context, mouseX, mouseY, delta);
   }

   public void removed() {
      this.client.getMusicTracker().stop(MusicType.CREDITS);
   }

   public MusicSound getMusic() {
      return MusicType.CREDITS;
   }

   static {
      SEPARATOR_LINE = Text.literal("============").formatted(Formatting.WHITE);
      OBFUSCATION_PLACEHOLDER = Formatting.WHITE + Formatting.OBFUSCATED + Formatting.GREEN + Formatting.AQUA;
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   private interface CreditsReader {
      void read(Reader reader) throws IOException;
   }
}
