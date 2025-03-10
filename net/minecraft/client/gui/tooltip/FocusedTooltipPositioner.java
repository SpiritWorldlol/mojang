package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@Environment(EnvType.CLIENT)
public class FocusedTooltipPositioner implements TooltipPositioner {
   private final ClickableWidget widget;

   public FocusedTooltipPositioner(ClickableWidget widget) {
      this.widget = widget;
   }

   public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
      Vector2i vector2i = new Vector2i();
      vector2i.x = this.widget.getX() + 3;
      vector2i.y = this.widget.getY() + this.widget.getHeight() + 3 + 1;
      if (vector2i.y + height + 3 > screenHeight) {
         vector2i.y = this.widget.getY() - height - 3 - 1;
      }

      if (vector2i.x + width > screenWidth) {
         vector2i.x = Math.max(this.widget.getX() + this.widget.getWidth() - width - 3, 4);
      }

      return vector2i;
   }
}
