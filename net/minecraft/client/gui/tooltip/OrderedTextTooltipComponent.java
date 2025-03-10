package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class OrderedTextTooltipComponent implements TooltipComponent {
   private final OrderedText text;

   public OrderedTextTooltipComponent(OrderedText text) {
      this.text = text;
   }

   public int getWidth(TextRenderer textRenderer) {
      return textRenderer.getWidth(this.text);
   }

   public int getHeight() {
      return 10;
   }

   public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, VertexConsumerProvider.Immediate vertexConsumers) {
      textRenderer.draw((OrderedText)this.text, (float)x, (float)y, -1, true, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
   }
}
