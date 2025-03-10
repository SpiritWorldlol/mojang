package net.minecraft.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class DustColorTransitionParticleEffect extends AbstractDustParticleEffect {
   public static final Vector3f SCULK_BLUE = Vec3d.unpackRgb(3790560).toVector3f();
   public static final DustColorTransitionParticleEffect DEFAULT;
   public static final Codec CODEC;
   public static final ParticleEffect.Factory FACTORY;
   private final Vector3f toColor;

   public DustColorTransitionParticleEffect(Vector3f fromColor, Vector3f toColor, float scale) {
      super(fromColor, scale);
      this.toColor = toColor;
   }

   public Vector3f getFromColor() {
      return this.color;
   }

   public Vector3f getToColor() {
      return this.toColor;
   }

   public void write(PacketByteBuf buf) {
      super.write(buf);
      buf.writeFloat(this.toColor.x());
      buf.writeFloat(this.toColor.y());
      buf.writeFloat(this.toColor.z());
   }

   public String asString() {
      return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f %.2f", Registries.PARTICLE_TYPE.getId(this.getType()), this.color.x(), this.color.y(), this.color.z(), this.scale, this.toColor.x(), this.toColor.y(), this.toColor.z());
   }

   public ParticleType getType() {
      return ParticleTypes.DUST_COLOR_TRANSITION;
   }

   static {
      DEFAULT = new DustColorTransitionParticleEffect(SCULK_BLUE, DustParticleEffect.RED, 1.0F);
      CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codecs.VECTOR_3F.fieldOf("fromColor").forGetter((effect) -> {
            return effect.color;
         }), Codecs.VECTOR_3F.fieldOf("toColor").forGetter((effect) -> {
            return effect.toColor;
         }), Codec.FLOAT.fieldOf("scale").forGetter((effect) -> {
            return effect.scale;
         })).apply(instance, DustColorTransitionParticleEffect::new);
      });
      FACTORY = new ParticleEffect.Factory() {
         public DustColorTransitionParticleEffect read(ParticleType arg, StringReader stringReader) throws CommandSyntaxException {
            Vector3f vector3f = AbstractDustParticleEffect.readColor(stringReader);
            stringReader.expect(' ');
            float f = stringReader.readFloat();
            Vector3f vector3f2 = AbstractDustParticleEffect.readColor(stringReader);
            return new DustColorTransitionParticleEffect(vector3f, vector3f2, f);
         }

         public DustColorTransitionParticleEffect read(ParticleType arg, PacketByteBuf arg2) {
            Vector3f vector3f = AbstractDustParticleEffect.readColor(arg2);
            float f = arg2.readFloat();
            Vector3f vector3f2 = AbstractDustParticleEffect.readColor(arg2);
            return new DustColorTransitionParticleEffect(vector3f, vector3f2, f);
         }

         // $FF: synthetic method
         public ParticleEffect read(ParticleType type, PacketByteBuf buf) {
            return this.read(type, buf);
         }

         // $FF: synthetic method
         public ParticleEffect read(ParticleType type, StringReader reader) throws CommandSyntaxException {
            return this.read(type, reader);
         }
      };
   }
}
