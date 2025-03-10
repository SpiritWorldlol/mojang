package net.minecraft.client.option;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class HotbarStorage {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int STORAGE_ENTRY_COUNT = 9;
   private final File file;
   private final DataFixer dataFixer;
   private final HotbarStorageEntry[] entries = new HotbarStorageEntry[9];
   private boolean loaded;

   public HotbarStorage(File file, DataFixer dataFixer) {
      this.file = new File(file, "hotbar.nbt");
      this.dataFixer = dataFixer;

      for(int i = 0; i < 9; ++i) {
         this.entries[i] = new HotbarStorageEntry();
      }

   }

   private void load() {
      try {
         NbtCompound lv = NbtIo.read(this.file);
         if (lv == null) {
            return;
         }

         int i = NbtHelper.getDataVersion(lv, 1343);
         lv = DataFixTypes.HOTBAR.update(this.dataFixer, lv, i);

         for(int j = 0; j < 9; ++j) {
            this.entries[j].readNbtList(lv.getList(String.valueOf(j), NbtElement.COMPOUND_TYPE));
         }
      } catch (Exception var4) {
         LOGGER.error("Failed to load creative mode options", var4);
      }

   }

   public void save() {
      try {
         NbtCompound lv = NbtHelper.putDataVersion(new NbtCompound());

         for(int i = 0; i < 9; ++i) {
            lv.put(String.valueOf(i), this.getSavedHotbar(i).toNbtList());
         }

         NbtIo.write(lv, this.file);
      } catch (Exception var3) {
         LOGGER.error("Failed to save creative mode options", var3);
      }

   }

   public HotbarStorageEntry getSavedHotbar(int i) {
      if (!this.loaded) {
         this.load();
         this.loaded = true;
      }

      return this.entries[i];
   }
}
