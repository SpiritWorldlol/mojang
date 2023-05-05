package net.minecraft.client.font;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringIdentifiable;

@Environment(EnvType.CLIENT)
public enum FontType implements StringIdentifiable {
   BITMAP("bitmap", BitmapFont.Loader.CODEC),
   TTF("ttf", TrueTypeFontLoader.CODEC),
   SPACE("space", SpaceFont.Loader.CODEC),
   UNIHEX("unihex", UnihexFont.Loader.CODEC),
   REFERENCE("reference", ReferenceFont.CODEC);

   public static final com.mojang.serialization.Codec CODEC = StringIdentifiable.createCodec(FontType::values);
   private final String id;
   private final MapCodec codec;

   private FontType(String id, MapCodec codec) {
      this.id = id;
      this.codec = codec;
   }

   public String asString() {
      return this.id;
   }

   public MapCodec getLoaderCodec() {
      return this.codec;
   }

   // $FF: synthetic method
   private static FontType[] method_36876() {
      return new FontType[]{BITMAP, TTF, SPACE, UNIHEX, REFERENCE};
   }
}
