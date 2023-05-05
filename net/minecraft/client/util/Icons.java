package net.minecraft.client.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import org.apache.commons.lang3.ArrayUtils;

@Environment(EnvType.CLIENT)
public enum Icons {
   RELEASE(new String[]{"icons"}),
   SNAPSHOT(new String[]{"icons", "snapshot"});

   private final String[] path;

   private Icons(String... path) {
      this.path = path;
   }

   public List getIcons(ResourcePack resourcePack) throws IOException {
      return List.of(this.getIcon(resourcePack, "icon_16x16.png"), this.getIcon(resourcePack, "icon_32x32.png"), this.getIcon(resourcePack, "icon_48x48.png"));
   }

   public InputSupplier getMacIcon(ResourcePack resourcePack) throws IOException {
      return this.getIcon(resourcePack, "minecraft.icns");
   }

   private InputSupplier getIcon(ResourcePack resourcePack, String fileName) throws IOException {
      String[] strings = (String[])ArrayUtils.add(this.path, fileName);
      InputSupplier lv = resourcePack.openRoot(strings);
      if (lv == null) {
         throw new FileNotFoundException(String.join("/", strings));
      } else {
         return lv;
      }
   }

   // $FF: synthetic method
   private static Icons[] method_51417() {
      return new Icons[]{RELEASE, SNAPSHOT};
   }
}
