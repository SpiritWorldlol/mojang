package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.task.RealmsGetServerDetailsTask;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsTermsScreen extends RealmsScreen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Text TITLE = Text.translatable("mco.terms.title");
   private static final Text SENTENCE_ONE_TEXT = Text.translatable("mco.terms.sentence.1");
   private static final Text SENTENCE_TWO_TEXT;
   private final Screen parent;
   private final RealmsMainScreen mainScreen;
   private final RealmsServer realmsServer;
   private boolean onLink;

   public RealmsTermsScreen(Screen parent, RealmsMainScreen mainScreen, RealmsServer realmsServer) {
      super(TITLE);
      this.parent = parent;
      this.mainScreen = mainScreen;
      this.realmsServer = realmsServer;
   }

   public void init() {
      int i = this.width / 4 - 2;
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.terms.buttons.agree"), (button) -> {
         this.agreedToTos();
      }).dimensions(this.width / 4, row(12), i, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.terms.buttons.disagree"), (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 + 4, row(12), i, 20).build());
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private void agreedToTos() {
      RealmsClient lv = RealmsClient.create();

      try {
         lv.agreeToTos();
         this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent, new RealmsGetServerDetailsTask(this.mainScreen, this.parent, this.realmsServer, new ReentrantLock())));
      } catch (RealmsServiceException var3) {
         LOGGER.error("Couldn't agree to TOS");
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.onLink) {
         this.client.keyboard.setClipboard("https://aka.ms/MinecraftRealmsTerms");
         Util.getOperatingSystem().open("https://aka.ms/MinecraftRealmsTerms");
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public Text getNarratedTitle() {
      return ScreenTexts.joinSentences(super.getNarratedTitle(), SENTENCE_ONE_TEXT).append(ScreenTexts.SPACE).append(SENTENCE_TWO_TEXT);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 17, 16777215);
      context.drawText(this.textRenderer, SENTENCE_ONE_TEXT, this.width / 2 - 120, row(5), 16777215, false);
      int k = this.textRenderer.getWidth((StringVisitable)SENTENCE_ONE_TEXT);
      int l = this.width / 2 - 121 + k;
      int m = row(5);
      int n = l + this.textRenderer.getWidth((StringVisitable)SENTENCE_TWO_TEXT) + 1;
      int var10000 = m + 1;
      Objects.requireNonNull(this.textRenderer);
      int o = var10000 + 9;
      this.onLink = l <= mouseX && mouseX <= n && m <= mouseY && mouseY <= o;
      context.drawText(this.textRenderer, SENTENCE_TWO_TEXT, this.width / 2 - 120 + k, row(5), this.onLink ? 7107012 : 3368635, false);
      super.render(context, mouseX, mouseY, delta);
   }

   static {
      SENTENCE_TWO_TEXT = ScreenTexts.space().append((Text)Text.translatable("mco.terms.sentence.2").fillStyle(Style.EMPTY.withUnderline(true)));
   }
}
