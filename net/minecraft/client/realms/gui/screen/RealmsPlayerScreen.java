package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.realms.dto.Ops;
import net.minecraft.client.realms.dto.PlayerInfo;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsPlayerScreen extends RealmsScreen {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final Identifier OP_ICON = new Identifier("realms", "textures/gui/realms/op_icon.png");
   static final Identifier USER_ICON = new Identifier("realms", "textures/gui/realms/user_icon.png");
   static final Identifier CROSS_PLAYER_ICON = new Identifier("realms", "textures/gui/realms/cross_player_icon.png");
   private static final Identifier OPTIONS_BACKGROUND = new Identifier("minecraft", "textures/gui/options_background.png");
   static final Text NORMAL_TOOLTIP = Text.translatable("mco.configure.world.invites.normal.tooltip");
   static final Text OPERATOR_TOOLTIP = Text.translatable("mco.configure.world.invites.ops.tooltip");
   static final Text REMOVE_TOOLTIP = Text.translatable("mco.configure.world.invites.remove.tooltip");
   private static final Text INVITED_TEXT = Text.translatable("mco.configure.world.invited");
   private static final int field_44530 = -1;
   private final RealmsConfigureWorldScreen parent;
   final RealmsServer serverData;
   InvitedObjectSelectionList invitedObjectSelectionList;
   int column1_x;
   int column_width;
   private ButtonWidget removeButton;
   private ButtonWidget opdeopButton;
   int player = -1;
   private boolean stateChanged;

   public RealmsPlayerScreen(RealmsConfigureWorldScreen parent, RealmsServer serverData) {
      super(Text.translatable("mco.configure.world.players.title"));
      this.parent = parent;
      this.serverData = serverData;
   }

   public void init() {
      this.column1_x = this.width / 2 - 160;
      this.column_width = 150;
      int i = this.width / 2 + 12;
      this.invitedObjectSelectionList = new InvitedObjectSelectionList();
      this.invitedObjectSelectionList.setLeftPos(this.column1_x);
      this.addSelectableChild(this.invitedObjectSelectionList);
      Iterator var2 = this.serverData.players.iterator();

      while(var2.hasNext()) {
         PlayerInfo lv = (PlayerInfo)var2.next();
         this.invitedObjectSelectionList.addEntry(lv);
      }

      this.player = -1;
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.buttons.invite"), (button) -> {
         this.client.setScreen(new RealmsInviteScreen(this.parent, this, this.serverData));
      }).dimensions(i, row(1), this.column_width + 10, 20).build());
      this.removeButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.invites.remove.tooltip"), (button) -> {
         this.uninvite(this.player);
      }).dimensions(i, row(7), this.column_width + 10, 20).build());
      this.opdeopButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.configure.world.invites.ops.tooltip"), (button) -> {
         if (((PlayerInfo)this.serverData.players.get(this.player)).isOperator()) {
            this.deop(this.player);
         } else {
            this.op(this.player);
         }

      }).dimensions(i, row(9), this.column_width + 10, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
         this.backButtonClicked();
      }).dimensions(i + this.column_width / 2 + 2, row(12), this.column_width / 2 + 10 - 2, 20).build());
      this.updateButtonStates();
   }

   void updateButtonStates() {
      this.removeButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.player);
      this.opdeopButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.player);
      this.invitedObjectSelectionList.updateButtonStates();
   }

   private boolean shouldRemoveAndOpdeopButtonBeVisible(int player) {
      return player != -1;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.backButtonClicked();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private void backButtonClicked() {
      if (this.stateChanged) {
         this.client.setScreen(this.parent.getNewScreen());
      } else {
         this.client.setScreen(this.parent);
      }

   }

   void op(int index) {
      RealmsClient lv = RealmsClient.create();
      String string = ((PlayerInfo)this.serverData.players.get(index)).getUuid();

      try {
         this.updateOps(lv.op(this.serverData.id, string));
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't op the user");
      }

      this.updateButtonStates();
   }

   void deop(int index) {
      RealmsClient lv = RealmsClient.create();
      String string = ((PlayerInfo)this.serverData.players.get(index)).getUuid();

      try {
         this.updateOps(lv.deop(this.serverData.id, string));
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't deop the user");
      }

      this.updateButtonStates();
   }

   private void updateOps(Ops ops) {
      Iterator var2 = this.serverData.players.iterator();

      while(var2.hasNext()) {
         PlayerInfo lv = (PlayerInfo)var2.next();
         lv.setOperator(ops.ops.contains(lv.getName()));
      }

   }

   void uninvite(int index) {
      this.updateButtonStates();
      if (index >= 0 && index < this.serverData.players.size()) {
         PlayerInfo lv = (PlayerInfo)this.serverData.players.get(index);
         RealmsConfirmScreen lv2 = new RealmsConfirmScreen((confirmed) -> {
            if (confirmed) {
               RealmsClient lvx = RealmsClient.create();

               try {
                  lvx.uninvite(this.serverData.id, lv.getUuid());
               } catch (RealmsServiceException var5) {
                  LOGGER.error("Couldn't uninvite user");
               }

               this.serverData.players.remove(this.player);
               this.player = -1;
               this.updateButtonStates();
            }

            this.stateChanged = true;
            this.client.setScreen(this);
         }, Text.literal("Question"), Text.translatable("mco.configure.world.uninvite.question").append(" '").append(lv.getName()).append("' ?"));
         this.client.setScreen(lv2);
      }

   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      this.invitedObjectSelectionList.render(context, mouseX, mouseY, delta);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 17, 16777215);
      int k = row(12) + 20;
      context.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
      context.drawTexture(OPTIONS_BACKGROUND, 0, k, 0.0F, 0.0F, this.width, this.height - k, 32, 32);
      context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.serverData.players != null) {
         context.drawText(this.textRenderer, (Text)Text.empty().append(INVITED_TEXT).append(" (").append(Integer.toString(this.serverData.players.size())).append(")"), this.column1_x, row(0), 10526880, false);
      } else {
         context.drawText(this.textRenderer, INVITED_TEXT, this.column1_x, row(0), 10526880, false);
      }

      super.render(context, mouseX, mouseY, delta);
   }

   @Environment(EnvType.CLIENT)
   private class InvitedObjectSelectionList extends RealmsObjectSelectionList {
      public InvitedObjectSelectionList() {
         super(RealmsPlayerScreen.this.column_width + 10, RealmsPlayerScreen.row(12) + 20, RealmsPlayerScreen.row(1), RealmsPlayerScreen.row(12) + 20, 13);
      }

      public void updateButtonStates() {
         if (RealmsPlayerScreen.this.player != -1) {
            ((InvitedObjectSelectionListEntry)this.getEntry(RealmsPlayerScreen.this.player)).updateButtonStates();
         }

      }

      public void addEntry(PlayerInfo playerInfo) {
         this.addEntry(RealmsPlayerScreen.this.new InvitedObjectSelectionListEntry(playerInfo));
      }

      public int getRowWidth() {
         return (int)((double)this.width * 1.0);
      }

      public void setSelected(int index) {
         super.setSelected(index);
         this.selectInviteListItem(index);
      }

      public void selectInviteListItem(int item) {
         RealmsPlayerScreen.this.player = item;
         RealmsPlayerScreen.this.updateButtonStates();
      }

      public void setSelected(@Nullable InvitedObjectSelectionListEntry arg) {
         super.setSelected(arg);
         RealmsPlayerScreen.this.player = this.children().indexOf(arg);
         RealmsPlayerScreen.this.updateButtonStates();
      }

      public void renderBackground(DrawContext context) {
         RealmsPlayerScreen.this.renderBackground(context);
      }

      public int getScrollbarPositionX() {
         return RealmsPlayerScreen.this.column1_x + this.width - 5;
      }

      public int getMaxPosition() {
         return this.getEntryCount() * 13;
      }
   }

   @Environment(EnvType.CLIENT)
   private class InvitedObjectSelectionListEntry extends AlwaysSelectedEntryListWidget.Entry {
      private static final int field_44531 = 3;
      private static final int field_44532 = 1;
      private static final int field_44533 = 8;
      private static final int field_44534 = 7;
      private final PlayerInfo playerInfo;
      private final List buttons = new ArrayList();
      private final TexturedButtonWidget uninviteButton;
      private final TexturedButtonWidget opButton;
      private final TexturedButtonWidget deopButton;

      public InvitedObjectSelectionListEntry(PlayerInfo playerInfo) {
         this.playerInfo = playerInfo;
         int i = RealmsPlayerScreen.this.serverData.players.indexOf(this.playerInfo);
         int j = RealmsPlayerScreen.this.invitedObjectSelectionList.getRowRight() - 16 - 9;
         int k = RealmsPlayerScreen.this.invitedObjectSelectionList.getRowTop(i) + 1;
         this.uninviteButton = new TexturedButtonWidget(j, k, 8, 7, 0, 0, 7, RealmsPlayerScreen.CROSS_PLAYER_ICON, 8, 14, (button) -> {
            RealmsPlayerScreen.this.uninvite(i);
         });
         this.uninviteButton.setTooltip(Tooltip.of(RealmsPlayerScreen.REMOVE_TOOLTIP));
         this.buttons.add(this.uninviteButton);
         j += 11;
         this.opButton = new TexturedButtonWidget(j, k, 8, 7, 0, 0, 7, RealmsPlayerScreen.USER_ICON, 8, 14, (button) -> {
            RealmsPlayerScreen.this.op(i);
         });
         this.opButton.setTooltip(Tooltip.of(RealmsPlayerScreen.NORMAL_TOOLTIP));
         this.buttons.add(this.opButton);
         this.deopButton = new TexturedButtonWidget(j, k, 8, 7, 0, 0, 7, RealmsPlayerScreen.OP_ICON, 8, 14, (button) -> {
            RealmsPlayerScreen.this.deop(i);
         });
         this.deopButton.setTooltip(Tooltip.of(RealmsPlayerScreen.OPERATOR_TOOLTIP));
         this.buttons.add(this.deopButton);
         this.updateButtonStates();
      }

      public void updateButtonStates() {
         this.opButton.visible = !this.playerInfo.isOperator();
         this.deopButton.visible = !this.opButton.visible;
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (!this.opButton.mouseClicked(mouseX, mouseY, button)) {
            this.deopButton.mouseClicked(mouseX, mouseY, button);
         }

         this.uninviteButton.mouseClicked(mouseX, mouseY, button);
         return true;
      }

      public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         int p;
         if (!this.playerInfo.isAccepted()) {
            p = 10526880;
         } else if (this.playerInfo.isOnline()) {
            p = 8388479;
         } else {
            p = 16777215;
         }

         RealmsUtil.drawPlayerHead(context, RealmsPlayerScreen.this.column1_x + 2 + 2, y + 1, 8, this.playerInfo.getUuid());
         context.drawText(RealmsPlayerScreen.this.textRenderer, this.playerInfo.getName(), RealmsPlayerScreen.this.column1_x + 3 + 12, y + 1, p, false);
         this.buttons.forEach((button) -> {
            button.setY(y + 1);
            button.render(context, mouseX, mouseY, tickDelta);
         });
      }

      public Text getNarration() {
         return Text.translatable("narrator.select", this.playerInfo.getName());
      }
   }
}
