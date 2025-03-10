package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.FileDownload;
import net.minecraft.client.realms.SizeUnit;
import net.minecraft.client.realms.dto.WorldDownload;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsDownloadLatestWorldScreen extends RealmsScreen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ReentrantLock DOWNLOAD_LOCK = new ReentrantLock();
   private static final int field_41772 = 200;
   private static final int field_41769 = 80;
   private static final int field_41770 = 95;
   private static final int field_41771 = 1;
   private final Screen parent;
   private final WorldDownload worldDownload;
   private final Text downloadTitle;
   private final RateLimiter narrationRateLimiter;
   private ButtonWidget cancelButton;
   private final String worldName;
   private final DownloadStatus downloadStatus;
   @Nullable
   private volatile Text downloadError;
   private volatile Text status = Text.translatable("mco.download.preparing");
   @Nullable
   private volatile String progress;
   private volatile boolean cancelled;
   private volatile boolean showDots = true;
   private volatile boolean finished;
   private volatile boolean extracting;
   @Nullable
   private Long previousWrittenBytes;
   @Nullable
   private Long previousTimeSnapshot;
   private long bytesPerSecond;
   private int animTick;
   private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
   private int dotIndex;
   private boolean checked;
   private final BooleanConsumer onBack;

   public RealmsDownloadLatestWorldScreen(Screen parent, WorldDownload worldDownload, String worldName, BooleanConsumer onBack) {
      super(NarratorManager.EMPTY);
      this.onBack = onBack;
      this.parent = parent;
      this.worldName = worldName;
      this.worldDownload = worldDownload;
      this.downloadStatus = new DownloadStatus();
      this.downloadTitle = Text.translatable("mco.download.title");
      this.narrationRateLimiter = RateLimiter.create(0.10000000149011612);
   }

   public void init() {
      this.cancelButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.cancelled = true;
         this.backButtonClicked();
      }).dimensions((this.width - 200) / 2, this.height - 42, 200, 20).build());
      this.checkDownloadSize();
   }

   private void checkDownloadSize() {
      if (!this.finished) {
         if (!this.checked && this.getContentLength(this.worldDownload.downloadLink) >= 5368709120L) {
            Text lv = Text.translatable("mco.download.confirmation.line1", SizeUnit.getUserFriendlyString(5368709120L));
            Text lv2 = Text.translatable("mco.download.confirmation.line2");
            this.client.setScreen(new RealmsLongConfirmationScreen((confirmed) -> {
               this.checked = true;
               this.client.setScreen(this);
               this.downloadSave();
            }, RealmsLongConfirmationScreen.Type.WARNING, lv, lv2, false));
         } else {
            this.downloadSave();
         }

      }
   }

   private long getContentLength(String downloadLink) {
      FileDownload lv = new FileDownload();
      return lv.contentLength(downloadLink);
   }

   public void tick() {
      super.tick();
      ++this.animTick;
      if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
         Text lv = this.getNarration();
         this.client.getNarratorManager().narrate(lv);
      }

   }

   private Text getNarration() {
      List list = Lists.newArrayList();
      list.add(this.downloadTitle);
      list.add(this.status);
      if (this.progress != null) {
         list.add(Text.literal(this.progress + "%"));
         list.add(Text.literal(SizeUnit.getUserFriendlyString(this.bytesPerSecond) + "/s"));
      }

      if (this.downloadError != null) {
         list.add(this.downloadError);
      }

      return ScreenTexts.joinLines((Collection)list);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.cancelled = true;
         this.backButtonClicked();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private void backButtonClicked() {
      if (this.finished && this.onBack != null && this.downloadError == null) {
         this.onBack.accept(true);
      }

      this.client.setScreen(this.parent);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.downloadTitle, this.width / 2, 20, 16777215);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.status, this.width / 2, 50, 16777215);
      if (this.showDots) {
         this.drawDots(context);
      }

      if (this.downloadStatus.bytesWritten != 0L && !this.cancelled) {
         this.drawProgressBar(context);
         this.drawDownloadSpeed(context);
      }

      if (this.downloadError != null) {
         context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.downloadError, this.width / 2, 110, 16711680);
      }

      super.render(context, mouseX, mouseY, delta);
   }

   private void drawDots(DrawContext context) {
      int i = this.textRenderer.getWidth((StringVisitable)this.status);
      if (this.animTick % 10 == 0) {
         ++this.dotIndex;
      }

      context.drawText(this.textRenderer, (String)DOTS[this.dotIndex % DOTS.length], this.width / 2 + i / 2 + 5, 50, 16777215, false);
   }

   private void drawProgressBar(DrawContext context) {
      double d = Math.min((double)this.downloadStatus.bytesWritten / (double)this.downloadStatus.totalBytes, 1.0);
      this.progress = String.format(Locale.ROOT, "%.1f", d * 100.0);
      int i = (this.width - 200) / 2;
      int j = i + (int)Math.round(200.0 * d);
      context.fill(i - 1, 79, j + 1, 96, -2501934);
      context.fill(i, 80, j, 95, -8355712);
      context.drawCenteredTextWithShadow(this.textRenderer, (String)(this.progress + " %"), this.width / 2, 84, 16777215);
   }

   private void drawDownloadSpeed(DrawContext context) {
      if (this.animTick % 20 == 0) {
         if (this.previousWrittenBytes != null) {
            long l = Util.getMeasuringTimeMs() - this.previousTimeSnapshot;
            if (l == 0L) {
               l = 1L;
            }

            this.bytesPerSecond = 1000L * (this.downloadStatus.bytesWritten - this.previousWrittenBytes) / l;
            this.drawDownloadSpeed0(context, this.bytesPerSecond);
         }

         this.previousWrittenBytes = this.downloadStatus.bytesWritten;
         this.previousTimeSnapshot = Util.getMeasuringTimeMs();
      } else {
         this.drawDownloadSpeed0(context, this.bytesPerSecond);
      }

   }

   private void drawDownloadSpeed0(DrawContext context, long bytesPerSecond) {
      if (bytesPerSecond > 0L) {
         int i = this.textRenderer.getWidth(this.progress);
         String string = "(" + SizeUnit.getUserFriendlyString(bytesPerSecond) + "/s)";
         context.drawText(this.textRenderer, (String)string, this.width / 2 + i / 2 + 15, 84, 16777215, false);
      }

   }

   private void downloadSave() {
      (new Thread(() -> {
         try {
            if (!DOWNLOAD_LOCK.tryLock(1L, TimeUnit.SECONDS)) {
               this.status = Text.translatable("mco.download.failed");
               return;
            }

            if (!this.cancelled) {
               this.status = Text.translatable("mco.download.downloading", this.worldName);
               FileDownload lv = new FileDownload();
               lv.contentLength(this.worldDownload.downloadLink);
               lv.downloadWorld(this.worldDownload, this.worldName, this.downloadStatus, this.client.getLevelStorage());

               while(!lv.isFinished()) {
                  if (lv.isError()) {
                     lv.cancel();
                     this.downloadError = Text.translatable("mco.download.failed");
                     this.cancelButton.setMessage(ScreenTexts.DONE);
                     return;
                  }

                  if (lv.isExtracting()) {
                     if (!this.extracting) {
                        this.status = Text.translatable("mco.download.extracting");
                     }

                     this.extracting = true;
                  }

                  if (this.cancelled) {
                     lv.cancel();
                     this.downloadCancelled();
                     return;
                  }

                  try {
                     Thread.sleep(500L);
                  } catch (InterruptedException var8) {
                     LOGGER.error("Failed to check Realms backup download status");
                  }
               }

               this.finished = true;
               this.status = Text.translatable("mco.download.done");
               this.cancelButton.setMessage(ScreenTexts.DONE);
               return;
            }

            this.downloadCancelled();
         } catch (InterruptedException var9) {
            LOGGER.error("Could not acquire upload lock");
            return;
         } catch (Exception var10) {
            this.downloadError = Text.translatable("mco.download.failed");
            var10.printStackTrace();
            return;
         } finally {
            if (!DOWNLOAD_LOCK.isHeldByCurrentThread()) {
               return;
            }

            DOWNLOAD_LOCK.unlock();
            this.showDots = false;
            this.finished = true;
         }

      })).start();
   }

   private void downloadCancelled() {
      this.status = Text.translatable("mco.download.cancelled");
   }

   @Environment(EnvType.CLIENT)
   public static class DownloadStatus {
      public volatile long bytesWritten;
      public volatile long totalBytes;
   }
}
