package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsLabel;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.WorldTemplate;
import net.minecraft.client.realms.dto.WorldTemplatePaginatedList;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.client.realms.task.ResettingNormalWorldTask;
import net.minecraft.client.realms.task.ResettingWorldTemplateTask;
import net.minecraft.client.realms.task.SwitchSlotTask;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsResetWorldScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   private final Screen parent;
   private final RealmsServer serverData;
   private Text subtitle;
   private Text buttonTitle;
   private int subtitleColor;
   private static final Identifier SLOT_FRAME_TEXTURE = new Identifier("realms", "textures/gui/realms/slot_frame.png");
   private static final Identifier UPLOAD_TEXTURE = new Identifier("realms", "textures/gui/realms/upload.png");
   private static final Identifier ADVENTURE_TEXTURE = new Identifier("realms", "textures/gui/realms/adventure.png");
   private static final Identifier SURVIVAL_SPAWN_TEXTURE = new Identifier("realms", "textures/gui/realms/survival_spawn.png");
   private static final Identifier NEW_WORLD_TEXTURE = new Identifier("realms", "textures/gui/realms/new_world.png");
   private static final Identifier EXPERIENCE_TEXTURE = new Identifier("realms", "textures/gui/realms/experience.png");
   private static final Identifier INSPIRATION_TEXTURE = new Identifier("realms", "textures/gui/realms/inspiration.png");
   WorldTemplatePaginatedList normalWorldTemplates;
   WorldTemplatePaginatedList adventureWorldTemplates;
   WorldTemplatePaginatedList experienceWorldTemplates;
   WorldTemplatePaginatedList inspirationWorldTemplates;
   public int slot;
   private Text resetTitle;
   private final Runnable resetCallback;
   private final Runnable selectFileUploadCallback;

   public RealmsResetWorldScreen(Screen parent, RealmsServer server, Text title, Runnable resetCallback, Runnable selectFileUploadCallback) {
      super(title);
      this.subtitle = Text.translatable("mco.reset.world.warning");
      this.buttonTitle = ScreenTexts.CANCEL;
      this.subtitleColor = 16711680;
      this.slot = -1;
      this.resetTitle = Text.translatable("mco.reset.world.resetting.screen.title");
      this.parent = parent;
      this.serverData = server;
      this.resetCallback = resetCallback;
      this.selectFileUploadCallback = selectFileUploadCallback;
   }

   public RealmsResetWorldScreen(Screen parent, RealmsServer serverData, Runnable resetCallback, Runnable selectFileUploadCallback) {
      this(parent, serverData, Text.translatable("mco.reset.world.title"), resetCallback, selectFileUploadCallback);
   }

   public RealmsResetWorldScreen(Screen parent, RealmsServer server, Text title, Text subtitle, int subtitleColor, Text buttonTitle, Runnable resetCallback, Runnable selectFileUploadCallback) {
      this(parent, server, title, resetCallback, selectFileUploadCallback);
      this.subtitle = subtitle;
      this.subtitleColor = subtitleColor;
      this.buttonTitle = buttonTitle;
   }

   public void setSlot(int slot) {
      this.slot = slot;
   }

   public void setResetTitle(Text resetTitle) {
      this.resetTitle = resetTitle;
   }

   public void init() {
      this.addDrawableChild(ButtonWidget.builder(this.buttonTitle, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 40, row(14) - 10, 80, 20).build());
      (new Thread("Realms-reset-world-fetcher") {
         public void run() {
            RealmsClient lv = RealmsClient.create();

            try {
               WorldTemplatePaginatedList lv2 = lv.fetchWorldTemplates(1, 10, RealmsServer.WorldType.NORMAL);
               WorldTemplatePaginatedList lv3 = lv.fetchWorldTemplates(1, 10, RealmsServer.WorldType.ADVENTUREMAP);
               WorldTemplatePaginatedList lv4 = lv.fetchWorldTemplates(1, 10, RealmsServer.WorldType.EXPERIENCE);
               WorldTemplatePaginatedList lv5 = lv.fetchWorldTemplates(1, 10, RealmsServer.WorldType.INSPIRATION);
               RealmsResetWorldScreen.this.client.execute(() -> {
                  RealmsResetWorldScreen.this.normalWorldTemplates = lv2;
                  RealmsResetWorldScreen.this.adventureWorldTemplates = lv3;
                  RealmsResetWorldScreen.this.experienceWorldTemplates = lv4;
                  RealmsResetWorldScreen.this.inspirationWorldTemplates = lv5;
               });
            } catch (RealmsServiceException var6) {
               RealmsResetWorldScreen.LOGGER.error("Couldn't fetch templates in reset world", var6);
            }

         }
      }).start();
      this.addLabel(new RealmsLabel(this.subtitle, this.width / 2, 22, this.subtitleColor));
      this.addDrawableChild(new FrameButton(this.frame(1), row(0) + 10, Text.translatable("mco.reset.world.generate"), NEW_WORLD_TEXTURE, (button) -> {
         this.client.setScreen(new RealmsResetNormalWorldScreen(this::onResetNormalWorld, this.title));
      }));
      this.addDrawableChild(new FrameButton(this.frame(2), row(0) + 10, Text.translatable("mco.reset.world.upload"), UPLOAD_TEXTURE, (button) -> {
         this.client.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this.slot != -1 ? this.slot : this.serverData.activeSlot, this, this.selectFileUploadCallback));
      }));
      this.addDrawableChild(new FrameButton(this.frame(3), row(0) + 10, Text.translatable("mco.reset.world.template"), SURVIVAL_SPAWN_TEXTURE, (button) -> {
         this.client.setScreen(new RealmsSelectWorldTemplateScreen(Text.translatable("mco.reset.world.template"), this::onSelectWorldTemplate, RealmsServer.WorldType.NORMAL, this.normalWorldTemplates));
      }));
      this.addDrawableChild(new FrameButton(this.frame(1), row(6) + 20, Text.translatable("mco.reset.world.adventure"), ADVENTURE_TEXTURE, (button) -> {
         this.client.setScreen(new RealmsSelectWorldTemplateScreen(Text.translatable("mco.reset.world.adventure"), this::onSelectWorldTemplate, RealmsServer.WorldType.ADVENTUREMAP, this.adventureWorldTemplates));
      }));
      this.addDrawableChild(new FrameButton(this.frame(2), row(6) + 20, Text.translatable("mco.reset.world.experience"), EXPERIENCE_TEXTURE, (button) -> {
         this.client.setScreen(new RealmsSelectWorldTemplateScreen(Text.translatable("mco.reset.world.experience"), this::onSelectWorldTemplate, RealmsServer.WorldType.EXPERIENCE, this.experienceWorldTemplates));
      }));
      this.addDrawableChild(new FrameButton(this.frame(3), row(6) + 20, Text.translatable("mco.reset.world.inspiration"), INSPIRATION_TEXTURE, (button) -> {
         this.client.setScreen(new RealmsSelectWorldTemplateScreen(Text.translatable("mco.reset.world.inspiration"), this::onSelectWorldTemplate, RealmsServer.WorldType.INSPIRATION, this.inspirationWorldTemplates));
      }));
   }

   public Text getNarratedTitle() {
      return ScreenTexts.joinSentences(this.getTitle(), this.narrateLabels());
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private int frame(int i) {
      return this.width / 2 - 130 + (i - 1) * 100;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 7, 16777215);
      super.render(context, mouseX, mouseY, delta);
   }

   void drawFrame(DrawContext context, int x, int y, Text text, Identifier texture, boolean hovered, boolean mouseOver) {
      if (hovered) {
         context.setShaderColor(0.56F, 0.56F, 0.56F, 1.0F);
      }

      context.drawTexture(texture, x + 2, y + 14, 0.0F, 0.0F, 56, 56, 56, 56);
      context.drawTexture(SLOT_FRAME_TEXTURE, x, y + 12, 0.0F, 0.0F, 60, 60, 60, 60);
      int k = hovered ? 10526880 : 16777215;
      context.drawCenteredTextWithShadow(this.textRenderer, text, x + 30, y, k);
      context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void executeLongRunningTask(LongRunningTask task) {
      this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent, task));
   }

   public void switchSlot(Runnable callback) {
      this.executeLongRunningTask(new SwitchSlotTask(this.serverData.id, this.slot, () -> {
         this.client.execute(callback);
      }));
   }

   private void onSelectWorldTemplate(@Nullable WorldTemplate template) {
      this.client.setScreen(this);
      if (template != null) {
         this.switchSlotAndResetWorld(() -> {
            this.executeLongRunningTask(new ResettingWorldTemplateTask(template, this.serverData.id, this.resetTitle, this.resetCallback));
         });
      }

   }

   private void onResetNormalWorld(@Nullable ResetWorldInfo info) {
      this.client.setScreen(this);
      if (info != null) {
         this.switchSlotAndResetWorld(() -> {
            this.executeLongRunningTask(new ResettingNormalWorldTask(info, this.serverData.id, this.resetTitle, this.resetCallback));
         });
      }

   }

   private void switchSlotAndResetWorld(Runnable resetter) {
      if (this.slot == -1) {
         resetter.run();
      } else {
         this.switchSlot(resetter);
      }

   }

   @Environment(EnvType.CLIENT)
   class FrameButton extends ButtonWidget {
      private final Identifier image;

      public FrameButton(int x, int y, Text message, Identifier image, ButtonWidget.PressAction onPress) {
         super(x, y, 60, 72, message, onPress, DEFAULT_NARRATION_SUPPLIER);
         this.image = image;
      }

      public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
         RealmsResetWorldScreen.this.drawFrame(context, this.getX(), this.getY(), this.getMessage(), this.image, this.isSelected(), this.isMouseOver((double)mouseX, (double)mouseY));
      }
   }
}
