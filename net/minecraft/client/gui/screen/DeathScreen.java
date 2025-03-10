package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class DeathScreen extends Screen {
   private int ticksSinceDeath;
   private final Text message;
   private final boolean isHardcore;
   private Text scoreText;
   private final List buttons = Lists.newArrayList();
   @Nullable
   private ButtonWidget titleScreenButton;

   public DeathScreen(@Nullable Text message, boolean isHardcore) {
      super(Text.translatable(isHardcore ? "deathScreen.title.hardcore" : "deathScreen.title"));
      this.message = message;
      this.isHardcore = isHardcore;
   }

   protected void init() {
      this.ticksSinceDeath = 0;
      this.buttons.clear();
      Text lv = this.isHardcore ? Text.translatable("deathScreen.spectate") : Text.translatable("deathScreen.respawn");
      this.buttons.add((ButtonWidget)this.addDrawableChild(ButtonWidget.builder(lv, (button) -> {
         this.client.player.requestRespawn();
         button.active = false;
      }).dimensions(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()));
      this.titleScreenButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("deathScreen.titleScreen"), (button) -> {
         this.client.getAbuseReportContext().tryShowDraftScreen(this.client, this, this::onTitleScreenButtonClicked, true);
      }).dimensions(this.width / 2 - 100, this.height / 4 + 96, 200, 20).build());
      this.buttons.add(this.titleScreenButton);
      this.setButtonsActive(false);
      this.scoreText = Text.translatable("deathScreen.score").append(": ").append((Text)Text.literal(Integer.toString(this.client.player.getScore())).formatted(Formatting.YELLOW));
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   private void onTitleScreenButtonClicked() {
      if (this.isHardcore) {
         this.quitLevel();
      } else {
         ConfirmScreen lv = new TitleScreenConfirmScreen((confirmed) -> {
            if (confirmed) {
               this.quitLevel();
            } else {
               this.client.player.requestRespawn();
               this.client.setScreen((Screen)null);
            }

         }, Text.translatable("deathScreen.quit.confirm"), ScreenTexts.EMPTY, Text.translatable("deathScreen.titleScreen"), Text.translatable("deathScreen.respawn"));
         this.client.setScreen(lv);
         lv.disableButtons(20);
      }
   }

   private void quitLevel() {
      if (this.client.world != null) {
         this.client.world.disconnect();
      }

      this.client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
      this.client.setScreen(new TitleScreen());
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      context.fillGradient(0, 0, this.width, this.height, 1615855616, -1602211792);
      context.getMatrices().push();
      context.getMatrices().scale(2.0F, 2.0F, 2.0F);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2 / 2, 30, 16777215);
      context.getMatrices().pop();
      if (this.message != null) {
         context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.message, this.width / 2, 85, 16777215);
      }

      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.scoreText, this.width / 2, 100, 16777215);
      if (this.message != null && mouseY > 85) {
         Objects.requireNonNull(this.textRenderer);
         if (mouseY < 85 + 9) {
            Style lv = this.getTextComponentUnderMouse(mouseX);
            context.drawHoverEvent(this.textRenderer, lv, mouseX, mouseY);
         }
      }

      super.render(context, mouseX, mouseY, delta);
      if (this.titleScreenButton != null && this.client.getAbuseReportContext().hasDraft()) {
         context.drawTexture(ClickableWidget.WIDGETS_TEXTURE, this.titleScreenButton.getX() + this.titleScreenButton.getWidth() - 17, this.titleScreenButton.getY() + 3, 182, 24, 15, 15);
      }

   }

   @Nullable
   private Style getTextComponentUnderMouse(int mouseX) {
      if (this.message == null) {
         return null;
      } else {
         int j = this.client.textRenderer.getWidth((StringVisitable)this.message);
         int k = this.width / 2 - j / 2;
         int l = this.width / 2 + j / 2;
         return mouseX >= k && mouseX <= l ? this.client.textRenderer.getTextHandler().getStyleAt((StringVisitable)this.message, mouseX - k) : null;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.message != null && mouseY > 85.0) {
         Objects.requireNonNull(this.textRenderer);
         if (mouseY < (double)(85 + 9)) {
            Style lv = this.getTextComponentUnderMouse((int)mouseX);
            if (lv != null && lv.getClickEvent() != null && lv.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
               this.handleTextClick(lv);
               return false;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean shouldPause() {
      return false;
   }

   public void tick() {
      super.tick();
      ++this.ticksSinceDeath;
      if (this.ticksSinceDeath == 20) {
         this.setButtonsActive(true);
      }

   }

   private void setButtonsActive(boolean active) {
      ButtonWidget lv;
      for(Iterator var2 = this.buttons.iterator(); var2.hasNext(); lv.active = active) {
         lv = (ButtonWidget)var2.next();
      }

   }

   @Environment(EnvType.CLIENT)
   public static class TitleScreenConfirmScreen extends ConfirmScreen {
      public TitleScreenConfirmScreen(BooleanConsumer booleanConsumer, Text arg, Text arg2, Text arg3, Text arg4) {
         super(booleanConsumer, arg, arg2, arg3, arg4);
      }
   }
}
