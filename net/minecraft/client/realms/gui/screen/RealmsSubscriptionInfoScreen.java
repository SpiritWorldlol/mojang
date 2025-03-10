package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.Subscription;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Urls;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSubscriptionInfoScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final Text SUBSCRIPTION_TITLE = Text.translatable("mco.configure.world.subscription.title");
   private static final Text SUBSCRIPTION_START_LABEL_TEXT = Text.translatable("mco.configure.world.subscription.start");
   private static final Text TIME_LEFT_LABEL_TEXT = Text.translatable("mco.configure.world.subscription.timeleft");
   private static final Text DAYS_LEFT_LABEL_TEXT = Text.translatable("mco.configure.world.subscription.recurring.daysleft");
   private static final Text EXPIRED_TEXT = Text.translatable("mco.configure.world.subscription.expired");
   private static final Text EXPIRES_IN_LESS_THAN_A_DAY_TEXT = Text.translatable("mco.configure.world.subscription.less_than_a_day");
   private static final Text MONTH_TEXT = Text.translatable("mco.configure.world.subscription.month");
   private static final Text MONTHS_TEXT = Text.translatable("mco.configure.world.subscription.months");
   private static final Text DAY_TEXT = Text.translatable("mco.configure.world.subscription.day");
   private static final Text DAYS_TEXT = Text.translatable("mco.configure.world.subscription.days");
   private static final Text UNKNOWN_TEXT = Text.translatable("mco.configure.world.subscription.unknown");
   private static final Text RECURRING_INFO_TEXT = Text.translatable("mco.configure.world.subscription.recurring.info");
   private final Screen parent;
   final RealmsServer serverData;
   final Screen mainScreen;
   private Text daysLeft;
   private Text startDate;
   @Nullable
   private Subscription.SubscriptionType type;

   public RealmsSubscriptionInfoScreen(Screen parent, RealmsServer serverData, Screen mainScreen) {
      super(NarratorManager.EMPTY);
      this.daysLeft = UNKNOWN_TEXT;
      this.startDate = UNKNOWN_TEXT;
      this.parent = parent;
      this.serverData = serverData;
      this.mainScreen = mainScreen;
   }

   public void init() {
      this.getSubscription(this.serverData.id);
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.subscription.extend"), (button) -> {
         String string = Urls.getExtendJavaRealmsUrl(this.serverData.remoteSubscriptionId, this.client.getSession().getUuid());
         this.client.keyboard.setClipboard(string);
         Util.getOperatingSystem().open(string);
      }).dimensions(this.width / 2 - 100, row(6), 200, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 100, row(12), 200, 20).build());
      if (this.serverData.expired) {
         this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.delete.button"), (button) -> {
            Text lv = Text.translatable("mco.configure.world.delete.question.line1");
            Text lv2 = Text.translatable("mco.configure.world.delete.question.line2");
            this.client.setScreen(new RealmsLongConfirmationScreen(this::onDeletionConfirmed, RealmsLongConfirmationScreen.Type.WARNING, lv, lv2, true));
         }).dimensions(this.width / 2 - 100, row(10), 200, 20).build());
      } else {
         this.addDrawableChild((new MultilineTextWidget(this.width / 2 - 100, row(8), RECURRING_INFO_TEXT, this.textRenderer)).setTextColor(10526880).setMaxWidth(200));
      }

   }

   public Text getNarratedTitle() {
      return ScreenTexts.joinLines(SUBSCRIPTION_TITLE, SUBSCRIPTION_START_LABEL_TEXT, this.startDate, TIME_LEFT_LABEL_TEXT, this.daysLeft);
   }

   private void onDeletionConfirmed(boolean delete) {
      if (delete) {
         (new Thread("Realms-delete-realm") {
            public void run() {
               try {
                  RealmsClient lv = RealmsClient.create();
                  lv.deleteWorld(RealmsSubscriptionInfoScreen.this.serverData.id);
               } catch (RealmsServiceException var2) {
                  RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world", var2);
               }

               RealmsSubscriptionInfoScreen.this.client.execute(() -> {
                  RealmsSubscriptionInfoScreen.this.client.setScreen(RealmsSubscriptionInfoScreen.this.mainScreen);
               });
            }
         }).start();
      }

      this.client.setScreen(this);
   }

   private void getSubscription(long worldId) {
      RealmsClient lv = RealmsClient.create();

      try {
         Subscription lv2 = lv.subscriptionFor(worldId);
         this.daysLeft = this.daysLeftPresentation(lv2.daysLeft);
         this.startDate = localPresentation(lv2.startDate);
         this.type = lv2.type;
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't get subscription");
         this.client.setScreen(new RealmsGenericErrorScreen(var5, this.parent));
      }

   }

   private static Text localPresentation(long time) {
      Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
      calendar.setTimeInMillis(time);
      return Text.literal(DateFormat.getDateTimeInstance().format(calendar.getTime()));
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      int k = this.width / 2 - 100;
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)SUBSCRIPTION_TITLE, this.width / 2, 17, 16777215);
      context.drawText(this.textRenderer, SUBSCRIPTION_START_LABEL_TEXT, k, row(0), 10526880, false);
      context.drawText(this.textRenderer, this.startDate, k, row(1), 16777215, false);
      if (this.type == Subscription.SubscriptionType.NORMAL) {
         context.drawText(this.textRenderer, TIME_LEFT_LABEL_TEXT, k, row(3), 10526880, false);
      } else if (this.type == Subscription.SubscriptionType.RECURRING) {
         context.drawText(this.textRenderer, DAYS_LEFT_LABEL_TEXT, k, row(3), 10526880, false);
      }

      context.drawText(this.textRenderer, this.daysLeft, k, row(4), 16777215, false);
      super.render(context, mouseX, mouseY, delta);
   }

   private Text daysLeftPresentation(int daysLeft) {
      if (daysLeft < 0 && this.serverData.expired) {
         return EXPIRED_TEXT;
      } else if (daysLeft <= 1) {
         return EXPIRES_IN_LESS_THAN_A_DAY_TEXT;
      } else {
         int j = daysLeft / 30;
         int k = daysLeft % 30;
         MutableText lv = Text.empty();
         if (j > 0) {
            lv.append(Integer.toString(j)).append(ScreenTexts.SPACE);
            if (j == 1) {
               lv.append(MONTH_TEXT);
            } else {
               lv.append(MONTHS_TEXT);
            }
         }

         if (k > 0) {
            if (j > 0) {
               lv.append(", ");
            }

            lv.append(Integer.toString(k)).append(ScreenTexts.SPACE);
            if (k == 1) {
               lv.append(DAY_TEXT);
            } else {
               lv.append(DAYS_TEXT);
            }
         }

         return lv;
      }
   }
}
