package net.minecraft.client.resource;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.util.RawTextureDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Environment(EnvType.CLIENT)
public class GrassColormapResourceSupplier extends SinglePreparationResourceReloader {
   private static final Identifier GRASS_COLORMAP_LOC = new Identifier("textures/colormap/grass.png");

   protected int[] tryLoad(ResourceManager resourceManager, Profiler profiler) {
      try {
         return RawTextureDataLoader.loadRawTextureData(resourceManager, GRASS_COLORMAP_LOC);
      } catch (IOException var4) {
         throw new IllegalStateException("Failed to load grass color texture", var4);
      }
   }

   protected void apply(int[] is, ResourceManager arg, Profiler arg2) {
      GrassColors.setColorMap(is);
   }

   // $FF: synthetic method
   protected Object prepare(ResourceManager manager, Profiler profiler) {
      return this.tryLoad(manager, profiler);
   }
}
