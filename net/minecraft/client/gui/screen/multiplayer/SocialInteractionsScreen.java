package net.minecraft.client.gui.screen.multiplayer;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SocialInteractionsScreen extends Screen {
   protected static final Identifier SOCIAL_INTERACTIONS_TEXTURE = new Identifier("textures/gui/social_interactions.png");
   private static final Text ALL_TAB_TITLE = Text.translatable("gui.socialInteractions.tab_all");
   private static final Text HIDDEN_TAB_TITLE = Text.translatable("gui.socialInteractions.tab_hidden");
   private static final Text BLOCKED_TAB_TITLE = Text.translatable("gui.socialInteractions.tab_blocked");
   private static final Text SELECTED_ALL_TAB_TITLE;
   private static final Text SELECTED_HIDDEN_TAB_TITLE;
   private static final Text SELECTED_BLOCKED_TAB_TITLE;
   private static final Text SEARCH_TEXT;
   static final Text EMPTY_SEARCH_TEXT;
   private static final Text EMPTY_HIDDEN_TEXT;
   private static final Text EMPTY_BLOCKED_TEXT;
   private static final Text BLOCKING_TEXT;
   private static final int field_32424 = 8;
   private static final int field_32426 = 236;
   private static final int field_32427 = 16;
   private static final int field_32428 = 64;
   public static final int field_32433 = 72;
   public static final int field_32432 = 88;
   private static final int field_32429 = 238;
   private static final int field_32430 = 20;
   private static final int field_32431 = 36;
   SocialInteractionsPlayerListWidget playerList;
   TextFieldWidget searchBox;
   private String currentSearch = "";
   private Tab currentTab;
   private ButtonWidget allTabButton;
   private ButtonWidget hiddenTabButton;
   private ButtonWidget blockedTabButton;
   private ButtonWidget blockingButton;
   @Nullable
   private Text serverLabel;
   private int playerCount;
   private boolean initialized;

   public SocialInteractionsScreen() {
      super(Text.translatable("gui.socialInteractions.title"));
      this.currentTab = SocialInteractionsScreen.Tab.ALL;
      this.updateServerLabel(MinecraftClient.getInstance());
   }

   private int getScreenHeight() {
      return Math.max(52, this.height - 128 - 16);
   }

   private int getPlayerListBottom() {
      return 80 + this.getScreenHeight() - 8;
   }

   private int getSearchBoxX() {
      return (this.width - 238) / 2;
   }

   public Text getNarratedTitle() {
      return (Text)(this.serverLabel != null ? ScreenTexts.joinSentences(super.getNarratedTitle(), this.serverLabel) : super.getNarratedTitle());
   }

   public void tick() {
      super.tick();
      this.searchBox.tick();
   }

   protected void init() {
      if (this.initialized) {
         this.playerList.updateSize(this.width, this.height, 88, this.getPlayerListBottom());
      } else {
         this.playerList = new SocialInteractionsPlayerListWidget(this, this.client, this.width, this.height, 88, this.getPlayerListBottom(), 36);
      }

      int i = this.playerList.getRowWidth() / 3;
      int j = this.playerList.getRowLeft();
      int k = this.playerList.getRowRight();
      int l = this.textRenderer.getWidth((StringVisitable)BLOCKING_TEXT) + 40;
      int m = 64 + this.getScreenHeight();
      int n = (this.width - l) / 2 + 3;
      this.allTabButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(ALL_TAB_TITLE, (button) -> {
         this.setCurrentTab(SocialInteractionsScreen.Tab.ALL);
      }).dimensions(j, 45, i, 20).build());
      this.hiddenTabButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(HIDDEN_TAB_TITLE, (button) -> {
         this.setCurrentTab(SocialInteractionsScreen.Tab.HIDDEN);
      }).dimensions((j + k - i) / 2 + 1, 45, i, 20).build());
      this.blockedTabButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(BLOCKED_TAB_TITLE, (button) -> {
         this.setCurrentTab(SocialInteractionsScreen.Tab.BLOCKED);
      }).dimensions(k - i + 1, 45, i, 20).build());
      String string = this.searchBox != null ? this.searchBox.getText() : "";
      this.searchBox = new TextFieldWidget(this.textRenderer, this.getSearchBoxX() + 29, 75, 198, 13, SEARCH_TEXT) {
         protected MutableText getNarrationMessage() {
            return !SocialInteractionsScreen.this.searchBox.getText().isEmpty() && SocialInteractionsScreen.this.playerList.isEmpty() ? super.getNarrationMessage().append(", ").append(SocialInteractionsScreen.EMPTY_SEARCH_TEXT) : super.getNarrationMessage();
         }
      };
      this.searchBox.setMaxLength(16);
      this.searchBox.setVisible(true);
      this.searchBox.setEditableColor(16777215);
      this.searchBox.setText(string);
      this.searchBox.setPlaceholder(SEARCH_TEXT);
      this.searchBox.setChangedListener(this::onSearchChange);
      this.addSelectableChild(this.searchBox);
      this.addSelectableChild(this.playerList);
      this.blockingButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(BLOCKING_TEXT, (button) -> {
         this.client.setScreen(new ConfirmLinkScreen((confirmed) -> {
            if (confirmed) {
               Util.getOperatingSystem().open("https://aka.ms/javablocking");
            }

            this.client.setScreen(this);
         }, "https://aka.ms/javablocking", true));
      }).dimensions(n, m, l, 20).build());
      this.initialized = true;
      this.setCurrentTab(this.currentTab);
   }

   private void setCurrentTab(Tab currentTab) {
      this.currentTab = currentTab;
      this.allTabButton.setMessage(ALL_TAB_TITLE);
      this.hiddenTabButton.setMessage(HIDDEN_TAB_TITLE);
      this.blockedTabButton.setMessage(BLOCKED_TAB_TITLE);
      boolean bl = false;
      switch (currentTab) {
         case ALL:
            this.allTabButton.setMessage(SELECTED_ALL_TAB_TITLE);
            Collection collection = this.client.player.networkHandler.getPlayerUuids();
            this.playerList.update(collection, this.playerList.getScrollAmount(), true);
            break;
         case HIDDEN:
            this.hiddenTabButton.setMessage(SELECTED_HIDDEN_TAB_TITLE);
            Set set = this.client.getSocialInteractionsManager().getHiddenPlayers();
            bl = set.isEmpty();
            this.playerList.update(set, this.playerList.getScrollAmount(), false);
            break;
         case BLOCKED:
            this.blockedTabButton.setMessage(SELECTED_BLOCKED_TAB_TITLE);
            SocialInteractionsManager lv = this.client.getSocialInteractionsManager();
            Stream var10000 = this.client.player.networkHandler.getPlayerUuids().stream();
            Objects.requireNonNull(lv);
            Set set2 = (Set)var10000.filter(lv::isPlayerBlocked).collect(Collectors.toSet());
            bl = set2.isEmpty();
            this.playerList.update(set2, this.playerList.getScrollAmount(), false);
      }

      NarratorManager lv2 = this.client.getNarratorManager();
      if (!this.searchBox.getText().isEmpty() && this.playerList.isEmpty() && !this.searchBox.isFocused()) {
         lv2.narrate(EMPTY_SEARCH_TEXT);
      } else if (bl) {
         if (currentTab == SocialInteractionsScreen.Tab.HIDDEN) {
            lv2.narrate(EMPTY_HIDDEN_TEXT);
         } else if (currentTab == SocialInteractionsScreen.Tab.BLOCKED) {
            lv2.narrate(EMPTY_BLOCKED_TEXT);
         }
      }

   }

   public void renderBackground(DrawContext context) {
      int i = this.getSearchBoxX() + 3;
      super.renderBackground(context);
      context.drawNineSlicedTexture(SOCIAL_INTERACTIONS_TEXTURE, i, 64, 236, this.getScreenHeight() + 16, 8, 236, 34, 1, 1);
      context.drawTexture(SOCIAL_INTERACTIONS_TEXTURE, i + 10, 76, 243, 1, 12, 12);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.updateServerLabel(this.client);
      this.renderBackground(context);
      if (this.serverLabel != null) {
         context.drawTextWithShadow(this.client.textRenderer, (Text)this.serverLabel, this.getSearchBoxX() + 8, 35, -1);
      }

      if (!this.playerList.isEmpty()) {
         this.playerList.render(context, mouseX, mouseY, delta);
      } else if (!this.searchBox.getText().isEmpty()) {
         context.drawCenteredTextWithShadow(this.client.textRenderer, (Text)EMPTY_SEARCH_TEXT, this.width / 2, (72 + this.getPlayerListBottom()) / 2, -1);
      } else if (this.currentTab == SocialInteractionsScreen.Tab.HIDDEN) {
         context.drawCenteredTextWithShadow(this.client.textRenderer, (Text)EMPTY_HIDDEN_TEXT, this.width / 2, (72 + this.getPlayerListBottom()) / 2, -1);
      } else if (this.currentTab == SocialInteractionsScreen.Tab.BLOCKED) {
         context.drawCenteredTextWithShadow(this.client.textRenderer, (Text)EMPTY_BLOCKED_TEXT, this.width / 2, (72 + this.getPlayerListBottom()) / 2, -1);
      }

      this.searchBox.render(context, mouseX, mouseY, delta);
      this.blockingButton.visible = this.currentTab == SocialInteractionsScreen.Tab.BLOCKED;
      super.render(context, mouseX, mouseY, delta);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.searchBox.isFocused() && this.client.options.socialInteractionsKey.matchesKey(keyCode, scanCode)) {
         this.client.setScreen((Screen)null);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean shouldPause() {
      return false;
   }

   private void onSearchChange(String currentSearch) {
      currentSearch = currentSearch.toLowerCase(Locale.ROOT);
      if (!currentSearch.equals(this.currentSearch)) {
         this.playerList.setCurrentSearch(currentSearch);
         this.currentSearch = currentSearch;
         this.setCurrentTab(this.currentTab);
      }

   }

   private void updateServerLabel(MinecraftClient client) {
      int i = client.getNetworkHandler().getPlayerList().size();
      if (this.playerCount != i) {
         String string = "";
         ServerInfo lv = client.getCurrentServerEntry();
         if (client.isInSingleplayer()) {
            string = client.getServer().getServerMotd();
         } else if (lv != null) {
            string = lv.name;
         }

         if (i > 1) {
            this.serverLabel = Text.translatable("gui.socialInteractions.server_label.multiple", string, i);
         } else {
            this.serverLabel = Text.translatable("gui.socialInteractions.server_label.single", string, i);
         }

         this.playerCount = i;
      }

   }

   public void setPlayerOnline(PlayerListEntry player) {
      this.playerList.setPlayerOnline(player, this.currentTab);
   }

   public void setPlayerOffline(UUID uuid) {
      this.playerList.setPlayerOffline(uuid);
   }

   static {
      SELECTED_ALL_TAB_TITLE = ALL_TAB_TITLE.copyContentOnly().formatted(Formatting.UNDERLINE);
      SELECTED_HIDDEN_TAB_TITLE = HIDDEN_TAB_TITLE.copyContentOnly().formatted(Formatting.UNDERLINE);
      SELECTED_BLOCKED_TAB_TITLE = BLOCKED_TAB_TITLE.copyContentOnly().formatted(Formatting.UNDERLINE);
      SEARCH_TEXT = Text.translatable("gui.socialInteractions.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
      EMPTY_SEARCH_TEXT = Text.translatable("gui.socialInteractions.search_empty").formatted(Formatting.GRAY);
      EMPTY_HIDDEN_TEXT = Text.translatable("gui.socialInteractions.empty_hidden").formatted(Formatting.GRAY);
      EMPTY_BLOCKED_TEXT = Text.translatable("gui.socialInteractions.empty_blocked").formatted(Formatting.GRAY);
      BLOCKING_TEXT = Text.translatable("gui.socialInteractions.blocking_hint");
   }

   @Environment(EnvType.CLIENT)
   public static enum Tab {
      ALL,
      HIDDEN,
      BLOCKED;

      // $FF: synthetic method
      private static Tab[] method_36890() {
         return new Tab[]{ALL, HIDDEN, BLOCKED};
      }
   }
}
