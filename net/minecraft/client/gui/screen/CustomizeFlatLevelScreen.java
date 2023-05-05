package net.minecraft.client.gui.screen;

import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CustomizeFlatLevelScreen extends Screen {
   private static final int ICON_TEXTURE_SIZE = 128;
   private static final int ICON_SIZE = 18;
   private static final int BUTTON_HEIGHT = 20;
   private static final int ICON_BACKGROUND_OFFSET_X = 1;
   private static final int ICON_BACKGROUND_OFFSET_Y = 1;
   private static final int ICON_OFFSET_X = 2;
   private static final int ICON_OFFSET_Y = 2;
   protected final CreateWorldScreen parent;
   private final Consumer configConsumer;
   FlatChunkGeneratorConfig config;
   private Text tileText;
   private Text heightText;
   private SuperflatLayersListWidget layers;
   private ButtonWidget widgetButtonRemoveLayer;

   public CustomizeFlatLevelScreen(CreateWorldScreen parent, Consumer configConsumer, FlatChunkGeneratorConfig config) {
      super(Text.translatable("createWorld.customize.flat.title"));
      this.parent = parent;
      this.configConsumer = configConsumer;
      this.config = config;
   }

   public FlatChunkGeneratorConfig getConfig() {
      return this.config;
   }

   public void setConfig(FlatChunkGeneratorConfig config) {
      this.config = config;
   }

   protected void init() {
      this.tileText = Text.translatable("createWorld.customize.flat.tile");
      this.heightText = Text.translatable("createWorld.customize.flat.height");
      this.layers = new SuperflatLayersListWidget();
      this.addSelectableChild(this.layers);
      this.widgetButtonRemoveLayer = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("createWorld.customize.flat.removeLayer"), (button) -> {
         if (this.hasLayerSelected()) {
            List list = this.config.getLayers();
            int i = this.layers.children().indexOf(this.layers.getSelectedOrNull());
            int j = list.size() - i - 1;
            list.remove(j);
            this.layers.setSelected(list.isEmpty() ? null : (SuperflatLayersListWidget.SuperflatLayerEntry)this.layers.children().get(Math.min(i, list.size() - 1)));
            this.config.updateLayerBlocks();
            this.layers.updateLayers();
            this.updateRemoveLayerButton();
         }
      }).dimensions(this.width / 2 - 155, this.height - 52, 150, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("createWorld.customize.presets"), (button) -> {
         this.client.setScreen(new PresetsScreen(this));
         this.config.updateLayerBlocks();
         this.updateRemoveLayerButton();
      }).dimensions(this.width / 2 + 5, this.height - 52, 150, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.configConsumer.accept(this.config);
         this.client.setScreen(this.parent);
         this.config.updateLayerBlocks();
      }).dimensions(this.width / 2 - 155, this.height - 28, 150, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.client.setScreen(this.parent);
         this.config.updateLayerBlocks();
      }).dimensions(this.width / 2 + 5, this.height - 28, 150, 20).build());
      this.config.updateLayerBlocks();
      this.updateRemoveLayerButton();
   }

   void updateRemoveLayerButton() {
      this.widgetButtonRemoveLayer.active = this.hasLayerSelected();
   }

   private boolean hasLayerSelected() {
      return this.layers.getSelectedOrNull() != null;
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      this.layers.render(context, mouseX, mouseY, delta);
      context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 8, 16777215);
      int k = this.width / 2 - 92 - 16;
      context.drawTextWithShadow(this.textRenderer, (Text)this.tileText, k, 32, 16777215);
      context.drawTextWithShadow(this.textRenderer, (Text)this.heightText, k + 2 + 213 - this.textRenderer.getWidth((StringVisitable)this.heightText), 32, 16777215);
      super.render(context, mouseX, mouseY, delta);
   }

   @Environment(EnvType.CLIENT)
   private class SuperflatLayersListWidget extends AlwaysSelectedEntryListWidget {
      static final Identifier STATS_ICONS_TEXTURE = new Identifier("textures/gui/container/stats_icons.png");

      public SuperflatLayersListWidget() {
         super(CustomizeFlatLevelScreen.this.client, CustomizeFlatLevelScreen.this.width, CustomizeFlatLevelScreen.this.height, 43, CustomizeFlatLevelScreen.this.height - 60, 24);

         for(int i = 0; i < CustomizeFlatLevelScreen.this.config.getLayers().size(); ++i) {
            this.addEntry(new SuperflatLayerEntry());
         }

      }

      public void setSelected(@Nullable SuperflatLayerEntry arg) {
         super.setSelected(arg);
         CustomizeFlatLevelScreen.this.updateRemoveLayerButton();
      }

      protected int getScrollbarPositionX() {
         return this.width - 70;
      }

      public void updateLayers() {
         int i = this.children().indexOf(this.getSelectedOrNull());
         this.clearEntries();

         for(int j = 0; j < CustomizeFlatLevelScreen.this.config.getLayers().size(); ++j) {
            this.addEntry(new SuperflatLayerEntry());
         }

         List list = this.children();
         if (i >= 0 && i < list.size()) {
            this.setSelected((SuperflatLayerEntry)list.get(i));
         }

      }

      @Environment(EnvType.CLIENT)
      private class SuperflatLayerEntry extends AlwaysSelectedEntryListWidget.Entry {
         SuperflatLayerEntry() {
         }

         public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            FlatChunkGeneratorLayer lv = (FlatChunkGeneratorLayer)CustomizeFlatLevelScreen.this.config.getLayers().get(CustomizeFlatLevelScreen.this.config.getLayers().size() - index - 1);
            BlockState lv2 = lv.getBlockState();
            ItemStack lv3 = this.createItemStackFor(lv2);
            this.renderIcon(context, x, y, lv3);
            context.drawText(CustomizeFlatLevelScreen.this.textRenderer, lv3.getName(), x + 18 + 5, y + 3, 16777215, false);
            MutableText lv4;
            if (index == 0) {
               lv4 = Text.translatable("createWorld.customize.flat.layer.top", lv.getThickness());
            } else if (index == CustomizeFlatLevelScreen.this.config.getLayers().size() - 1) {
               lv4 = Text.translatable("createWorld.customize.flat.layer.bottom", lv.getThickness());
            } else {
               lv4 = Text.translatable("createWorld.customize.flat.layer", lv.getThickness());
            }

            context.drawText(CustomizeFlatLevelScreen.this.textRenderer, (Text)lv4, x + 2 + 213 - CustomizeFlatLevelScreen.this.textRenderer.getWidth((StringVisitable)lv4), y + 3, 16777215, false);
         }

         private ItemStack createItemStackFor(BlockState state) {
            Item lv = state.getBlock().asItem();
            if (lv == Items.AIR) {
               if (state.isOf(Blocks.WATER)) {
                  lv = Items.WATER_BUCKET;
               } else if (state.isOf(Blocks.LAVA)) {
                  lv = Items.LAVA_BUCKET;
               }
            }

            return new ItemStack(lv);
         }

         public Text getNarration() {
            FlatChunkGeneratorLayer lv = (FlatChunkGeneratorLayer)CustomizeFlatLevelScreen.this.config.getLayers().get(CustomizeFlatLevelScreen.this.config.getLayers().size() - SuperflatLayersListWidget.this.children().indexOf(this) - 1);
            ItemStack lv2 = this.createItemStackFor(lv.getBlockState());
            return (Text)(!lv2.isEmpty() ? Text.translatable("narrator.select", lv2.getName()) : ScreenTexts.EMPTY);
         }

         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
               SuperflatLayersListWidget.this.setSelected(this);
               return true;
            } else {
               return false;
            }
         }

         private void renderIcon(DrawContext context, int x, int y, ItemStack iconItem) {
            this.renderIconBackgroundTexture(context, x + 1, y + 1);
            if (!iconItem.isEmpty()) {
               context.drawItemWithoutEntity(iconItem, x + 2, y + 2);
            }

         }

         private void renderIconBackgroundTexture(DrawContext context, int x, int y) {
            context.drawTexture(CustomizeFlatLevelScreen.SuperflatLayersListWidget.STATS_ICONS_TEXTURE, x, y, 0, 0.0F, 0.0F, 18, 18, 128, 128);
         }
      }
   }
}
