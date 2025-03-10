package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.task.WorldCreationTask;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class RealmsCreateRealmScreen extends RealmsScreen {
   private static final Text WORLD_NAME_TEXT = Text.translatable("mco.configure.world.name");
   private static final Text WORLD_DESCRIPTION_TEXT = Text.translatable("mco.configure.world.description");
   private final RealmsServer server;
   private final RealmsMainScreen parent;
   private TextFieldWidget nameBox;
   private TextFieldWidget descriptionBox;
   private ButtonWidget createButton;

   public RealmsCreateRealmScreen(RealmsServer server, RealmsMainScreen parent) {
      super(Text.translatable("mco.selectServer.create"));
      this.server = server;
      this.parent = parent;
   }

   public void tick() {
      if (this.nameBox != null) {
         this.nameBox.tick();
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.tick();
      }

   }

   public void init() {
      this.createButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.create.world"), (button) -> {
         this.createWorld();
      }).dimensions(this.width / 2 - 100, this.height / 4 + 120 + 17, 97, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 + 5, this.height / 4 + 120 + 17, 95, 20).build());
      this.createButton.active = false;
      this.nameBox = new TextFieldWidget(this.client.textRenderer, this.width / 2 - 100, 65, 200, 20, (TextFieldWidget)null, Text.translatable("mco.configure.world.name"));
      this.addSelectableChild(this.nameBox);
      this.setInitialFocus(this.nameBox);
      this.descriptionBox = new TextFieldWidget(this.client.textRenderer, this.width / 2 - 100, 115, 200, 20, (TextFieldWidget)null, Text.translatable("mco.configure.world.description"));
      this.addSelectableChild(this.descriptionBox);
   }

   public boolean charTyped(char chr, int modifiers) {
      boolean bl = super.charTyped(chr, modifiers);
      this.createButton.active = this.valid();
      return bl;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         boolean bl = super.keyPressed(keyCode, scanCode, modifiers);
         this.createButton.active = this.valid();
         return bl;
      }
   }

   private void createWorld() {
      if (this.valid()) {
         RealmsResetWorldScreen lv = new RealmsResetWorldScreen(this.parent, this.server, Text.translatable("mco.selectServer.create"), Text.translatable("mco.create.world.subtitle"), 10526880, Text.translatable("mco.create.world.skip"), () -> {
            this.client.execute(() -> {
               this.client.setScreen(this.parent.newScreen());
            });
         }, () -> {
            this.client.setScreen(this.parent.newScreen());
         });
         lv.setResetTitle(Text.translatable("mco.create.world.reset.title"));
         this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent, new WorldCreationTask(this.server.id, this.nameBox.getText(), this.descriptionBox.getText(), lv)));
      }

   }

   private boolean valid() {
      return !this.nameBox.getText().trim().isEmpty();
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 11, 16777215);
      context.drawText(this.textRenderer, (Text)WORLD_NAME_TEXT, this.width / 2 - 100, 52, 10526880, false);
      context.drawText(this.textRenderer, (Text)WORLD_DESCRIPTION_TEXT, this.width / 2 - 100, 102, 10526880, false);
      if (this.nameBox != null) {
         this.nameBox.render(context, mouseX, mouseY, delta);
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.render(context, mouseX, mouseY, delta);
      }

      super.render(context, mouseX, mouseY, delta);
   }
}
