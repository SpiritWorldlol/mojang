package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.VertexSorter;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public abstract class RenderLayer extends RenderPhase {
   private static final int field_32776 = 4;
   private static final int field_32777 = 1048576;
   public static final int SOLID_BUFFER_SIZE = 2097152;
   public static final int TRANSLUCENT_BUFFER_SIZE = 262144;
   public static final int CUTOUT_BUFFER_SIZE = 131072;
   public static final int DEFAULT_BUFFER_SIZE = 256;
   private static final RenderLayer SOLID;
   private static final RenderLayer CUTOUT_MIPPED;
   private static final RenderLayer CUTOUT;
   private static final RenderLayer TRANSLUCENT;
   private static final RenderLayer TRANSLUCENT_MOVING_BLOCK;
   private static final RenderLayer TRANSLUCENT_NO_CRUMBLING;
   private static final Function ARMOR_CUTOUT_NO_CULL;
   private static final Function ENTITY_SOLID;
   private static final Function ENTITY_CUTOUT;
   private static final BiFunction ENTITY_CUTOUT_NO_CULL;
   private static final BiFunction ENTITY_CUTOUT_NO_CULL_Z_OFFSET;
   private static final Function ITEM_ENTITY_TRANSLUCENT_CULL;
   private static final Function ENTITY_TRANSLUCENT_CULL;
   private static final BiFunction ENTITY_TRANSLUCENT;
   private static final BiFunction ENTITY_TRANSLUCENT_EMISSIVE;
   private static final Function ENTITY_SMOOTH_CUTOUT;
   private static final BiFunction BEACON_BEAM;
   private static final Function ENTITY_DECAL;
   private static final Function ENTITY_NO_OUTLINE;
   private static final Function ENTITY_SHADOW;
   private static final Function ENTITY_ALPHA;
   private static final Function EYES;
   private static final RenderLayer LEASH;
   private static final RenderLayer WATER_MASK;
   private static final RenderLayer ARMOR_GLINT;
   private static final RenderLayer ARMOR_ENTITY_GLINT;
   private static final RenderLayer GLINT_TRANSLUCENT;
   private static final RenderLayer GLINT;
   private static final RenderLayer DIRECT_GLINT;
   private static final RenderLayer ENTITY_GLINT;
   private static final RenderLayer DIRECT_ENTITY_GLINT;
   private static final Function CRUMBLING;
   private static final Function TEXT;
   private static final RenderLayer TEXT_BACKGROUND;
   private static final Function TEXT_INTENSITY;
   private static final Function TEXT_POLYGON_OFFSET;
   private static final Function TEXT_INTENSITY_POLYGON_OFFSET;
   private static final Function TEXT_SEE_THROUGH;
   private static final RenderLayer TEXT_BACKGROUND_SEE_THROUGH;
   private static final Function TEXT_INTENSITY_SEE_THROUGH;
   private static final RenderLayer LIGHTNING;
   private static final RenderLayer TRIPWIRE;
   private static final RenderLayer END_PORTAL;
   private static final RenderLayer END_GATEWAY;
   public static final MultiPhase LINES;
   public static final MultiPhase LINE_STRIP;
   private static final Function DEBUG_LINE_STRIP;
   private static final MultiPhase DEBUG_FILLED_BOX;
   private static final MultiPhase DEBUG_QUADS;
   private static final MultiPhase DEBUG_SECTION_QUADS;
   private static final MultiPhase GUI;
   private static final MultiPhase GUI_OVERLAY;
   private static final MultiPhase GUI_TEXT_HIGHLIGHT;
   private static final MultiPhase GUI_GHOST_RECIPE_OVERLAY;
   private static final ImmutableList BLOCK_LAYERS;
   private final VertexFormat vertexFormat;
   private final VertexFormat.DrawMode drawMode;
   private final int expectedBufferSize;
   private final boolean hasCrumbling;
   private final boolean translucent;
   private final Optional optionalThis;

   public static RenderLayer getSolid() {
      return SOLID;
   }

   public static RenderLayer getCutoutMipped() {
      return CUTOUT_MIPPED;
   }

   public static RenderLayer getCutout() {
      return CUTOUT;
   }

   private static MultiPhaseParameters of(RenderPhase.ShaderProgram program) {
      return RenderLayer.MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).program(program).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).transparency(TRANSLUCENT_TRANSPARENCY).target(TRANSLUCENT_TARGET).build(true);
   }

   public static RenderLayer getTranslucent() {
      return TRANSLUCENT;
   }

   private static MultiPhaseParameters getItemPhaseData() {
      return RenderLayer.MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).program(TRANSLUCENT_MOVING_BLOCK_PROGRAM).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_TARGET).build(true);
   }

   public static RenderLayer getTranslucentMovingBlock() {
      return TRANSLUCENT_MOVING_BLOCK;
   }

   public static RenderLayer getTranslucentNoCrumbling() {
      return TRANSLUCENT_NO_CRUMBLING;
   }

   public static RenderLayer getArmorCutoutNoCull(Identifier texture) {
      return (RenderLayer)ARMOR_CUTOUT_NO_CULL.apply(texture);
   }

   public static RenderLayer getEntitySolid(Identifier texture) {
      return (RenderLayer)ENTITY_SOLID.apply(texture);
   }

   public static RenderLayer getEntityCutout(Identifier texture) {
      return (RenderLayer)ENTITY_CUTOUT.apply(texture);
   }

   public static RenderLayer getEntityCutoutNoCull(Identifier texture, boolean affectsOutline) {
      return (RenderLayer)ENTITY_CUTOUT_NO_CULL.apply(texture, affectsOutline);
   }

   public static RenderLayer getEntityCutoutNoCull(Identifier texture) {
      return getEntityCutoutNoCull(texture, true);
   }

   public static RenderLayer getEntityCutoutNoCullZOffset(Identifier texture, boolean affectsOutline) {
      return (RenderLayer)ENTITY_CUTOUT_NO_CULL_Z_OFFSET.apply(texture, affectsOutline);
   }

   public static RenderLayer getEntityCutoutNoCullZOffset(Identifier texture) {
      return getEntityCutoutNoCullZOffset(texture, true);
   }

   public static RenderLayer getItemEntityTranslucentCull(Identifier texture) {
      return (RenderLayer)ITEM_ENTITY_TRANSLUCENT_CULL.apply(texture);
   }

   public static RenderLayer getEntityTranslucentCull(Identifier texture) {
      return (RenderLayer)ENTITY_TRANSLUCENT_CULL.apply(texture);
   }

   public static RenderLayer getEntityTranslucent(Identifier texture, boolean affectsOutline) {
      return (RenderLayer)ENTITY_TRANSLUCENT.apply(texture, affectsOutline);
   }

   public static RenderLayer getEntityTranslucent(Identifier texture) {
      return getEntityTranslucent(texture, true);
   }

   public static RenderLayer getEntityTranslucentEmissive(Identifier texture, boolean affectsOutline) {
      return (RenderLayer)ENTITY_TRANSLUCENT_EMISSIVE.apply(texture, affectsOutline);
   }

   public static RenderLayer getEntityTranslucentEmissive(Identifier texture) {
      return getEntityTranslucentEmissive(texture, true);
   }

   public static RenderLayer getEntitySmoothCutout(Identifier texture) {
      return (RenderLayer)ENTITY_SMOOTH_CUTOUT.apply(texture);
   }

   public static RenderLayer getBeaconBeam(Identifier texture, boolean translucent) {
      return (RenderLayer)BEACON_BEAM.apply(texture, translucent);
   }

   public static RenderLayer getEntityDecal(Identifier texture) {
      return (RenderLayer)ENTITY_DECAL.apply(texture);
   }

   public static RenderLayer getEntityNoOutline(Identifier texture) {
      return (RenderLayer)ENTITY_NO_OUTLINE.apply(texture);
   }

   public static RenderLayer getEntityShadow(Identifier texture) {
      return (RenderLayer)ENTITY_SHADOW.apply(texture);
   }

   public static RenderLayer getEntityAlpha(Identifier texture) {
      return (RenderLayer)ENTITY_ALPHA.apply(texture);
   }

   public static RenderLayer getEyes(Identifier texture) {
      return (RenderLayer)EYES.apply(texture);
   }

   public static RenderLayer getEnergySwirl(Identifier texture, float x, float y) {
      return of("energy_swirl", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(ENERGY_SWIRL_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).texturing(new RenderPhase.OffsetTexturing(x, y)).transparency(ADDITIVE_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(false));
   }

   public static RenderLayer getLeash() {
      return LEASH;
   }

   public static RenderLayer getWaterMask() {
      return WATER_MASK;
   }

   public static RenderLayer getOutline(Identifier texture) {
      return (RenderLayer)RenderLayer.MultiPhase.CULLING_LAYERS.apply(texture, DISABLE_CULLING);
   }

   public static RenderLayer getArmorGlint() {
      return ARMOR_GLINT;
   }

   public static RenderLayer getArmorEntityGlint() {
      return ARMOR_ENTITY_GLINT;
   }

   public static RenderLayer getGlintTranslucent() {
      return GLINT_TRANSLUCENT;
   }

   public static RenderLayer getGlint() {
      return GLINT;
   }

   public static RenderLayer getDirectGlint() {
      return DIRECT_GLINT;
   }

   public static RenderLayer getEntityGlint() {
      return ENTITY_GLINT;
   }

   public static RenderLayer getDirectEntityGlint() {
      return DIRECT_ENTITY_GLINT;
   }

   public static RenderLayer getBlockBreaking(Identifier texture) {
      return (RenderLayer)CRUMBLING.apply(texture);
   }

   public static RenderLayer getText(Identifier texture) {
      return (RenderLayer)TEXT.apply(texture);
   }

   public static RenderLayer getTextBackground() {
      return TEXT_BACKGROUND;
   }

   public static RenderLayer getTextIntensity(Identifier texture) {
      return (RenderLayer)TEXT_INTENSITY.apply(texture);
   }

   public static RenderLayer getTextPolygonOffset(Identifier texture) {
      return (RenderLayer)TEXT_POLYGON_OFFSET.apply(texture);
   }

   public static RenderLayer getTextIntensityPolygonOffset(Identifier texture) {
      return (RenderLayer)TEXT_INTENSITY_POLYGON_OFFSET.apply(texture);
   }

   public static RenderLayer getTextSeeThrough(Identifier texture) {
      return (RenderLayer)TEXT_SEE_THROUGH.apply(texture);
   }

   public static RenderLayer getTextBackgroundSeeThrough() {
      return TEXT_BACKGROUND_SEE_THROUGH;
   }

   public static RenderLayer getTextIntensitySeeThrough(Identifier texture) {
      return (RenderLayer)TEXT_INTENSITY_SEE_THROUGH.apply(texture);
   }

   public static RenderLayer getLightning() {
      return LIGHTNING;
   }

   private static MultiPhaseParameters getTripwirePhaseData() {
      return RenderLayer.MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).program(TRIPWIRE_PROGRAM).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).transparency(TRANSLUCENT_TRANSPARENCY).target(WEATHER_TARGET).build(true);
   }

   public static RenderLayer getTripwire() {
      return TRIPWIRE;
   }

   public static RenderLayer getEndPortal() {
      return END_PORTAL;
   }

   public static RenderLayer getEndGateway() {
      return END_GATEWAY;
   }

   public static RenderLayer getLines() {
      return LINES;
   }

   public static RenderLayer getLineStrip() {
      return LINE_STRIP;
   }

   public static RenderLayer getDebugLineStrip(double lineWidth) {
      return (RenderLayer)DEBUG_LINE_STRIP.apply(lineWidth);
   }

   public static RenderLayer getDebugFilledBox() {
      return DEBUG_FILLED_BOX;
   }

   public static RenderLayer getDebugQuads() {
      return DEBUG_QUADS;
   }

   public static RenderLayer getDebugSectionQuads() {
      return DEBUG_SECTION_QUADS;
   }

   public static RenderLayer method_51784() {
      return GUI;
   }

   public static RenderLayer method_51785() {
      return GUI_OVERLAY;
   }

   public static RenderLayer method_51786() {
      return GUI_TEXT_HIGHLIGHT;
   }

   public static RenderLayer method_51787() {
      return GUI_GHOST_RECIPE_OVERLAY;
   }

   public RenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
      super(name, startAction, endAction);
      this.vertexFormat = vertexFormat;
      this.drawMode = drawMode;
      this.expectedBufferSize = expectedBufferSize;
      this.hasCrumbling = hasCrumbling;
      this.translucent = translucent;
      this.optionalThis = Optional.of(this);
   }

   static MultiPhase of(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, MultiPhaseParameters phaseData) {
      return of(name, vertexFormat, drawMode, expectedBufferSize, false, false, phaseData);
   }

   private static MultiPhase of(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, MultiPhaseParameters phases) {
      return new MultiPhase(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, phases);
   }

   public void draw(BufferBuilder buffer, VertexSorter sorter) {
      if (buffer.isBuilding()) {
         if (this.translucent) {
            buffer.setSorter(sorter);
         }

         BufferBuilder.BuiltBuffer lv = buffer.end();
         this.startDrawing();
         BufferRenderer.drawWithGlobalProgram(lv);
         this.endDrawing();
      }
   }

   public String toString() {
      return this.name;
   }

   public static List getBlockLayers() {
      return BLOCK_LAYERS;
   }

   public int getExpectedBufferSize() {
      return this.expectedBufferSize;
   }

   public VertexFormat getVertexFormat() {
      return this.vertexFormat;
   }

   public VertexFormat.DrawMode getDrawMode() {
      return this.drawMode;
   }

   public Optional getAffectedOutline() {
      return Optional.empty();
   }

   public boolean isOutline() {
      return false;
   }

   public boolean hasCrumbling() {
      return this.hasCrumbling;
   }

   public boolean areVerticesNotShared() {
      return !this.drawMode.shareVertices;
   }

   public Optional asOptional() {
      return this.optionalThis;
   }

   static {
      SOLID = of("solid", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 2097152, true, false, RenderLayer.MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).program(SOLID_PROGRAM).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).build(true));
      CUTOUT_MIPPED = of("cutout_mipped", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 131072, true, false, RenderLayer.MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).program(CUTOUT_MIPPED_PROGRAM).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).build(true));
      CUTOUT = of("cutout", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 131072, true, false, RenderLayer.MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).program(CUTOUT_PROGRAM).texture(BLOCK_ATLAS_TEXTURE).build(true));
      TRANSLUCENT = of("translucent", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 2097152, true, true, of(TRANSLUCENT_PROGRAM));
      TRANSLUCENT_MOVING_BLOCK = of("translucent_moving_block", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 262144, false, true, getItemPhaseData());
      TRANSLUCENT_NO_CRUMBLING = of("translucent_no_crumbling", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 262144, false, true, of(TRANSLUCENT_NO_CRUMBLING_PROGRAM));
      ARMOR_CUTOUT_NO_CULL = Util.memoize((texture) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ARMOR_CUTOUT_NO_CULL_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(NO_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(VIEW_OFFSET_Z_LAYERING).build(true);
         return of("armor_cutout_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, false, lv);
      });
      ENTITY_SOLID = Util.memoize((texture) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_SOLID_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(NO_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true);
         return of("entity_solid", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, false, lv);
      });
      ENTITY_CUTOUT = Util.memoize((texture) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_CUTOUT_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(NO_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true);
         return of("entity_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, false, lv);
      });
      ENTITY_CUTOUT_NO_CULL = Util.memoize((texture, affectsOutline) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_CUTOUT_NONULL_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(NO_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(affectsOutline);
         return of("entity_cutout_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, false, lv);
      });
      ENTITY_CUTOUT_NO_CULL_Z_OFFSET = Util.memoize((texture, affectsOutline) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_CUTOUT_NONULL_OFFSET_Z_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(NO_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).layering(VIEW_OFFSET_Z_LAYERING).build(affectsOutline);
         return of("entity_cutout_no_cull_z_offset", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, false, lv);
      });
      ITEM_ENTITY_TRANSLUCENT_CULL = Util.memoize((texture) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ITEM_ENTITY_TRANSLUCENT_CULL_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_TARGET).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).writeMaskState(RenderPhase.ALL_MASK).build(true);
         return of("item_entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, true, lv);
      });
      ENTITY_TRANSLUCENT_CULL = Util.memoize((texture) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_TRANSLUCENT_CULL_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true);
         return of("entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, true, lv);
      });
      ENTITY_TRANSLUCENT = Util.memoize((texture, affectsOutline) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_TRANSLUCENT_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(affectsOutline);
         return of("entity_translucent", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, true, lv);
      });
      ENTITY_TRANSLUCENT_EMISSIVE = Util.memoize((texture, affectsOutline) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).cull(DISABLE_CULLING).writeMaskState(COLOR_MASK).overlay(ENABLE_OVERLAY_COLOR).build(affectsOutline);
         return of("entity_translucent_emissive", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, true, lv);
      });
      ENTITY_SMOOTH_CUTOUT = Util.memoize((texture) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_SMOOTH_CUTOUT_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).build(true);
         return of("entity_smooth_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, lv);
      });
      BEACON_BEAM = Util.memoize((texture, affectsOutline) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(BEACON_BEAM_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(affectsOutline ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY).writeMaskState(affectsOutline ? COLOR_MASK : ALL_MASK).build(false);
         return of("beacon_beam", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, true, lv);
      });
      ENTITY_DECAL = Util.memoize((texture) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_DECAL_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).depthTest(EQUAL_DEPTH_TEST).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(false);
         return of("entity_decal", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, lv);
      });
      ENTITY_NO_OUTLINE = Util.memoize((texture) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_NO_OUTLINE_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).writeMaskState(COLOR_MASK).build(false);
         return of("entity_no_outline", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, true, lv);
      });
      ENTITY_SHADOW = Util.memoize((texture) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_SHADOW_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).cull(ENABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).writeMaskState(COLOR_MASK).depthTest(LEQUAL_DEPTH_TEST).layering(VIEW_OFFSET_Z_LAYERING).build(false);
         return of("entity_shadow", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, false, lv);
      });
      ENTITY_ALPHA = Util.memoize((texture) -> {
         MultiPhaseParameters lv = RenderLayer.MultiPhaseParameters.builder().program(ENTITY_ALPHA_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).cull(DISABLE_CULLING).build(true);
         return of("entity_alpha", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, lv);
      });
      EYES = Util.memoize((texture) -> {
         RenderPhase.Texture lv = new RenderPhase.Texture(texture, false, false);
         return of("eyes", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(EYES_PROGRAM).texture(lv).transparency(ADDITIVE_TRANSPARENCY).writeMaskState(COLOR_MASK).build(false));
      });
      LEASH = of("leash", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.TRIANGLE_STRIP, 256, RenderLayer.MultiPhaseParameters.builder().program(LEASH_PROGRAM).texture(NO_TEXTURE).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).build(false));
      WATER_MASK = of("water_mask", VertexFormats.POSITION, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(WATER_MASK_PROGRAM).texture(NO_TEXTURE).writeMaskState(DEPTH_MASK).build(false));
      ARMOR_GLINT = of("armor_glint", VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(ARMOR_GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ENTITY_ENCHANTMENT_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(GLINT_TEXTURING).layering(VIEW_OFFSET_Z_LAYERING).build(false));
      ARMOR_ENTITY_GLINT = of("armor_entity_glint", VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(ARMOR_ENTITY_GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ENTITY_ENCHANTMENT_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(ENTITY_GLINT_TEXTURING).layering(VIEW_OFFSET_Z_LAYERING).build(false));
      GLINT_TRANSLUCENT = of("glint_translucent", VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(TRANSLUCENT_GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(GLINT_TEXTURING).target(ITEM_TARGET).build(false));
      GLINT = of("glint", VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(GLINT_TEXTURING).build(false));
      DIRECT_GLINT = of("glint_direct", VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(DIRECT_GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(GLINT_TEXTURING).build(false));
      ENTITY_GLINT = of("entity_glint", VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(ENTITY_GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ENTITY_ENCHANTMENT_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).target(ITEM_TARGET).texturing(ENTITY_GLINT_TEXTURING).build(false));
      DIRECT_ENTITY_GLINT = of("entity_glint_direct", VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(DIRECT_ENTITY_GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ENTITY_ENCHANTMENT_GLINT, true, false)).writeMaskState(COLOR_MASK).cull(DISABLE_CULLING).depthTest(EQUAL_DEPTH_TEST).transparency(GLINT_TRANSPARENCY).texturing(ENTITY_GLINT_TEXTURING).build(false));
      CRUMBLING = Util.memoize((texture) -> {
         RenderPhase.Texture lv = new RenderPhase.Texture(texture, false, false);
         return of("crumbling", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(CRUMBLING_PROGRAM).texture(lv).transparency(CRUMBLING_TRANSPARENCY).writeMaskState(COLOR_MASK).layering(POLYGON_OFFSET_LAYERING).build(false));
      });
      TEXT = Util.memoize((texture) -> {
         return of("text", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(TEXT_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).build(false));
      });
      TEXT_BACKGROUND = of("text_background", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(TEXT_BACKGROUND_PROGRAM).texture(NO_TEXTURE).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).build(false));
      TEXT_INTENSITY = Util.memoize((texture) -> {
         return of("text_intensity", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(TEXT_INTENSITY_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).build(false));
      });
      TEXT_POLYGON_OFFSET = Util.memoize((texture) -> {
         return of("text_polygon_offset", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(TEXT_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).layering(POLYGON_OFFSET_LAYERING).build(false));
      });
      TEXT_INTENSITY_POLYGON_OFFSET = Util.memoize((texture) -> {
         return of("text_intensity_polygon_offset", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(TEXT_INTENSITY_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).layering(POLYGON_OFFSET_LAYERING).build(false));
      });
      TEXT_SEE_THROUGH = Util.memoize((texture) -> {
         return of("text_see_through", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(TRANSPARENT_TEXT_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).depthTest(ALWAYS_DEPTH_TEST).writeMaskState(COLOR_MASK).build(false));
      });
      TEXT_BACKGROUND_SEE_THROUGH = of("text_background_see_through", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(TRANSPARENT_TEXT_BACKGROUND_PROGRAM).texture(NO_TEXTURE).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).depthTest(ALWAYS_DEPTH_TEST).writeMaskState(COLOR_MASK).build(false));
      TEXT_INTENSITY_SEE_THROUGH = Util.memoize((texture) -> {
         return of("text_intensity_see_through", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(TRANSPARENT_TEXT_INTENSITY_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).depthTest(ALWAYS_DEPTH_TEST).writeMaskState(COLOR_MASK).build(false));
      });
      LIGHTNING = of("lightning", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder().program(LIGHTNING_PROGRAM).writeMaskState(ALL_MASK).transparency(LIGHTNING_TRANSPARENCY).target(WEATHER_TARGET).build(false));
      TRIPWIRE = of("tripwire", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 262144, true, true, getTripwirePhaseData());
      END_PORTAL = of("end_portal", VertexFormats.POSITION, VertexFormat.DrawMode.QUADS, 256, false, false, RenderLayer.MultiPhaseParameters.builder().program(END_PORTAL_PROGRAM).texture(RenderPhase.Textures.create().add(EndPortalBlockEntityRenderer.SKY_TEXTURE, false, false).add(EndPortalBlockEntityRenderer.PORTAL_TEXTURE, false, false).build()).build(false));
      END_GATEWAY = of("end_gateway", VertexFormats.POSITION, VertexFormat.DrawMode.QUADS, 256, false, false, RenderLayer.MultiPhaseParameters.builder().program(END_GATEWAY_PROGRAM).texture(RenderPhase.Textures.create().add(EndPortalBlockEntityRenderer.SKY_TEXTURE, false, false).add(EndPortalBlockEntityRenderer.PORTAL_TEXTURE, false, false).build()).build(false));
      LINES = of("lines", VertexFormats.LINES, VertexFormat.DrawMode.LINES, 256, RenderLayer.MultiPhaseParameters.builder().program(LINES_PROGRAM).lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty())).layering(VIEW_OFFSET_Z_LAYERING).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_TARGET).writeMaskState(ALL_MASK).cull(DISABLE_CULLING).build(false));
      LINE_STRIP = of("line_strip", VertexFormats.LINES, VertexFormat.DrawMode.LINE_STRIP, 256, RenderLayer.MultiPhaseParameters.builder().program(LINES_PROGRAM).lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty())).layering(VIEW_OFFSET_Z_LAYERING).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_TARGET).writeMaskState(ALL_MASK).cull(DISABLE_CULLING).build(false));
      DEBUG_LINE_STRIP = Util.memoize((lineWidth) -> {
         return of("debug_line_strip", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINE_STRIP, 256, RenderLayer.MultiPhaseParameters.builder().program(COLOR_PROGRAM).lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(lineWidth))).transparency(NO_TRANSPARENCY).cull(DISABLE_CULLING).build(false));
      });
      DEBUG_FILLED_BOX = of("debug_filled_box", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP, 131072, false, true, RenderLayer.MultiPhaseParameters.builder().program(COLOR_PROGRAM).layering(VIEW_OFFSET_Z_LAYERING).transparency(TRANSLUCENT_TRANSPARENCY).build(false));
      DEBUG_QUADS = of("debug_quads", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 131072, false, true, RenderLayer.MultiPhaseParameters.builder().program(COLOR_PROGRAM).transparency(TRANSLUCENT_TRANSPARENCY).cull(DISABLE_CULLING).build(false));
      DEBUG_SECTION_QUADS = of("debug_section_quads", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 131072, false, true, RenderLayer.MultiPhaseParameters.builder().program(COLOR_PROGRAM).layering(VIEW_OFFSET_Z_LAYERING).transparency(TRANSLUCENT_TRANSPARENCY).cull(ENABLE_CULLING).build(false));
      GUI = of("gui", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(field_44817).transparency(TRANSLUCENT_TRANSPARENCY).depthTest(LEQUAL_DEPTH_TEST).build(false));
      GUI_OVERLAY = of("gui_overlay", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(field_44818).transparency(TRANSLUCENT_TRANSPARENCY).depthTest(ALWAYS_DEPTH_TEST).writeMaskState(COLOR_MASK).build(false));
      GUI_TEXT_HIGHLIGHT = of("gui_text_highlight", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(field_44819).transparency(TRANSLUCENT_TRANSPARENCY).depthTest(ALWAYS_DEPTH_TEST).method_51788(field_44816).build(false));
      GUI_GHOST_RECIPE_OVERLAY = of("gui_ghost_recipe_overlay", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(field_44820).transparency(TRANSLUCENT_TRANSPARENCY).depthTest(field_44814).writeMaskState(COLOR_MASK).build(false));
      BLOCK_LAYERS = ImmutableList.of(getSolid(), getCutoutMipped(), getCutout(), getTranslucent(), getTripwire());
   }

   @Environment(EnvType.CLIENT)
   protected static final class MultiPhaseParameters {
      final RenderPhase.TextureBase texture;
      private final RenderPhase.ShaderProgram program;
      private final RenderPhase.Transparency transparency;
      private final RenderPhase.DepthTest depthTest;
      final RenderPhase.Cull cull;
      private final RenderPhase.Lightmap lightmap;
      private final RenderPhase.Overlay overlay;
      private final RenderPhase.Layering layering;
      private final RenderPhase.Target target;
      private final RenderPhase.Texturing texturing;
      private final RenderPhase.WriteMaskState writeMaskState;
      private final RenderPhase.LineWidth lineWidth;
      private final RenderPhase.class_8559 field_44825;
      final OutlineMode outlineMode;
      final ImmutableList phases;

      MultiPhaseParameters(RenderPhase.TextureBase texture, RenderPhase.ShaderProgram program, RenderPhase.Transparency transparency, RenderPhase.DepthTest depthTest, RenderPhase.Cull cull, RenderPhase.Lightmap lightmap, RenderPhase.Overlay overlay, RenderPhase.Layering layering, RenderPhase.Target target, RenderPhase.Texturing texturing, RenderPhase.WriteMaskState writeMaskState, RenderPhase.LineWidth lineWidth, RenderPhase.class_8559 arg13, OutlineMode arg14) {
         this.texture = texture;
         this.program = program;
         this.transparency = transparency;
         this.depthTest = depthTest;
         this.cull = cull;
         this.lightmap = lightmap;
         this.overlay = overlay;
         this.layering = layering;
         this.target = target;
         this.texturing = texturing;
         this.writeMaskState = writeMaskState;
         this.lineWidth = lineWidth;
         this.field_44825 = arg13;
         this.outlineMode = arg14;
         this.phases = ImmutableList.of(this.texture, this.program, this.transparency, this.depthTest, this.cull, this.lightmap, this.overlay, this.layering, this.target, this.texturing, this.writeMaskState, this.field_44825, new RenderPhase[]{this.lineWidth});
      }

      public String toString() {
         return "CompositeState[" + this.phases + ", outlineProperty=" + this.outlineMode + "]";
      }

      public static Builder builder() {
         return new Builder();
      }

      @Environment(EnvType.CLIENT)
      public static class Builder {
         private RenderPhase.TextureBase texture;
         private RenderPhase.ShaderProgram program;
         private RenderPhase.Transparency transparency;
         private RenderPhase.DepthTest depthTest;
         private RenderPhase.Cull cull;
         private RenderPhase.Lightmap lightmap;
         private RenderPhase.Overlay overlay;
         private RenderPhase.Layering layering;
         private RenderPhase.Target target;
         private RenderPhase.Texturing texturing;
         private RenderPhase.WriteMaskState writeMaskState;
         private RenderPhase.LineWidth lineWidth;
         private RenderPhase.class_8559 field_44826;

         Builder() {
            this.texture = RenderPhase.NO_TEXTURE;
            this.program = RenderPhase.NO_PROGRAM;
            this.transparency = RenderPhase.NO_TRANSPARENCY;
            this.depthTest = RenderPhase.LEQUAL_DEPTH_TEST;
            this.cull = RenderPhase.ENABLE_CULLING;
            this.lightmap = RenderPhase.DISABLE_LIGHTMAP;
            this.overlay = RenderPhase.DISABLE_OVERLAY_COLOR;
            this.layering = RenderPhase.NO_LAYERING;
            this.target = RenderPhase.MAIN_TARGET;
            this.texturing = RenderPhase.DEFAULT_TEXTURING;
            this.writeMaskState = RenderPhase.ALL_MASK;
            this.lineWidth = RenderPhase.FULL_LINE_WIDTH;
            this.field_44826 = RenderPhase.field_44815;
         }

         public Builder texture(RenderPhase.TextureBase texture) {
            this.texture = texture;
            return this;
         }

         public Builder program(RenderPhase.ShaderProgram program) {
            this.program = program;
            return this;
         }

         public Builder transparency(RenderPhase.Transparency transparency) {
            this.transparency = transparency;
            return this;
         }

         public Builder depthTest(RenderPhase.DepthTest depthTest) {
            this.depthTest = depthTest;
            return this;
         }

         public Builder cull(RenderPhase.Cull cull) {
            this.cull = cull;
            return this;
         }

         public Builder lightmap(RenderPhase.Lightmap lightmap) {
            this.lightmap = lightmap;
            return this;
         }

         public Builder overlay(RenderPhase.Overlay overlay) {
            this.overlay = overlay;
            return this;
         }

         public Builder layering(RenderPhase.Layering layering) {
            this.layering = layering;
            return this;
         }

         public Builder target(RenderPhase.Target target) {
            this.target = target;
            return this;
         }

         public Builder texturing(RenderPhase.Texturing texturing) {
            this.texturing = texturing;
            return this;
         }

         public Builder writeMaskState(RenderPhase.WriteMaskState writeMaskState) {
            this.writeMaskState = writeMaskState;
            return this;
         }

         public Builder lineWidth(RenderPhase.LineWidth lineWidth) {
            this.lineWidth = lineWidth;
            return this;
         }

         public Builder method_51788(RenderPhase.class_8559 arg) {
            this.field_44826 = arg;
            return this;
         }

         public MultiPhaseParameters build(boolean affectsOutline) {
            return this.build(affectsOutline ? RenderLayer.OutlineMode.AFFECTS_OUTLINE : RenderLayer.OutlineMode.NONE);
         }

         public MultiPhaseParameters build(OutlineMode arg) {
            return new MultiPhaseParameters(this.texture, this.program, this.transparency, this.depthTest, this.cull, this.lightmap, this.overlay, this.layering, this.target, this.texturing, this.writeMaskState, this.lineWidth, this.field_44826, arg);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class MultiPhase extends RenderLayer {
      static final BiFunction CULLING_LAYERS = Util.memoize((texture, culling) -> {
         return RenderLayer.of("outline", VertexFormats.POSITION_COLOR_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder().program(OUTLINE_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).cull(culling).depthTest(ALWAYS_DEPTH_TEST).target(OUTLINE_TARGET).build(RenderLayer.OutlineMode.IS_OUTLINE));
      });
      private final MultiPhaseParameters phases;
      private final Optional affectedOutline;
      private final boolean outline;

      MultiPhase(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, MultiPhaseParameters phases) {
         super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, () -> {
            phases.phases.forEach(RenderPhase::startDrawing);
         }, () -> {
            phases.phases.forEach(RenderPhase::endDrawing);
         });
         this.phases = phases;
         this.affectedOutline = phases.outlineMode == RenderLayer.OutlineMode.AFFECTS_OUTLINE ? phases.texture.getId().map((texture) -> {
            return (RenderLayer)CULLING_LAYERS.apply(texture, phases.cull);
         }) : Optional.empty();
         this.outline = phases.outlineMode == RenderLayer.OutlineMode.IS_OUTLINE;
      }

      public Optional getAffectedOutline() {
         return this.affectedOutline;
      }

      public boolean isOutline() {
         return this.outline;
      }

      protected final MultiPhaseParameters getPhases() {
         return this.phases;
      }

      public String toString() {
         return "RenderType[" + this.name + ":" + this.phases + "]";
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum OutlineMode {
      NONE("none"),
      IS_OUTLINE("is_outline"),
      AFFECTS_OUTLINE("affects_outline");

      private final String name;

      private OutlineMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      // $FF: synthetic method
      private static OutlineMode[] method_36916() {
         return new OutlineMode[]{NONE, IS_OUTLINE, AFFECTS_OUTLINE};
      }
   }
}
