package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public interface SoundContainer {
   int getWeight();

   Object getSound(Random random);

   void preload(SoundSystem soundSystem);
}
