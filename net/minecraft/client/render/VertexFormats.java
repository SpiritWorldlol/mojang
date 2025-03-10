package net.minecraft.client.render;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexFormats {
   public static final VertexFormatElement POSITION_ELEMENT;
   public static final VertexFormatElement COLOR_ELEMENT;
   public static final VertexFormatElement TEXTURE_ELEMENT;
   public static final VertexFormatElement OVERLAY_ELEMENT;
   public static final VertexFormatElement LIGHT_ELEMENT;
   public static final VertexFormatElement NORMAL_ELEMENT;
   public static final VertexFormatElement PADDING_ELEMENT;
   public static final VertexFormatElement UV_ELEMENT;
   public static final VertexFormat BLIT_SCREEN;
   public static final VertexFormat POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
   public static final VertexFormat POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL;
   public static final VertexFormat POSITION_TEXTURE_COLOR_LIGHT;
   public static final VertexFormat POSITION;
   public static final VertexFormat POSITION_COLOR;
   public static final VertexFormat LINES;
   public static final VertexFormat POSITION_COLOR_LIGHT;
   public static final VertexFormat POSITION_TEXTURE;
   public static final VertexFormat POSITION_COLOR_TEXTURE;
   public static final VertexFormat POSITION_TEXTURE_COLOR;
   public static final VertexFormat POSITION_COLOR_TEXTURE_LIGHT;
   public static final VertexFormat POSITION_TEXTURE_LIGHT_COLOR;
   public static final VertexFormat POSITION_TEXTURE_COLOR_NORMAL;

   static {
      POSITION_ELEMENT = new VertexFormatElement(0, VertexFormatElement.ComponentType.FLOAT, VertexFormatElement.Type.POSITION, 3);
      COLOR_ELEMENT = new VertexFormatElement(0, VertexFormatElement.ComponentType.UBYTE, VertexFormatElement.Type.COLOR, 4);
      TEXTURE_ELEMENT = new VertexFormatElement(0, VertexFormatElement.ComponentType.FLOAT, VertexFormatElement.Type.UV, 2);
      OVERLAY_ELEMENT = new VertexFormatElement(1, VertexFormatElement.ComponentType.SHORT, VertexFormatElement.Type.UV, 2);
      LIGHT_ELEMENT = new VertexFormatElement(2, VertexFormatElement.ComponentType.SHORT, VertexFormatElement.Type.UV, 2);
      NORMAL_ELEMENT = new VertexFormatElement(0, VertexFormatElement.ComponentType.BYTE, VertexFormatElement.Type.NORMAL, 3);
      PADDING_ELEMENT = new VertexFormatElement(0, VertexFormatElement.ComponentType.BYTE, VertexFormatElement.Type.PADDING, 1);
      UV_ELEMENT = TEXTURE_ELEMENT;
      BLIT_SCREEN = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("UV", UV_ELEMENT).put("Color", COLOR_ELEMENT).build());
      POSITION_COLOR_TEXTURE_LIGHT_NORMAL = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("Color", COLOR_ELEMENT).put("UV0", TEXTURE_ELEMENT).put("UV2", LIGHT_ELEMENT).put("Normal", NORMAL_ELEMENT).put("Padding", PADDING_ELEMENT).build());
      POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("Color", COLOR_ELEMENT).put("UV0", TEXTURE_ELEMENT).put("UV1", OVERLAY_ELEMENT).put("UV2", LIGHT_ELEMENT).put("Normal", NORMAL_ELEMENT).put("Padding", PADDING_ELEMENT).build());
      POSITION_TEXTURE_COLOR_LIGHT = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("UV0", TEXTURE_ELEMENT).put("Color", COLOR_ELEMENT).put("UV2", LIGHT_ELEMENT).build());
      POSITION = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).build());
      POSITION_COLOR = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("Color", COLOR_ELEMENT).build());
      LINES = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("Color", COLOR_ELEMENT).put("Normal", NORMAL_ELEMENT).put("Padding", PADDING_ELEMENT).build());
      POSITION_COLOR_LIGHT = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("Color", COLOR_ELEMENT).put("UV2", LIGHT_ELEMENT).build());
      POSITION_TEXTURE = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("UV0", TEXTURE_ELEMENT).build());
      POSITION_COLOR_TEXTURE = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("Color", COLOR_ELEMENT).put("UV0", TEXTURE_ELEMENT).build());
      POSITION_TEXTURE_COLOR = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("UV0", TEXTURE_ELEMENT).put("Color", COLOR_ELEMENT).build());
      POSITION_COLOR_TEXTURE_LIGHT = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("Color", COLOR_ELEMENT).put("UV0", TEXTURE_ELEMENT).put("UV2", LIGHT_ELEMENT).build());
      POSITION_TEXTURE_LIGHT_COLOR = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("UV0", TEXTURE_ELEMENT).put("UV2", LIGHT_ELEMENT).put("Color", COLOR_ELEMENT).build());
      POSITION_TEXTURE_COLOR_NORMAL = new VertexFormat(ImmutableMap.builder().put("Position", POSITION_ELEMENT).put("UV0", TEXTURE_ELEMENT).put("Color", COLOR_ELEMENT).put("Normal", NORMAL_ELEMENT).put("Padding", PADDING_ELEMENT).build());
   }
}
