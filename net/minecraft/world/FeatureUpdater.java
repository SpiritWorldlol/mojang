package net.minecraft.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class FeatureUpdater {
   private static final Map OLD_TO_NEW = (Map)Util.make(Maps.newHashMap(), (map) -> {
      map.put("Village", "Village");
      map.put("Mineshaft", "Mineshaft");
      map.put("Mansion", "Mansion");
      map.put("Igloo", "Temple");
      map.put("Desert_Pyramid", "Temple");
      map.put("Jungle_Pyramid", "Temple");
      map.put("Swamp_Hut", "Temple");
      map.put("Stronghold", "Stronghold");
      map.put("Monument", "Monument");
      map.put("Fortress", "Fortress");
      map.put("EndCity", "EndCity");
   });
   private static final Map ANCIENT_TO_OLD = (Map)Util.make(Maps.newHashMap(), (map) -> {
      map.put("Iglu", "Igloo");
      map.put("TeDP", "Desert_Pyramid");
      map.put("TeJP", "Jungle_Pyramid");
      map.put("TeSH", "Swamp_Hut");
   });
   private static final Set field_37194 = Set.of("pillager_outpost", "mineshaft", "mansion", "jungle_pyramid", "desert_pyramid", "igloo", "ruined_portal", "shipwreck", "swamp_hut", "stronghold", "monument", "ocean_ruin", "fortress", "endcity", "buried_treasure", "village", "nether_fossil", "bastion_remnant");
   private final boolean needsUpdate;
   private final Map featureIdToChunkNbt = Maps.newHashMap();
   private final Map updateStates = Maps.newHashMap();
   private final List field_17658;
   private final List field_17659;

   public FeatureUpdater(@Nullable PersistentStateManager persistentStateManager, List list, List list2) {
      this.field_17658 = list;
      this.field_17659 = list2;
      this.init(persistentStateManager);
      boolean bl = false;

      String string;
      for(Iterator var5 = this.field_17659.iterator(); var5.hasNext(); bl |= this.featureIdToChunkNbt.get(string) != null) {
         string = (String)var5.next();
      }

      this.needsUpdate = bl;
   }

   public void markResolved(long l) {
      Iterator var3 = this.field_17658.iterator();

      while(var3.hasNext()) {
         String string = (String)var3.next();
         ChunkUpdateState lv = (ChunkUpdateState)this.updateStates.get(string);
         if (lv != null && lv.isRemaining(l)) {
            lv.markResolved(l);
            lv.markDirty();
         }
      }

   }

   public NbtCompound getUpdatedReferences(NbtCompound nbt) {
      NbtCompound lv = nbt.getCompound("Level");
      ChunkPos lv2 = new ChunkPos(lv.getInt("xPos"), lv.getInt("zPos"));
      if (this.needsUpdate(lv2.x, lv2.z)) {
         nbt = this.getUpdatedStarts(nbt, lv2);
      }

      NbtCompound lv3 = lv.getCompound("Structures");
      NbtCompound lv4 = lv3.getCompound("References");
      Iterator var6 = this.field_17659.iterator();

      while(true) {
         String string;
         boolean bl;
         do {
            do {
               if (!var6.hasNext()) {
                  lv3.put("References", lv4);
                  lv.put("Structures", lv3);
                  nbt.put("Level", lv);
                  return nbt;
               }

               string = (String)var6.next();
               bl = field_37194.contains(string.toLowerCase(Locale.ROOT));
            } while(lv4.contains(string, NbtElement.LONG_ARRAY_TYPE));
         } while(!bl);

         int i = true;
         LongList longList = new LongArrayList();

         for(int j = lv2.x - 8; j <= lv2.x + 8; ++j) {
            for(int k = lv2.z - 8; k <= lv2.z + 8; ++k) {
               if (this.needsUpdate(j, k, string)) {
                  longList.add(ChunkPos.toLong(j, k));
               }
            }
         }

         lv4.putLongArray(string, (List)longList);
      }
   }

   private boolean needsUpdate(int chunkX, int chunkZ, String id) {
      if (!this.needsUpdate) {
         return false;
      } else {
         return this.featureIdToChunkNbt.get(id) != null && ((ChunkUpdateState)this.updateStates.get(OLD_TO_NEW.get(id))).contains(ChunkPos.toLong(chunkX, chunkZ));
      }
   }

   private boolean needsUpdate(int chunkX, int chunkZ) {
      if (!this.needsUpdate) {
         return false;
      } else {
         Iterator var3 = this.field_17659.iterator();

         String string;
         do {
            if (!var3.hasNext()) {
               return false;
            }

            string = (String)var3.next();
         } while(this.featureIdToChunkNbt.get(string) == null || !((ChunkUpdateState)this.updateStates.get(OLD_TO_NEW.get(string))).isRemaining(ChunkPos.toLong(chunkX, chunkZ)));

         return true;
      }
   }

   private NbtCompound getUpdatedStarts(NbtCompound nbt, ChunkPos pos) {
      NbtCompound lv = nbt.getCompound("Level");
      NbtCompound lv2 = lv.getCompound("Structures");
      NbtCompound lv3 = lv2.getCompound("Starts");
      Iterator var6 = this.field_17659.iterator();

      while(var6.hasNext()) {
         String string = (String)var6.next();
         Long2ObjectMap long2ObjectMap = (Long2ObjectMap)this.featureIdToChunkNbt.get(string);
         if (long2ObjectMap != null) {
            long l = pos.toLong();
            if (((ChunkUpdateState)this.updateStates.get(OLD_TO_NEW.get(string))).isRemaining(l)) {
               NbtCompound lv4 = (NbtCompound)long2ObjectMap.get(l);
               if (lv4 != null) {
                  lv3.put(string, lv4);
               }
            }
         }
      }

      lv2.put("Starts", lv3);
      lv.put("Structures", lv2);
      nbt.put("Level", lv);
      return nbt;
   }

   private void init(@Nullable PersistentStateManager persistentStateManager) {
      if (persistentStateManager != null) {
         Iterator var2 = this.field_17658.iterator();

         while(var2.hasNext()) {
            String string = (String)var2.next();
            NbtCompound lv = new NbtCompound();

            try {
               lv = persistentStateManager.readNbt(string, 1493).getCompound("data").getCompound("Features");
               if (lv.isEmpty()) {
                  continue;
               }
            } catch (IOException var13) {
            }

            Iterator var5 = lv.getKeys().iterator();

            while(var5.hasNext()) {
               String string2 = (String)var5.next();
               NbtCompound lv2 = lv.getCompound(string2);
               long l = ChunkPos.toLong(lv2.getInt("ChunkX"), lv2.getInt("ChunkZ"));
               NbtList lv3 = lv2.getList("Children", NbtElement.COMPOUND_TYPE);
               String string3;
               if (!lv3.isEmpty()) {
                  string3 = lv3.getCompound(0).getString("id");
                  String string4 = (String)ANCIENT_TO_OLD.get(string3);
                  if (string4 != null) {
                     lv2.putString("id", string4);
                  }
               }

               string3 = lv2.getString("id");
               ((Long2ObjectMap)this.featureIdToChunkNbt.computeIfAbsent(string3, (stringx) -> {
                  return new Long2ObjectOpenHashMap();
               })).put(l, lv2);
            }

            String string5 = string + "_index";
            ChunkUpdateState lv4 = (ChunkUpdateState)persistentStateManager.getOrCreate(ChunkUpdateState::fromNbt, ChunkUpdateState::new, string5);
            if (!lv4.getAll().isEmpty()) {
               this.updateStates.put(string, lv4);
            } else {
               ChunkUpdateState lv5 = new ChunkUpdateState();
               this.updateStates.put(string, lv5);
               Iterator var17 = lv.getKeys().iterator();

               while(var17.hasNext()) {
                  String string6 = (String)var17.next();
                  NbtCompound lv6 = lv.getCompound(string6);
                  lv5.add(ChunkPos.toLong(lv6.getInt("ChunkX"), lv6.getInt("ChunkZ")));
               }

               lv5.markDirty();
            }
         }

      }
   }

   public static FeatureUpdater create(RegistryKey world, @Nullable PersistentStateManager persistentStateManager) {
      if (world == World.OVERWORLD) {
         return new FeatureUpdater(persistentStateManager, ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"), ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument"));
      } else {
         ImmutableList list;
         if (world == World.NETHER) {
            list = ImmutableList.of("Fortress");
            return new FeatureUpdater(persistentStateManager, list, list);
         } else if (world == World.END) {
            list = ImmutableList.of("EndCity");
            return new FeatureUpdater(persistentStateManager, list, list);
         } else {
            throw new RuntimeException(String.format(Locale.ROOT, "Unknown dimension type : %s", world));
         }
      }
   }
}
