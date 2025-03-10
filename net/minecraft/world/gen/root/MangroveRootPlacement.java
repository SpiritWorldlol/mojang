package net.minecraft.world.gen.root;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record MangroveRootPlacement(RegistryEntryList canGrowThrough, RegistryEntryList muddyRootsIn, BlockStateProvider muddyRootsProvider, int maxRootWidth, int maxRootLength, float randomSkewChance) {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("can_grow_through").forGetter((rootPlacement) -> {
         return rootPlacement.canGrowThrough;
      }), RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("muddy_roots_in").forGetter((rootPlacement) -> {
         return rootPlacement.muddyRootsIn;
      }), BlockStateProvider.TYPE_CODEC.fieldOf("muddy_roots_provider").forGetter((rootPlacement) -> {
         return rootPlacement.muddyRootsProvider;
      }), Codec.intRange(1, 12).fieldOf("max_root_width").forGetter((rootPlacement) -> {
         return rootPlacement.maxRootWidth;
      }), Codec.intRange(1, 64).fieldOf("max_root_length").forGetter((rootPlacement) -> {
         return rootPlacement.maxRootLength;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("random_skew_chance").forGetter((rootPlacement) -> {
         return rootPlacement.randomSkewChance;
      })).apply(instance, MangroveRootPlacement::new);
   });

   public MangroveRootPlacement(RegistryEntryList arg, RegistryEntryList arg2, BlockStateProvider arg3, int i, int j, float f) {
      this.canGrowThrough = arg;
      this.muddyRootsIn = arg2;
      this.muddyRootsProvider = arg3;
      this.maxRootWidth = i;
      this.maxRootLength = j;
      this.randomSkewChance = f;
   }

   public RegistryEntryList canGrowThrough() {
      return this.canGrowThrough;
   }

   public RegistryEntryList muddyRootsIn() {
      return this.muddyRootsIn;
   }

   public BlockStateProvider muddyRootsProvider() {
      return this.muddyRootsProvider;
   }

   public int maxRootWidth() {
      return this.maxRootWidth;
   }

   public int maxRootLength() {
      return this.maxRootLength;
   }

   public float randomSkewChance() {
      return this.randomSkewChance;
   }
}
