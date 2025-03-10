package net.minecraft.client.resource;

import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ZipResourcePack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ServerResourcePackProvider implements ResourcePackProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Pattern SHA1_PATTERN = Pattern.compile("^[a-fA-F0-9]{40}$");
   private static final int MAX_FILE_SIZE = 262144000;
   private static final int MAX_SAVED_PACKS = 10;
   private static final String SERVER = "server";
   private static final Text SERVER_NAME_TEXT = Text.translatable("resourcePack.server.name");
   private static final Text APPLYING_PACK_TEXT = Text.translatable("multiplayer.applyingPack");
   private final File serverPacksRoot;
   private final ReentrantLock lock = new ReentrantLock();
   @Nullable
   private CompletableFuture downloadTask;
   @Nullable
   private ResourcePackProfile serverContainer;

   public ServerResourcePackProvider(File serverPacksRoot) {
      this.serverPacksRoot = serverPacksRoot;
   }

   public void register(Consumer profileAdder) {
      if (this.serverContainer != null) {
         profileAdder.accept(this.serverContainer);
      }

   }

   private static Map getDownloadHeaders() {
      return Map.of("X-Minecraft-Username", MinecraftClient.getInstance().getSession().getUsername(), "X-Minecraft-UUID", MinecraftClient.getInstance().getSession().getUuid(), "X-Minecraft-Version", SharedConstants.getGameVersion().getName(), "X-Minecraft-Version-ID", SharedConstants.getGameVersion().getId(), "X-Minecraft-Pack-Format", String.valueOf(SharedConstants.getGameVersion().getResourceVersion(ResourceType.CLIENT_RESOURCES)), "User-Agent", "Minecraft Java/" + SharedConstants.getGameVersion().getName());
   }

   public CompletableFuture download(URL url, String packSha1, boolean closeAfterDownload) {
      String string2 = Hashing.sha1().hashString(url.toString(), StandardCharsets.UTF_8).toString();
      String string3 = SHA1_PATTERN.matcher(packSha1).matches() ? packSha1 : "";
      this.lock.lock();

      CompletableFuture var14;
      try {
         MinecraftClient lv = MinecraftClient.getInstance();
         File file = new File(this.serverPacksRoot, string2);
         CompletableFuture completableFuture;
         if (file.exists()) {
            completableFuture = CompletableFuture.completedFuture("");
         } else {
            ProgressScreen lv2 = new ProgressScreen(closeAfterDownload);
            Map map = getDownloadHeaders();
            lv.submitAndJoin(() -> {
               lv.setScreen(lv2);
            });
            completableFuture = NetworkUtils.downloadResourcePack(file, url, map, 262144000, lv2, lv.getNetworkProxy());
         }

         this.downloadTask = completableFuture.thenCompose((object) -> {
            if (!this.verifyFile(string3, file)) {
               return CompletableFuture.failedFuture(new RuntimeException("Hash check failure for file " + file + ", see log"));
            } else {
               lv.execute(() -> {
                  if (!closeAfterDownload) {
                     lv.setScreen(new MessageScreen(APPLYING_PACK_TEXT));
                  }

               });
               return this.loadServerPack(file, ResourcePackSource.SERVER);
            }
         }).exceptionallyCompose((throwable) -> {
            return this.clear().thenAcceptAsync((void_) -> {
               LOGGER.warn("Pack application failed: {}, deleting file {}", throwable.getMessage(), file);
               delete(file);
            }, Util.getIoWorkerExecutor()).thenAcceptAsync((void_) -> {
               lv.setScreen(new ConfirmScreen((confirmed) -> {
                  if (confirmed) {
                     lv.setScreen((Screen)null);
                  } else {
                     ClientPlayNetworkHandler lvx = lv.getNetworkHandler();
                     if (lvx != null) {
                        lvx.getConnection().disconnect(Text.translatable("connect.aborted"));
                     }
                  }

               }, Text.translatable("multiplayer.texturePrompt.failure.line1"), Text.translatable("multiplayer.texturePrompt.failure.line2"), ScreenTexts.PROCEED, Text.translatable("menu.disconnect")));
            }, lv);
         }).thenAcceptAsync((void_) -> {
            this.deleteOldServerPack();
         }, Util.getIoWorkerExecutor());
         var14 = this.downloadTask;
      } finally {
         this.lock.unlock();
      }

      return var14;
   }

   private static void delete(File file) {
      try {
         Files.delete(file.toPath());
      } catch (IOException var2) {
         LOGGER.warn("Failed to delete file {}: {}", file, var2.getMessage());
      }

   }

   public CompletableFuture clear() {
      this.lock.lock();

      CompletableFuture var1;
      try {
         if (this.downloadTask != null) {
            this.downloadTask.cancel(true);
         }

         this.downloadTask = null;
         if (this.serverContainer == null) {
            return CompletableFuture.completedFuture((Object)null);
         }

         this.serverContainer = null;
         var1 = MinecraftClient.getInstance().reloadResourcesConcurrently();
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   private boolean verifyFile(String expectedSha1, File file) {
      try {
         String string2 = com.google.common.io.Files.asByteSource(file).hash(Hashing.sha1()).toString();
         if (expectedSha1.isEmpty()) {
            LOGGER.info("Found file {} without verification hash", file);
            return true;
         }

         if (string2.toLowerCase(Locale.ROOT).equals(expectedSha1.toLowerCase(Locale.ROOT))) {
            LOGGER.info("Found file {} matching requested hash {}", file, expectedSha1);
            return true;
         }

         LOGGER.warn("File {} had wrong hash (expected {}, found {}).", new Object[]{file, expectedSha1, string2});
      } catch (IOException var4) {
         LOGGER.warn("File {} couldn't be hashed.", file, var4);
      }

      return false;
   }

   private void deleteOldServerPack() {
      if (this.serverPacksRoot.isDirectory()) {
         try {
            List list = new ArrayList(FileUtils.listFiles(this.serverPacksRoot, TrueFileFilter.TRUE, (IOFileFilter)null));
            list.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            int i = 0;
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
               File file = (File)var3.next();
               if (i++ >= 10) {
                  LOGGER.info("Deleting old server resource pack {}", file.getName());
                  FileUtils.deleteQuietly(file);
               }
            }
         } catch (Exception var5) {
            LOGGER.error("Error while deleting old server resource pack : {}", var5.getMessage());
         }

      }
   }

   public CompletableFuture loadServerPack(File packZip, ResourcePackSource packSource) {
      ResourcePackProfile.PackFactory lv = (name) -> {
         return new ZipResourcePack(name, packZip, false);
      };
      ResourcePackProfile.Metadata lv2 = ResourcePackProfile.loadMetadata("server", lv);
      if (lv2 == null) {
         return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid pack metadata at " + packZip));
      } else {
         LOGGER.info("Applying server pack {}", packZip);
         this.serverContainer = ResourcePackProfile.of("server", SERVER_NAME_TEXT, true, lv, lv2, ResourceType.CLIENT_RESOURCES, ResourcePackProfile.InsertionPosition.TOP, true, packSource);
         return MinecraftClient.getInstance().reloadResourcesConcurrently();
      }
   }

   public CompletableFuture loadServerPack(LevelStorage.Session session) {
      Path path = session.getDirectory(WorldSavePath.RESOURCES_ZIP);
      return Files.exists(path, new LinkOption[0]) && !Files.isDirectory(path, new LinkOption[0]) ? this.loadServerPack(path.toFile(), ResourcePackSource.WORLD) : CompletableFuture.completedFuture((Object)null);
   }
}
