package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import java.time.Duration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.RepeatedNarrator;
import net.minecraft.client.realms.exception.RealmsDefaultUncaughtExceptionHandler;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.client.realms.util.Errable;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsLongRunningMcoTaskScreen extends RealmsScreen implements Errable {
   private static final RepeatedNarrator NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Screen parent;
   private volatile Text title;
   @Nullable
   private volatile Text errorMessage;
   private volatile boolean aborted;
   private int animTicks;
   private final LongRunningTask task;
   private final int buttonLength;
   private ButtonWidget cancelButton;
   public static final String[] SYMBOLS = new String[]{"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄", "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ _ _ ▃ ▄ ▅ ▆ ▇ █", "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄", "▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _", "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _", "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "█ ▇ ▆ ▅ ▄ ▃ _ _ _ _ _", "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _", "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _"};

   public RealmsLongRunningMcoTaskScreen(Screen parent, LongRunningTask task) {
      super(NarratorManager.EMPTY);
      this.title = ScreenTexts.EMPTY;
      this.buttonLength = 212;
      this.parent = parent;
      this.task = task;
      task.setScreen(this);
      Thread thread = new Thread(task, "Realms-long-running-task");
      thread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
      thread.start();
   }

   public void tick() {
      super.tick();
      NARRATOR.narrate(this.client.getNarratorManager(), this.title);
      ++this.animTicks;
      this.task.tick();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.cancelOrBackButtonClicked();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void init() {
      this.task.init();
      this.cancelButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.cancelOrBackButtonClicked();
      }).dimensions(this.width / 2 - 106, row(12), 212, 20).build());
   }

   private void cancelOrBackButtonClicked() {
      this.aborted = true;
      this.task.abortTask();
      this.client.setScreen(this.parent);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, row(3), 16777215);
      Text lv = this.errorMessage;
      if (lv == null) {
         context.drawCenteredTextWithShadow(this.textRenderer, SYMBOLS[this.animTicks % SYMBOLS.length], this.width / 2, row(8), 8421504);
      } else {
         context.drawCenteredTextWithShadow(this.textRenderer, lv, this.width / 2, row(8), 16711680);
      }

      super.render(context, mouseX, mouseY, delta);
   }

   public void error(Text errorMessage) {
      this.errorMessage = errorMessage;
      this.client.getNarratorManager().narrate(errorMessage);
      this.client.execute(() -> {
         this.remove(this.cancelButton);
         this.cancelButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
            this.cancelOrBackButtonClicked();
         }).dimensions(this.width / 2 - 106, this.height / 4 + 120 + 12, 200, 20).build());
      });
   }

   public void setTitle(Text title) {
      this.title = title;
   }

   public boolean aborted() {
      return this.aborted;
   }
}
