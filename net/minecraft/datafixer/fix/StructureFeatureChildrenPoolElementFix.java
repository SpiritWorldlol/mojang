package net.minecraft.datafixer.fix;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class StructureFeatureChildrenPoolElementFix extends DataFix {
   private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("\\[(\\d+)\\]");
   private static final Set TARGET_CHILDREN_IDS = Sets.newHashSet(new String[]{"minecraft:jigsaw", "minecraft:nvi", "minecraft:pcp", "minecraft:bastionremnant", "minecraft:runtime"});
   private static final Set TARGET_FEATURES = Sets.newHashSet(new String[]{"minecraft:tree", "minecraft:flower", "minecraft:block_pile", "minecraft:random_patch"});

   public StructureFeatureChildrenPoolElementFix(Schema outputSchema) {
      super(outputSchema, false);
   }

   public TypeRewriteRule makeRule() {
      return this.writeFixAndRead("SavedDataFeaturePoolElementFix", this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE), this.getOutputSchema().getType(TypeReferences.STRUCTURE_FEATURE), StructureFeatureChildrenPoolElementFix::updateStructureFeature);
   }

   private static Dynamic updateStructureFeature(Dynamic structureFeature) {
      return structureFeature.update("Children", StructureFeatureChildrenPoolElementFix::updateChildren);
   }

   private static Dynamic updateChildren(Dynamic children) {
      DataResult var10000 = children.asStreamOpt().map(StructureFeatureChildrenPoolElementFix::mapChildren);
      Objects.requireNonNull(children);
      return (Dynamic)var10000.map(children::createList).result().orElse(children);
   }

   private static Stream mapChildren(Stream children) {
      return children.map((child) -> {
         String string = child.get("id").asString("");
         if (!TARGET_CHILDREN_IDS.contains(string)) {
            return child;
         } else {
            OptionalDynamic optionalDynamic = child.get("pool_element");
            return !optionalDynamic.get("element_type").asString("").equals("minecraft:feature_pool_element") ? child : child.update("pool_element", (poolElement) -> {
               return poolElement.update("feature", StructureFeatureChildrenPoolElementFix::updatePoolElementFeature);
            });
         }
      });
   }

   private static OptionalDynamic findValueAt(Dynamic root, String... pathParts) {
      if (pathParts.length == 0) {
         throw new IllegalArgumentException("Missing path");
      } else {
         OptionalDynamic optionalDynamic = root.get(pathParts[0]);

         for(int i = 1; i < pathParts.length; ++i) {
            String string = pathParts[i];
            Matcher matcher = ARRAY_INDEX_PATTERN.matcher(string);
            if (matcher.matches()) {
               int j = Integer.parseInt(matcher.group(1));
               List list = optionalDynamic.asList(Function.identity());
               if (j >= 0 && j < list.size()) {
                  optionalDynamic = new OptionalDynamic(root.getOps(), DataResult.success((Dynamic)list.get(j)));
               } else {
                  optionalDynamic = new OptionalDynamic(root.getOps(), DataResult.error(() -> {
                     return "Missing id:" + j;
                  }));
               }
            } else {
               optionalDynamic = optionalDynamic.get(string);
            }
         }

         return optionalDynamic;
      }
   }

   @VisibleForTesting
   protected static Dynamic updatePoolElementFeature(Dynamic feature) {
      Optional optional = updateFeature(findValueAt(feature, "type").asString(""), findValueAt(feature, "name").asString(""), findValueAt(feature, "config", "state_provider", "type").asString(""), findValueAt(feature, "config", "state_provider", "state", "Name").asString(""), findValueAt(feature, "config", "state_provider", "entries", "[0]", "data", "Name").asString(""), findValueAt(feature, "config", "foliage_placer", "type").asString(""), findValueAt(feature, "config", "leaves_provider", "state", "Name").asString(""));
      return optional.isPresent() ? feature.createString((String)optional.get()) : feature;
   }

   private static Optional updateFeature(String type, String name, String stateProviderType, String stateProviderStateName, String stateProviderFirstEntryName, String foliagePlacerType, String leavesProviderStateName) {
      String string8;
      if (!type.isEmpty()) {
         string8 = type;
      } else {
         if (name.isEmpty()) {
            return Optional.empty();
         }

         if ("minecraft:normal_tree".equals(name)) {
            string8 = "minecraft:tree";
         } else {
            string8 = name;
         }
      }

      if (TARGET_FEATURES.contains(string8)) {
         if ("minecraft:random_patch".equals(string8)) {
            if ("minecraft:simple_state_provider".equals(stateProviderType)) {
               if ("minecraft:sweet_berry_bush".equals(stateProviderStateName)) {
                  return Optional.of("minecraft:patch_berry_bush");
               }

               if ("minecraft:cactus".equals(stateProviderStateName)) {
                  return Optional.of("minecraft:patch_cactus");
               }
            } else if ("minecraft:weighted_state_provider".equals(stateProviderType) && ("minecraft:grass".equals(stateProviderFirstEntryName) || "minecraft:fern".equals(stateProviderFirstEntryName))) {
               return Optional.of("minecraft:patch_taiga_grass");
            }
         } else if ("minecraft:block_pile".equals(string8)) {
            if (!"minecraft:simple_state_provider".equals(stateProviderType) && !"minecraft:rotated_block_provider".equals(stateProviderType)) {
               if ("minecraft:weighted_state_provider".equals(stateProviderType)) {
                  if ("minecraft:packed_ice".equals(stateProviderFirstEntryName) || "minecraft:blue_ice".equals(stateProviderFirstEntryName)) {
                     return Optional.of("minecraft:pile_ice");
                  }

                  if ("minecraft:jack_o_lantern".equals(stateProviderFirstEntryName) || "minecraft:pumpkin".equals(stateProviderFirstEntryName)) {
                     return Optional.of("minecraft:pile_pumpkin");
                  }
               }
            } else {
               if ("minecraft:hay_block".equals(stateProviderStateName)) {
                  return Optional.of("minecraft:pile_hay");
               }

               if ("minecraft:melon".equals(stateProviderStateName)) {
                  return Optional.of("minecraft:pile_melon");
               }

               if ("minecraft:snow".equals(stateProviderStateName)) {
                  return Optional.of("minecraft:pile_snow");
               }
            }
         } else {
            if ("minecraft:flower".equals(string8)) {
               return Optional.of("minecraft:flower_plain");
            }

            if ("minecraft:tree".equals(string8)) {
               if ("minecraft:acacia_foliage_placer".equals(foliagePlacerType)) {
                  return Optional.of("minecraft:acacia");
               }

               if ("minecraft:blob_foliage_placer".equals(foliagePlacerType) && "minecraft:oak_leaves".equals(leavesProviderStateName)) {
                  return Optional.of("minecraft:oak");
               }

               if ("minecraft:pine_foliage_placer".equals(foliagePlacerType)) {
                  return Optional.of("minecraft:pine");
               }

               if ("minecraft:spruce_foliage_placer".equals(foliagePlacerType)) {
                  return Optional.of("minecraft:spruce");
               }
            }
         }
      }

      return Optional.empty();
   }
}
