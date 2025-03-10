package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ParticleEffectArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("foo", "foo:bar", "particle with options");
   public static final DynamicCommandExceptionType UNKNOWN_PARTICLE_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return Text.translatable("particle.notFound", id);
   });
   private final RegistryWrapper registryWrapper;

   public ParticleEffectArgumentType(CommandRegistryAccess registryAccess) {
      this.registryWrapper = registryAccess.createWrapper(RegistryKeys.PARTICLE_TYPE);
   }

   public static ParticleEffectArgumentType particleEffect(CommandRegistryAccess registryAccess) {
      return new ParticleEffectArgumentType(registryAccess);
   }

   public static ParticleEffect getParticle(CommandContext context, String name) {
      return (ParticleEffect)context.getArgument(name, ParticleEffect.class);
   }

   public ParticleEffect parse(StringReader stringReader) throws CommandSyntaxException {
      return readParameters(stringReader, this.registryWrapper);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   public static ParticleEffect readParameters(StringReader reader, RegistryWrapper registryWrapper) throws CommandSyntaxException {
      ParticleType lv = getType(reader, registryWrapper);
      return readParameters(reader, lv);
   }

   private static ParticleType getType(StringReader reader, RegistryWrapper registryWrapper) throws CommandSyntaxException {
      Identifier lv = Identifier.fromCommandInput(reader);
      RegistryKey lv2 = RegistryKey.of(RegistryKeys.PARTICLE_TYPE, lv);
      return (ParticleType)((RegistryEntry.Reference)registryWrapper.getOptional(lv2).orElseThrow(() -> {
         return UNKNOWN_PARTICLE_EXCEPTION.create(lv);
      })).value();
   }

   private static ParticleEffect readParameters(StringReader reader, ParticleType type) throws CommandSyntaxException {
      return type.getParametersFactory().read(type, reader);
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      return CommandSource.suggestIdentifiers(this.registryWrapper.streamKeys().map(RegistryKey::getValue), builder);
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }
}
