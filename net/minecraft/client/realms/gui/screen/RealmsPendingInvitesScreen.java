package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.realms.dto.PendingInvite;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsPendingInvitesScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   static final Identifier ACCEPT_ICON = new Identifier("realms", "textures/gui/realms/accept_icon.png");
   static final Identifier REJECT_ICON = new Identifier("realms", "textures/gui/realms/reject_icon.png");
   private static final Text NO_PENDING_TEXT = Text.translatable("mco.invites.nopending");
   static final Text ACCEPT_TEXT = Text.translatable("mco.invites.button.accept");
   static final Text REJECT_TEXT = Text.translatable("mco.invites.button.reject");
   private final Screen parent;
   @Nullable
   Text tooltip;
   boolean loaded;
   PendingInvitationSelectionList pendingInvitationSelectionList;
   int selectedInvite = -1;
   private ButtonWidget acceptButton;
   private ButtonWidget rejectButton;

   public RealmsPendingInvitesScreen(Screen parent, Text title) {
      super(title);
      this.parent = parent;
   }

   public void init() {
      this.pendingInvitationSelectionList = new PendingInvitationSelectionList();
      (new Thread("Realms-pending-invitations-fetcher") {
         public void run() {
            RealmsClient lv = RealmsClient.create();

            try {
               List list = lv.pendingInvites().pendingInvites;
               List list2 = (List)list.stream().map((invite) -> {
                  return RealmsPendingInvitesScreen.this.new PendingInvitationSelectionListEntry(invite);
               }).collect(Collectors.toList());
               RealmsPendingInvitesScreen.this.client.execute(() -> {
                  RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.replaceEntries(list2);
               });
            } catch (RealmsServiceException var7) {
               RealmsPendingInvitesScreen.LOGGER.error("Couldn't list invites");
            } finally {
               RealmsPendingInvitesScreen.this.loaded = true;
            }

         }
      }).start();
      this.addSelectableChild(this.pendingInvitationSelectionList);
      this.acceptButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.invites.button.accept"), (button) -> {
         this.accept(this.selectedInvite);
         this.selectedInvite = -1;
         this.updateButtonStates();
      }).dimensions(this.width / 2 - 174, this.height - 32, 100, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.client.setScreen(new RealmsMainScreen(this.parent));
      }).dimensions(this.width / 2 - 50, this.height - 32, 100, 20).build());
      this.rejectButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.invites.button.reject"), (button) -> {
         this.reject(this.selectedInvite);
         this.selectedInvite = -1;
         this.updateButtonStates();
      }).dimensions(this.width / 2 + 74, this.height - 32, 100, 20).build());
      this.updateButtonStates();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.client.setScreen(new RealmsMainScreen(this.parent));
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   void updateList(int slot) {
      this.pendingInvitationSelectionList.removeAtIndex(slot);
   }

   void reject(final int slot) {
      if (slot < this.pendingInvitationSelectionList.getEntryCount()) {
         (new Thread("Realms-reject-invitation") {
            public void run() {
               try {
                  RealmsClient lv = RealmsClient.create();
                  lv.rejectInvitation(((PendingInvitationSelectionListEntry)RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children().get(slot)).mPendingInvite.invitationId);
                  RealmsPendingInvitesScreen.this.client.execute(() -> {
                     RealmsPendingInvitesScreen.this.updateList(slot);
                  });
               } catch (RealmsServiceException var2) {
                  RealmsPendingInvitesScreen.LOGGER.error("Couldn't reject invite");
               }

            }
         }).start();
      }

   }

   void accept(final int slot) {
      if (slot < this.pendingInvitationSelectionList.getEntryCount()) {
         (new Thread("Realms-accept-invitation") {
            public void run() {
               try {
                  RealmsClient lv = RealmsClient.create();
                  lv.acceptInvitation(((PendingInvitationSelectionListEntry)RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children().get(slot)).mPendingInvite.invitationId);
                  RealmsPendingInvitesScreen.this.client.execute(() -> {
                     RealmsPendingInvitesScreen.this.updateList(slot);
                  });
               } catch (RealmsServiceException var2) {
                  RealmsPendingInvitesScreen.LOGGER.error("Couldn't accept invite");
               }

            }
         }).start();
      }

   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.tooltip = null;
      this.renderBackground(context);
      this.pendingInvitationSelectionList.render(context, mouseX, mouseY, delta);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 12, 16777215);
      if (this.tooltip != null) {
         this.renderMousehoverTooltip(context, this.tooltip, mouseX, mouseY);
      }

      if (this.pendingInvitationSelectionList.getEntryCount() == 0 && this.loaded) {
         context.drawCenteredTextWithShadow(this.textRenderer, NO_PENDING_TEXT, this.width / 2, this.height / 2 - 20, 16777215);
      }

      super.render(context, mouseX, mouseY, delta);
   }

   protected void renderMousehoverTooltip(DrawContext context, @Nullable Text tooltip, int mouseX, int mouseY) {
      if (tooltip != null) {
         int k = mouseX + 12;
         int l = mouseY - 12;
         int m = this.textRenderer.getWidth((StringVisitable)tooltip);
         context.fillGradient(k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
         context.drawTextWithShadow(this.textRenderer, tooltip, k, l, 16777215);
      }
   }

   void updateButtonStates() {
      this.acceptButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
      this.rejectButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
   }

   private boolean shouldAcceptAndRejectButtonBeVisible(int invite) {
      return invite != -1;
   }

   @Environment(EnvType.CLIENT)
   private class PendingInvitationSelectionList extends RealmsObjectSelectionList {
      public PendingInvitationSelectionList() {
         super(RealmsPendingInvitesScreen.this.width, RealmsPendingInvitesScreen.this.height, 32, RealmsPendingInvitesScreen.this.height - 40, 36);
      }

      public void removeAtIndex(int index) {
         this.remove(index);
      }

      public int getMaxPosition() {
         return this.getEntryCount() * 36;
      }

      public int getRowWidth() {
         return 260;
      }

      public void renderBackground(DrawContext context) {
         RealmsPendingInvitesScreen.this.renderBackground(context);
      }

      public void setSelected(int index) {
         super.setSelected(index);
         this.selectInviteListItem(index);
      }

      public void selectInviteListItem(int item) {
         RealmsPendingInvitesScreen.this.selectedInvite = item;
         RealmsPendingInvitesScreen.this.updateButtonStates();
      }

      public void setSelected(@Nullable PendingInvitationSelectionListEntry arg) {
         super.setSelected(arg);
         RealmsPendingInvitesScreen.this.selectedInvite = this.children().indexOf(arg);
         RealmsPendingInvitesScreen.this.updateButtonStates();
      }
   }

   @Environment(EnvType.CLIENT)
   private class PendingInvitationSelectionListEntry extends AlwaysSelectedEntryListWidget.Entry {
      private static final int field_32123 = 38;
      final PendingInvite mPendingInvite;
      private final List buttons;

      PendingInvitationSelectionListEntry(PendingInvite pendingInvite) {
         this.mPendingInvite = pendingInvite;
         this.buttons = Arrays.asList(new AcceptButton(), new RejectButton());
      }

      public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.renderPendingInvitationItem(context, this.mPendingInvite, x, y, mouseX, mouseY);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         RealmsAcceptRejectButton.handleClick(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, this, this.buttons, button, mouseX, mouseY);
         return true;
      }

      private void renderPendingInvitationItem(DrawContext context, PendingInvite invite, int x, int y, int mouseX, int mouseY) {
         context.drawText(RealmsPendingInvitesScreen.this.textRenderer, invite.worldName, x + 38, y + 1, 16777215, false);
         context.drawText(RealmsPendingInvitesScreen.this.textRenderer, invite.worldOwnerName, x + 38, y + 12, 7105644, false);
         context.drawText(RealmsPendingInvitesScreen.this.textRenderer, RealmsUtil.convertToAgePresentation(invite.date), x + 38, y + 24, 7105644, false);
         RealmsAcceptRejectButton.render(context, this.buttons, RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, x, y, mouseX, mouseY);
         RealmsUtil.drawPlayerHead(context, x, y, 32, invite.worldOwnerUuid);
      }

      public Text getNarration() {
         Text lv = ScreenTexts.joinLines(Text.literal(this.mPendingInvite.worldName), Text.literal(this.mPendingInvite.worldOwnerName), Text.literal(RealmsUtil.convertToAgePresentation(this.mPendingInvite.date)));
         return Text.translatable("narrator.select", lv);
      }

      @Environment(EnvType.CLIENT)
      class AcceptButton extends RealmsAcceptRejectButton {
         AcceptButton() {
            super(15, 15, 215, 5);
         }

         protected void render(DrawContext context, int x, int y, boolean showTooltip) {
            float f = showTooltip ? 19.0F : 0.0F;
            context.drawTexture(RealmsPendingInvitesScreen.ACCEPT_ICON, x, y, f, 0.0F, 18, 18, 37, 18);
            if (showTooltip) {
               RealmsPendingInvitesScreen.this.tooltip = RealmsPendingInvitesScreen.ACCEPT_TEXT;
            }

         }

         public void handleClick(int index) {
            RealmsPendingInvitesScreen.this.accept(index);
         }
      }

      @Environment(EnvType.CLIENT)
      class RejectButton extends RealmsAcceptRejectButton {
         RejectButton() {
            super(15, 15, 235, 5);
         }

         protected void render(DrawContext context, int x, int y, boolean showTooltip) {
            float f = showTooltip ? 19.0F : 0.0F;
            context.drawTexture(RealmsPendingInvitesScreen.REJECT_ICON, x, y, f, 0.0F, 18, 18, 37, 18);
            if (showTooltip) {
               RealmsPendingInvitesScreen.this.tooltip = RealmsPendingInvitesScreen.REJECT_TEXT;
            }

         }

         public void handleClick(int index) {
            RealmsPendingInvitesScreen.this.reject(index);
         }
      }
   }
}
