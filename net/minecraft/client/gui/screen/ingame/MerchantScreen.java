package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;

@Environment(EnvType.CLIENT)
public class MerchantScreen extends HandledScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/villager2.png");
   private static final int TEXTURE_WIDTH = 512;
   private static final int TEXTURE_HEIGHT = 256;
   private static final int field_32356 = 99;
   private static final int XP_BAR_X_OFFSET = 136;
   private static final int TRADE_LIST_AREA_Y_OFFSET = 16;
   private static final int FIRST_BUY_ITEM_X_OFFSET = 5;
   private static final int SECOND_BUY_ITEM_X_OFFSET = 35;
   private static final int SOLD_ITEM_X_OFFSET = 68;
   private static final int field_32362 = 6;
   private static final int MAX_TRADE_OFFERS = 7;
   private static final int field_32364 = 5;
   private static final int TRADE_OFFER_BUTTON_HEIGHT = 20;
   private static final int TRADE_OFFER_BUTTON_WIDTH = 88;
   private static final int SCROLLBAR_HEIGHT = 27;
   private static final int SCROLLBAR_WIDTH = 6;
   private static final int SCROLLBAR_AREA_HEIGHT = 139;
   private static final int SCROLLBAR_OFFSET_Y = 18;
   private static final int SCROLLBAR_OFFSET_X = 94;
   private static final Text TRADES_TEXT = Text.translatable("merchant.trades");
   private static final Text SEPARATOR_TEXT = Text.literal(" - ");
   private static final Text DEPRECATED_TEXT = Text.translatable("merchant.deprecated");
   private int selectedIndex;
   private final WidgetButtonPage[] offers = new WidgetButtonPage[7];
   int indexStartOffset;
   private boolean scrolling;

   public MerchantScreen(MerchantScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.backgroundWidth = 276;
      this.playerInventoryTitleX = 107;
   }

   private void syncRecipeIndex() {
      ((MerchantScreenHandler)this.handler).setRecipeIndex(this.selectedIndex);
      ((MerchantScreenHandler)this.handler).switchTo(this.selectedIndex);
      this.client.getNetworkHandler().sendPacket(new SelectMerchantTradeC2SPacket(this.selectedIndex));
   }

   protected void init() {
      super.init();
      int i = (this.width - this.backgroundWidth) / 2;
      int j = (this.height - this.backgroundHeight) / 2;
      int k = j + 16 + 2;

      for(int l = 0; l < 7; ++l) {
         this.offers[l] = (WidgetButtonPage)this.addDrawableChild(new WidgetButtonPage(i + 5, k, l, (button) -> {
            if (button instanceof WidgetButtonPage) {
               this.selectedIndex = ((WidgetButtonPage)button).getIndex() + this.indexStartOffset;
               this.syncRecipeIndex();
            }

         }));
         k += 20;
      }

   }

   protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
      int k = ((MerchantScreenHandler)this.handler).getLevelProgress();
      if (k > 0 && k <= 5 && ((MerchantScreenHandler)this.handler).isLeveled()) {
         Text lv = this.title.copy().append(SEPARATOR_TEXT).append((Text)Text.translatable("merchant.level." + k));
         int l = this.textRenderer.getWidth((StringVisitable)lv);
         int m = 49 + this.backgroundWidth / 2 - l / 2;
         context.drawText(this.textRenderer, (Text)lv, m, 6, 4210752, false);
      } else {
         context.drawText(this.textRenderer, (Text)this.title, 49 + this.backgroundWidth / 2 - this.textRenderer.getWidth((StringVisitable)this.title) / 2, 6, 4210752, false);
      }

      context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 4210752, false);
      int n = this.textRenderer.getWidth((StringVisitable)TRADES_TEXT);
      context.drawText(this.textRenderer, (Text)TRADES_TEXT, 5 - n / 2 + 48, 6, 4210752, false);
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      int k = (this.width - this.backgroundWidth) / 2;
      int l = (this.height - this.backgroundHeight) / 2;
      context.drawTexture(TEXTURE, k, l, 0, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 512, 256);
      TradeOfferList lv = ((MerchantScreenHandler)this.handler).getRecipes();
      if (!lv.isEmpty()) {
         int m = this.selectedIndex;
         if (m < 0 || m >= lv.size()) {
            return;
         }

         TradeOffer lv2 = (TradeOffer)lv.get(m);
         if (lv2.isDisabled()) {
            context.drawTexture(TEXTURE, this.x + 83 + 99, this.y + 35, 0, 311.0F, 0.0F, 28, 21, 512, 256);
         }
      }

   }

   private void drawLevelInfo(DrawContext context, int x, int y, TradeOffer tradeOffer) {
      int k = ((MerchantScreenHandler)this.handler).getLevelProgress();
      int l = ((MerchantScreenHandler)this.handler).getExperience();
      if (k < 5) {
         context.drawTexture(TEXTURE, x + 136, y + 16, 0, 0.0F, 186.0F, 102, 5, 512, 256);
         int m = VillagerData.getLowerLevelExperience(k);
         if (l >= m && VillagerData.canLevelUp(k)) {
            int n = true;
            float f = 100.0F / (float)(VillagerData.getUpperLevelExperience(k) - m);
            int o = Math.min(MathHelper.floor(f * (float)(l - m)), 100);
            context.drawTexture(TEXTURE, x + 136, y + 16, 0, 0.0F, 191.0F, o + 1, 5, 512, 256);
            int p = ((MerchantScreenHandler)this.handler).getMerchantRewardedExperience();
            if (p > 0) {
               int q = Math.min(MathHelper.floor((float)p * f), 100 - o);
               context.drawTexture(TEXTURE, x + 136 + o + 1, y + 16 + 1, 0, 2.0F, 182.0F, q, 3, 512, 256);
            }

         }
      }
   }

   private void renderScrollbar(DrawContext context, int x, int y, TradeOfferList tradeOffers) {
      int k = tradeOffers.size() + 1 - 7;
      if (k > 1) {
         int l = 139 - (27 + (k - 1) * 139 / k);
         int m = 1 + l / k + 139 / k;
         int n = true;
         int o = Math.min(113, this.indexStartOffset * m);
         if (this.indexStartOffset == k - 1) {
            o = 113;
         }

         context.drawTexture(TEXTURE, x + 94, y + 18 + o, 0, 0.0F, 199.0F, 6, 27, 512, 256);
      } else {
         context.drawTexture(TEXTURE, x + 94, y + 18, 0, 6.0F, 199.0F, 6, 27, 512, 256);
      }

   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      TradeOfferList lv = ((MerchantScreenHandler)this.handler).getRecipes();
      if (!lv.isEmpty()) {
         int k = (this.width - this.backgroundWidth) / 2;
         int l = (this.height - this.backgroundHeight) / 2;
         int m = l + 16 + 1;
         int n = k + 5 + 5;
         this.renderScrollbar(context, k, l, lv);
         int o = 0;
         Iterator var11 = lv.iterator();

         while(true) {
            TradeOffer lv2;
            while(var11.hasNext()) {
               lv2 = (TradeOffer)var11.next();
               if (this.canScroll(lv.size()) && (o < this.indexStartOffset || o >= 7 + this.indexStartOffset)) {
                  ++o;
               } else {
                  ItemStack lv3 = lv2.getOriginalFirstBuyItem();
                  ItemStack lv4 = lv2.getAdjustedFirstBuyItem();
                  ItemStack lv5 = lv2.getSecondBuyItem();
                  ItemStack lv6 = lv2.getSellItem();
                  context.getMatrices().push();
                  context.getMatrices().translate(0.0F, 0.0F, 100.0F);
                  int p = m + 2;
                  this.renderFirstBuyItem(context, lv4, lv3, n, p);
                  if (!lv5.isEmpty()) {
                     context.drawItemWithoutEntity(lv5, k + 5 + 35, p);
                     context.drawItemInSlot(this.textRenderer, lv5, k + 5 + 35, p);
                  }

                  this.renderArrow(context, lv2, k, p);
                  context.drawItemWithoutEntity(lv6, k + 5 + 68, p);
                  context.drawItemInSlot(this.textRenderer, lv6, k + 5 + 68, p);
                  context.getMatrices().pop();
                  m += 20;
                  ++o;
               }
            }

            int q = this.selectedIndex;
            lv2 = (TradeOffer)lv.get(q);
            if (((MerchantScreenHandler)this.handler).isLeveled()) {
               this.drawLevelInfo(context, k, l, lv2);
            }

            if (lv2.isDisabled() && this.isPointWithinBounds(186, 35, 22, 21, (double)mouseX, (double)mouseY) && ((MerchantScreenHandler)this.handler).canRefreshTrades()) {
               context.drawTooltip(this.textRenderer, DEPRECATED_TEXT, mouseX, mouseY);
            }

            WidgetButtonPage[] var19 = this.offers;
            int var20 = var19.length;

            for(int var21 = 0; var21 < var20; ++var21) {
               WidgetButtonPage lv7 = var19[var21];
               if (lv7.isSelected()) {
                  lv7.renderTooltip(context, mouseX, mouseY);
               }

               lv7.visible = lv7.index < ((MerchantScreenHandler)this.handler).getRecipes().size();
            }

            RenderSystem.enableDepthTest();
            break;
         }
      }

      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   private void renderArrow(DrawContext context, TradeOffer tradeOffer, int x, int y) {
      RenderSystem.enableBlend();
      if (tradeOffer.isDisabled()) {
         context.drawTexture(TEXTURE, x + 5 + 35 + 20, y + 3, 0, 25.0F, 171.0F, 10, 9, 512, 256);
      } else {
         context.drawTexture(TEXTURE, x + 5 + 35 + 20, y + 3, 0, 15.0F, 171.0F, 10, 9, 512, 256);
      }

   }

   private void renderFirstBuyItem(DrawContext context, ItemStack adjustedFirstBuyItem, ItemStack originalFirstBuyItem, int x, int y) {
      context.drawItemWithoutEntity(adjustedFirstBuyItem, x, y);
      if (originalFirstBuyItem.getCount() == adjustedFirstBuyItem.getCount()) {
         context.drawItemInSlot(this.textRenderer, adjustedFirstBuyItem, x, y);
      } else {
         context.drawItemInSlot(this.textRenderer, originalFirstBuyItem, x, y, originalFirstBuyItem.getCount() == 1 ? "1" : null);
         context.drawItemInSlot(this.textRenderer, adjustedFirstBuyItem, x + 14, y, adjustedFirstBuyItem.getCount() == 1 ? "1" : null);
         context.getMatrices().push();
         context.getMatrices().translate(0.0F, 0.0F, 300.0F);
         context.drawTexture(TEXTURE, x + 7, y + 12, 0, 0.0F, 176.0F, 9, 2, 512, 256);
         context.getMatrices().pop();
      }

   }

   private boolean canScroll(int listSize) {
      return listSize > 7;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      int i = ((MerchantScreenHandler)this.handler).getRecipes().size();
      if (this.canScroll(i)) {
         int j = i - 7;
         this.indexStartOffset = MathHelper.clamp((int)((double)this.indexStartOffset - amount), 0, j);
      }

      return true;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      int j = ((MerchantScreenHandler)this.handler).getRecipes().size();
      if (this.scrolling) {
         int k = this.y + 18;
         int l = k + 139;
         int m = j - 7;
         float h = ((float)mouseY - (float)k - 13.5F) / ((float)(l - k) - 27.0F);
         h = h * (float)m + 0.5F;
         this.indexStartOffset = MathHelper.clamp((int)h, 0, m);
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.scrolling = false;
      int j = (this.width - this.backgroundWidth) / 2;
      int k = (this.height - this.backgroundHeight) / 2;
      if (this.canScroll(((MerchantScreenHandler)this.handler).getRecipes().size()) && mouseX > (double)(j + 94) && mouseX < (double)(j + 94 + 6) && mouseY > (double)(k + 18) && mouseY <= (double)(k + 18 + 139 + 1)) {
         this.scrolling = true;
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Environment(EnvType.CLIENT)
   private class WidgetButtonPage extends ButtonWidget {
      final int index;

      public WidgetButtonPage(int x, int y, int index, ButtonWidget.PressAction onPress) {
         super(x, y, 88, 20, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
         this.index = index;
         this.visible = false;
      }

      public int getIndex() {
         return this.index;
      }

      public void renderTooltip(DrawContext context, int x, int y) {
         if (this.hovered && ((MerchantScreenHandler)MerchantScreen.this.handler).getRecipes().size() > this.index + MerchantScreen.this.indexStartOffset) {
            ItemStack lv;
            if (x < this.getX() + 20) {
               lv = ((TradeOffer)((MerchantScreenHandler)MerchantScreen.this.handler).getRecipes().get(this.index + MerchantScreen.this.indexStartOffset)).getAdjustedFirstBuyItem();
               context.drawItemTooltip(MerchantScreen.this.textRenderer, lv, x, y);
            } else if (x < this.getX() + 50 && x > this.getX() + 30) {
               lv = ((TradeOffer)((MerchantScreenHandler)MerchantScreen.this.handler).getRecipes().get(this.index + MerchantScreen.this.indexStartOffset)).getSecondBuyItem();
               if (!lv.isEmpty()) {
                  context.drawItemTooltip(MerchantScreen.this.textRenderer, lv, x, y);
               }
            } else if (x > this.getX() + 65) {
               lv = ((TradeOffer)((MerchantScreenHandler)MerchantScreen.this.handler).getRecipes().get(this.index + MerchantScreen.this.indexStartOffset)).getSellItem();
               context.drawItemTooltip(MerchantScreen.this.textRenderer, lv, x, y);
            }
         }

      }
   }
}
