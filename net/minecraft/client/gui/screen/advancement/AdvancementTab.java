package net.minecraft.client.gui.screen.advancement;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AdvancementTab {
   private final MinecraftClient client;
   private final AdvancementsScreen screen;
   private final AdvancementTabType type;
   private final int index;
   private final Advancement root;
   private final AdvancementDisplay display;
   private final ItemStack icon;
   private final Text title;
   private final AdvancementWidget rootWidget;
   private final Map widgets = Maps.newLinkedHashMap();
   private double originX;
   private double originY;
   private int minPanX = Integer.MAX_VALUE;
   private int minPanY = Integer.MAX_VALUE;
   private int maxPanX = Integer.MIN_VALUE;
   private int maxPanY = Integer.MIN_VALUE;
   private float alpha;
   private boolean initialized;

   public AdvancementTab(MinecraftClient client, AdvancementsScreen screen, AdvancementTabType type, int index, Advancement root, AdvancementDisplay display) {
      this.client = client;
      this.screen = screen;
      this.type = type;
      this.index = index;
      this.root = root;
      this.display = display;
      this.icon = display.getIcon();
      this.title = display.getTitle();
      this.rootWidget = new AdvancementWidget(this, client, root, display);
      this.addWidget(this.rootWidget, root);
   }

   public AdvancementTabType getType() {
      return this.type;
   }

   public int getIndex() {
      return this.index;
   }

   public Advancement getRoot() {
      return this.root;
   }

   public Text getTitle() {
      return this.title;
   }

   public AdvancementDisplay getDisplay() {
      return this.display;
   }

   public void drawBackground(DrawContext context, int x, int y, boolean selected) {
      this.type.drawBackground(context, x, y, selected, this.index);
   }

   public void drawIcon(DrawContext context, int x, int y) {
      this.type.drawIcon(context, x, y, this.index, this.icon);
   }

   public void render(DrawContext context, int x, int y) {
      if (!this.initialized) {
         this.originX = (double)(117 - (this.maxPanX + this.minPanX) / 2);
         this.originY = (double)(56 - (this.maxPanY + this.minPanY) / 2);
         this.initialized = true;
      }

      context.enableScissor(x, y, x + 234, y + 113);
      context.getMatrices().push();
      context.getMatrices().translate((float)x, (float)y, 0.0F);
      Identifier lv = (Identifier)Objects.requireNonNullElse(this.display.getBackground(), TextureManager.MISSING_IDENTIFIER);
      int k = MathHelper.floor(this.originX);
      int l = MathHelper.floor(this.originY);
      int m = k % 16;
      int n = l % 16;

      for(int o = -1; o <= 15; ++o) {
         for(int p = -1; p <= 8; ++p) {
            context.drawTexture(lv, m + 16 * o, n + 16 * p, 0.0F, 0.0F, 16, 16, 16, 16);
         }
      }

      this.rootWidget.renderLines(context, k, l, true);
      this.rootWidget.renderLines(context, k, l, false);
      this.rootWidget.renderWidgets(context, k, l);
      context.getMatrices().pop();
      context.disableScissor();
   }

   public void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int x, int y) {
      context.getMatrices().push();
      context.getMatrices().translate(0.0F, 0.0F, -200.0F);
      context.fill(0, 0, 234, 113, MathHelper.floor(this.alpha * 255.0F) << 24);
      boolean bl = false;
      int m = MathHelper.floor(this.originX);
      int n = MathHelper.floor(this.originY);
      if (mouseX > 0 && mouseX < 234 && mouseY > 0 && mouseY < 113) {
         Iterator var9 = this.widgets.values().iterator();

         while(var9.hasNext()) {
            AdvancementWidget lv = (AdvancementWidget)var9.next();
            if (lv.shouldRender(m, n, mouseX, mouseY)) {
               bl = true;
               lv.drawTooltip(context, m, n, this.alpha, x, y);
               break;
            }
         }
      }

      context.getMatrices().pop();
      if (bl) {
         this.alpha = MathHelper.clamp(this.alpha + 0.02F, 0.0F, 0.3F);
      } else {
         this.alpha = MathHelper.clamp(this.alpha - 0.04F, 0.0F, 1.0F);
      }

   }

   public boolean isClickOnTab(int screenX, int screenY, double mouseX, double mouseY) {
      return this.type.isClickOnTab(screenX, screenY, this.index, mouseX, mouseY);
   }

   @Nullable
   public static AdvancementTab create(MinecraftClient client, AdvancementsScreen screen, int index, Advancement root) {
      if (root.getDisplay() == null) {
         return null;
      } else {
         AdvancementTabType[] var4 = AdvancementTabType.values();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            AdvancementTabType lv = var4[var6];
            if (index < lv.getTabCount()) {
               return new AdvancementTab(client, screen, lv, index, root, root.getDisplay());
            }

            index -= lv.getTabCount();
         }

         return null;
      }
   }

   public void move(double offsetX, double offsetY) {
      if (this.maxPanX - this.minPanX > 234) {
         this.originX = MathHelper.clamp(this.originX + offsetX, (double)(-(this.maxPanX - 234)), 0.0);
      }

      if (this.maxPanY - this.minPanY > 113) {
         this.originY = MathHelper.clamp(this.originY + offsetY, (double)(-(this.maxPanY - 113)), 0.0);
      }

   }

   public void addAdvancement(Advancement advancement) {
      if (advancement.getDisplay() != null) {
         AdvancementWidget lv = new AdvancementWidget(this, this.client, advancement, advancement.getDisplay());
         this.addWidget(lv, advancement);
      }
   }

   private void addWidget(AdvancementWidget widget, Advancement advancement) {
      this.widgets.put(advancement, widget);
      int i = widget.getX();
      int j = i + 28;
      int k = widget.getY();
      int l = k + 27;
      this.minPanX = Math.min(this.minPanX, i);
      this.maxPanX = Math.max(this.maxPanX, j);
      this.minPanY = Math.min(this.minPanY, k);
      this.maxPanY = Math.max(this.maxPanY, l);
      Iterator var7 = this.widgets.values().iterator();

      while(var7.hasNext()) {
         AdvancementWidget lv = (AdvancementWidget)var7.next();
         lv.addToTree();
      }

   }

   @Nullable
   public AdvancementWidget getWidget(Advancement advancement) {
      return (AdvancementWidget)this.widgets.get(advancement);
   }

   public AdvancementsScreen getScreen() {
      return this.screen;
   }
}
