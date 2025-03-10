package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.math.WordPackedArray;
import org.jetbrains.annotations.Nullable;

public class LeavesFix extends DataFix {
   private static final int field_29886 = 128;
   private static final int field_29887 = 64;
   private static final int field_29888 = 32;
   private static final int field_29889 = 16;
   private static final int field_29890 = 8;
   private static final int field_29891 = 4;
   private static final int field_29892 = 2;
   private static final int field_29893 = 1;
   private static final int[][] field_5687 = new int[][]{{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
   private static final int field_29894 = 7;
   private static final int field_29895 = 12;
   private static final int field_29896 = 4096;
   static final Object2IntMap LEAVES_MAP = (Object2IntMap)DataFixUtils.make(new Object2IntOpenHashMap(), (map) -> {
      map.put("minecraft:acacia_leaves", 0);
      map.put("minecraft:birch_leaves", 1);
      map.put("minecraft:dark_oak_leaves", 2);
      map.put("minecraft:jungle_leaves", 3);
      map.put("minecraft:oak_leaves", 4);
      map.put("minecraft:spruce_leaves", 5);
   });
   static final Set LOGS_MAP = ImmutableSet.of("minecraft:acacia_bark", "minecraft:birch_bark", "minecraft:dark_oak_bark", "minecraft:jungle_bark", "minecraft:oak_bark", "minecraft:spruce_bark", new String[]{"minecraft:acacia_log", "minecraft:birch_log", "minecraft:dark_oak_log", "minecraft:jungle_log", "minecraft:oak_log", "minecraft:spruce_log", "minecraft:stripped_acacia_log", "minecraft:stripped_birch_log", "minecraft:stripped_dark_oak_log", "minecraft:stripped_jungle_log", "minecraft:stripped_oak_log", "minecraft:stripped_spruce_log"});

   public LeavesFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   protected TypeRewriteRule makeRule() {
      Type type = this.getInputSchema().getType(TypeReferences.CHUNK);
      OpticFinder opticFinder = type.findField("Level");
      OpticFinder opticFinder2 = opticFinder.type().findField("Sections");
      Type type2 = opticFinder2.type();
      if (!(type2 instanceof List.ListType)) {
         throw new IllegalStateException("Expecting sections to be a list.");
      } else {
         Type type3 = ((List.ListType)type2).getElement();
         OpticFinder opticFinder3 = DSL.typeFinder(type3);
         return this.fixTypeEverywhereTyped("Leaves fix", type, (typed) -> {
            return typed.updateTyped(opticFinder, (typedx) -> {
               int[] is = new int[]{0};
               Typed typed2 = typedx.updateTyped(opticFinder2, (typed) -> {
                  Int2ObjectMap int2ObjectMap = new Int2ObjectOpenHashMap((Map)typed.getAllTyped(opticFinder3).stream().map((typedx) -> {
                     return new LeavesLogFixer(typedx, this.getInputSchema());
                  }).collect(Collectors.toMap(ListFixer::getY, (arg) -> {
                     return arg;
                  })));
                  if (int2ObjectMap.values().stream().allMatch(ListFixer::isFixed)) {
                     return typed;
                  } else {
                     java.util.List list = Lists.newArrayList();

                     int i;
                     for(i = 0; i < 7; ++i) {
                        list.add(new IntOpenHashSet());
                     }

                     ObjectIterator var25 = int2ObjectMap.values().iterator();

                     while(true) {
                        LeavesLogFixer lv;
                        int l;
                        int m;
                        do {
                           if (!var25.hasNext()) {
                              for(i = 1; i < 7; ++i) {
                                 IntSet intSet = (IntSet)list.get(i - 1);
                                 IntSet intSet2 = (IntSet)list.get(i);
                                 IntIterator intIterator = intSet.iterator();

                                 while(intIterator.hasNext()) {
                                    l = intIterator.nextInt();
                                    m = this.method_5052(l);
                                    int n = this.method_5062(l);
                                    int o = this.method_5050(l);
                                    int[][] var14 = field_5687;
                                    int var15 = var14.length;

                                    for(int var16 = 0; var16 < var15; ++var16) {
                                       int[] js = var14[var16];
                                       int p = m + js[0];
                                       int q = n + js[1];
                                       int r = o + js[2];
                                       if (p >= 0 && p <= 15 && r >= 0 && r <= 15 && q >= 0 && q <= 255) {
                                          LeavesLogFixer lv2 = (LeavesLogFixer)int2ObjectMap.get(q >> 4);
                                          if (lv2 != null && !lv2.isFixed()) {
                                             int s = method_5051(p, q & 15, r);
                                             int t = lv2.needsFix(s);
                                             if (lv2.isLeaf(t)) {
                                                int u = lv2.getDistanceToLog(t);
                                                if (u > i) {
                                                   lv2.computeLeafStates(s, t, i);
                                                   intSet2.add(method_5051(p, q, r));
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }

                              return typed.updateTyped(opticFinder3, (typedx) -> {
                                 return ((LeavesLogFixer)int2ObjectMap.get(((Dynamic)typedx.get(DSL.remainderFinder())).get("Y").asInt(0))).method_5083(typedx);
                              });
                           }

                           lv = (LeavesLogFixer)var25.next();
                        } while(lv.isFixed());

                        for(int j = 0; j < 4096; ++j) {
                           int k = lv.needsFix(j);
                           if (lv.isLog(k)) {
                              ((IntSet)list.get(0)).add(lv.getY() << 12 | j);
                           } else if (lv.isLeaf(k)) {
                              l = this.method_5052(j);
                              m = this.method_5050(j);
                              is[0] |= method_5061(l == 0, l == 15, m == 0, m == 15);
                           }
                        }
                     }
                  }
               });
               if (is[0] != 0) {
                  typed2 = typed2.update(DSL.remainderFinder(), (dynamic) -> {
                     Dynamic dynamic2 = (Dynamic)DataFixUtils.orElse(dynamic.get("UpgradeData").result(), dynamic.emptyMap());
                     return dynamic.set("UpgradeData", dynamic2.set("Sides", dynamic.createByte((byte)(dynamic2.get("Sides").asByte((byte)0) | is[0]))));
                  });
               }

               return typed2;
            });
         });
      }
   }

   public static int method_5051(int i, int j, int k) {
      return j << 8 | k << 4 | i;
   }

   private int method_5052(int i) {
      return i & 15;
   }

   private int method_5062(int i) {
      return i >> 8 & 255;
   }

   private int method_5050(int i) {
      return i >> 4 & 15;
   }

   public static int method_5061(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
      int i = 0;
      if (bl3) {
         if (bl2) {
            i |= 2;
         } else if (bl) {
            i |= 128;
         } else {
            i |= 1;
         }
      } else if (bl4) {
         if (bl) {
            i |= 32;
         } else if (bl2) {
            i |= 8;
         } else {
            i |= 16;
         }
      } else if (bl2) {
         i |= 4;
      } else if (bl) {
         i |= 64;
      }

      return i;
   }

   public static final class LeavesLogFixer extends ListFixer {
      private static final String PERSISTENT = "persistent";
      private static final String DECAYABLE = "decayable";
      private static final String DISTANCE = "distance";
      @Nullable
      private IntSet leafIndices;
      @Nullable
      private IntSet logIndices;
      @Nullable
      private Int2IntMap leafStates;

      public LeavesLogFixer(Typed typed, Schema schema) {
         super(typed, schema);
      }

      protected boolean needsFix() {
         this.leafIndices = new IntOpenHashSet();
         this.logIndices = new IntOpenHashSet();
         this.leafStates = new Int2IntOpenHashMap();

         for(int i = 0; i < this.properties.size(); ++i) {
            Dynamic dynamic = (Dynamic)this.properties.get(i);
            String string = dynamic.get("Name").asString("");
            if (LeavesFix.LEAVES_MAP.containsKey(string)) {
               boolean bl = Objects.equals(dynamic.get("Properties").get("decayable").asString(""), "false");
               this.leafIndices.add(i);
               this.leafStates.put(this.computeFlags(string, bl, 7), i);
               this.properties.set(i, this.createLeafProperties(dynamic, string, bl, 7));
            }

            if (LeavesFix.LOGS_MAP.contains(string)) {
               this.logIndices.add(i);
            }
         }

         return this.leafIndices.isEmpty() && this.logIndices.isEmpty();
      }

      private Dynamic createLeafProperties(Dynamic dynamic, String string, boolean bl, int i) {
         Dynamic dynamic2 = dynamic.emptyMap();
         dynamic2 = dynamic2.set("persistent", dynamic2.createString(bl ? "true" : "false"));
         dynamic2 = dynamic2.set("distance", dynamic2.createString(Integer.toString(i)));
         Dynamic dynamic3 = dynamic.emptyMap();
         dynamic3 = dynamic3.set("Properties", dynamic2);
         dynamic3 = dynamic3.set("Name", dynamic3.createString(string));
         return dynamic3;
      }

      public boolean isLog(int i) {
         return this.logIndices.contains(i);
      }

      public boolean isLeaf(int i) {
         return this.leafIndices.contains(i);
      }

      int getDistanceToLog(int i) {
         return this.isLog(i) ? 0 : Integer.parseInt(((Dynamic)this.properties.get(i)).get("Properties").get("distance").asString(""));
      }

      void computeLeafStates(int i, int j, int k) {
         Dynamic dynamic = (Dynamic)this.properties.get(j);
         String string = dynamic.get("Name").asString("");
         boolean bl = Objects.equals(dynamic.get("Properties").get("persistent").asString(""), "true");
         int l = this.computeFlags(string, bl, k);
         int m;
         if (!this.leafStates.containsKey(l)) {
            m = this.properties.size();
            this.leafIndices.add(m);
            this.leafStates.put(l, m);
            this.properties.add(this.createLeafProperties(dynamic, string, bl, k));
         }

         m = this.leafStates.get(l);
         if (1 << this.blockStateMap.getUnitSize() <= m) {
            WordPackedArray lv = new WordPackedArray(this.blockStateMap.getUnitSize() + 1, 4096);

            for(int n = 0; n < 4096; ++n) {
               lv.set(n, this.blockStateMap.get(n));
            }

            this.blockStateMap = lv;
         }

         this.blockStateMap.set(i, m);
      }
   }

   public abstract static class ListFixer {
      protected static final String BLOCK_STATES_KEY = "BlockStates";
      protected static final String NAME_KEY = "Name";
      protected static final String PROPERTIES_KEY = "Properties";
      private final Type field_5695;
      protected final OpticFinder field_5693;
      protected final java.util.List properties;
      protected final int y;
      @Nullable
      protected WordPackedArray blockStateMap;

      public ListFixer(Typed typed, Schema schema) {
         this.field_5695 = DSL.named(TypeReferences.BLOCK_STATE.typeName(), DSL.remainderType());
         this.field_5693 = DSL.fieldFinder("Palette", DSL.list(this.field_5695));
         if (!Objects.equals(schema.getType(TypeReferences.BLOCK_STATE), this.field_5695)) {
            throw new IllegalStateException("Block state type is not what was expected.");
         } else {
            Optional optional = typed.getOptional(this.field_5693);
            this.properties = (java.util.List)optional.map((list) -> {
               return (java.util.List)list.stream().map(Pair::getSecond).collect(Collectors.toList());
            }).orElse(ImmutableList.of());
            Dynamic dynamic = (Dynamic)typed.get(DSL.remainderFinder());
            this.y = dynamic.get("Y").asInt(0);
            this.computeFixableBlockStates(dynamic);
         }
      }

      protected void computeFixableBlockStates(Dynamic dynamic) {
         if (this.needsFix()) {
            this.blockStateMap = null;
         } else {
            long[] ls = dynamic.get("BlockStates").asLongStream().toArray();
            int i = Math.max(4, DataFixUtils.ceillog2(this.properties.size()));
            this.blockStateMap = new WordPackedArray(i, 4096, ls);
         }

      }

      public Typed method_5083(Typed typed) {
         return this.isFixed() ? typed : typed.update(DSL.remainderFinder(), (dynamic) -> {
            return dynamic.set("BlockStates", dynamic.createLongList(Arrays.stream(this.blockStateMap.getAlignedArray())));
         }).set(this.field_5693, (java.util.List)this.properties.stream().map((dynamic) -> {
            return Pair.of(TypeReferences.BLOCK_STATE.typeName(), dynamic);
         }).collect(Collectors.toList()));
      }

      public boolean isFixed() {
         return this.blockStateMap == null;
      }

      public int needsFix(int i) {
         return this.blockStateMap.get(i);
      }

      protected int computeFlags(String leafBlockName, boolean persistent, int i) {
         return LeavesFix.LEAVES_MAP.get(leafBlockName) << 5 | (persistent ? 16 : 0) | i;
      }

      int getY() {
         return this.y;
      }

      protected abstract boolean needsFix();
   }
}
