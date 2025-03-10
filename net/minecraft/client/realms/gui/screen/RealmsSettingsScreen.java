package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class RealmsSettingsScreen extends RealmsScreen {
   private static final int TEXT_FIELD_WIDTH = 212;
   private static final Text WORLD_NAME_TEXT = Text.translatable("mco.configure.world.name");
   private static final Text WORLD_DESCRIPTION_TEXT = Text.translatable("mco.configure.world.description");
   private final RealmsConfigureWorldScreen parent;
   private final RealmsServer serverData;
   private ButtonWidget doneButton;
   private TextFieldWidget descEdit;
   private TextFieldWidget nameEdit;

   public RealmsSettingsScreen(RealmsConfigureWorldScreen parent, RealmsServer serverData) {
      super(Text.translatable("mco.configure.world.settings.title"));
      this.parent = parent;
      this.serverData = serverData;
   }

   public void tick() {
      this.nameEdit.tick();
      this.descEdit.tick();
      this.doneButton.active = !this.nameEdit.getText().trim().isEmpty();
   }

   public void init() {
      int i = this.width / 2 - 106;
      this.doneButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.buttons.done"), (button) -> {
         this.save();
      }).dimensions(i - 2, row(12), 106, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 + 2, row(12), 106, 20).build());
      String string = this.serverData.state == RealmsServer.State.OPEN ? "mco.configure.world.buttons.close" : "mco.configure.world.buttons.open";
      ButtonWidget lv = ButtonWidget.builder(Text.translatable(string), (button) -> {
         if (this.serverData.state == RealmsServer.State.OPEN) {
            Text lv = Text.translatable("mco.configure.world.close.question.line1");
            Text lv2 = Text.translatable("mco.configure.world.close.question.line2");
            this.client.setScreen(new RealmsLongConfirmationScreen((confirmed) -> {
               if (confirmed) {
                  this.parent.closeTheWorld(this);
               } else {
                  this.client.setScreen(this);
               }

            }, RealmsLongConfirmationScreen.Type.INFO, lv, lv2, true));
         } else {
            this.parent.openTheWorld(false, this);
         }

      }).dimensions(this.width / 2 - 53, row(0), 106, 20).build();
      this.addDrawableChild(lv);
      this.nameEdit = new TextFieldWidget(this.client.textRenderer, i, row(4), 212, 20, (TextFieldWidget)null, Text.translatable("mco.configure.world.name"));
      this.nameEdit.setMaxLength(32);
      this.nameEdit.setText(this.serverData.getName());
      this.addSelectableChild(this.nameEdit);
      this.focusOn(this.nameEdit);
      this.descEdit = new TextFieldWidget(this.client.textRenderer, i, row(8), 212, 20, (TextFieldWidget)null, Text.translatable("mco.configure.world.description"));
      this.descEdit.setMaxLength(32);
      this.descEdit.setText(this.serverData.getDescription());
      this.addSelectableChild(this.descEdit);
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
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 17, 16777215);
      context.drawText(this.textRenderer, WORLD_NAME_TEXT, this.width / 2 - 106, row(3), 10526880, false);
      context.drawText(this.textRenderer, WORLD_DESCRIPTION_TEXT, this.width / 2 - 106, row(7), 10526880, false);
      this.nameEdit.render(context, mouseX, mouseY, delta);
      this.descEdit.render(context, mouseX, mouseY, delta);
      super.render(context, mouseX, mouseY, delta);
   }

   public void save() {
      this.parent.saveSettings(this.nameEdit.getText(), this.descEdit.getText());
   }
}
