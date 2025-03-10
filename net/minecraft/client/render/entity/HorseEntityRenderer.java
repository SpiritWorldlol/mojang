package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.HorseArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.HorseMarkingFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public final class HorseEntityRenderer extends AbstractHorseEntityRenderer {
   private static final Map TEXTURES = (Map)Util.make(Maps.newEnumMap(HorseColor.class), (map) -> {
      map.put(HorseColor.WHITE, new Identifier("textures/entity/horse/horse_white.png"));
      map.put(HorseColor.CREAMY, new Identifier("textures/entity/horse/horse_creamy.png"));
      map.put(HorseColor.CHESTNUT, new Identifier("textures/entity/horse/horse_chestnut.png"));
      map.put(HorseColor.BROWN, new Identifier("textures/entity/horse/horse_brown.png"));
      map.put(HorseColor.BLACK, new Identifier("textures/entity/horse/horse_black.png"));
      map.put(HorseColor.GRAY, new Identifier("textures/entity/horse/horse_gray.png"));
      map.put(HorseColor.DARK_BROWN, new Identifier("textures/entity/horse/horse_darkbrown.png"));
   });

   public HorseEntityRenderer(EntityRendererFactory.Context arg) {
      super(arg, new HorseEntityModel(arg.getPart(EntityModelLayers.HORSE)), 1.1F);
      this.addFeature(new HorseMarkingFeatureRenderer(this));
      this.addFeature(new HorseArmorFeatureRenderer(this, arg.getModelLoader()));
   }

   public Identifier getTexture(HorseEntity arg) {
      return (Identifier)TEXTURES.get(arg.getVariant());
   }
}
