package net.minecraft.client.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public record TextRenderLayerSet(RenderLayer normal, RenderLayer seeThrough, RenderLayer polygonOffset) {
   public TextRenderLayerSet(RenderLayer arg, RenderLayer arg2, RenderLayer arg3) {
      this.normal = arg;
      this.seeThrough = arg2;
      this.polygonOffset = arg3;
   }

   public static TextRenderLayerSet ofIntensity(Identifier textureId) {
      return new TextRenderLayerSet(RenderLayer.getTextIntensity(textureId), RenderLayer.getTextIntensitySeeThrough(textureId), RenderLayer.getTextIntensityPolygonOffset(textureId));
   }

   public static TextRenderLayerSet of(Identifier textureId) {
      return new TextRenderLayerSet(RenderLayer.getText(textureId), RenderLayer.getTextSeeThrough(textureId), RenderLayer.getTextPolygonOffset(textureId));
   }

   public RenderLayer getRenderLayer(TextRenderer.TextLayerType layerType) {
      RenderLayer var10000;
      switch (layerType) {
         case NORMAL:
            var10000 = this.normal;
            break;
         case SEE_THROUGH:
            var10000 = this.seeThrough;
            break;
         case POLYGON_OFFSET:
            var10000 = this.polygonOffset;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public RenderLayer normal() {
      return this.normal;
   }

   public RenderLayer seeThrough() {
      return this.seeThrough;
   }

   public RenderLayer polygonOffset() {
      return this.polygonOffset;
   }
}
