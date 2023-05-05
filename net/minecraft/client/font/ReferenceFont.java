package net.minecraft.client.font;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ReferenceFont implements FontLoader {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Identifier.CODEC.fieldOf("id").forGetter((font) -> {
         return font.id;
      })).apply(instance, ReferenceFont::new);
   });
   private final Identifier id;

   private ReferenceFont(Identifier id) {
      this.id = id;
   }

   public static FontLoader fromJson(JsonObject json) {
      return (FontLoader)CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, (error) -> {
      });
   }

   public Either build() {
      return Either.right(new FontLoader.Reference(this.id));
   }
}
