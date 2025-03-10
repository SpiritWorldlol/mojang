package net.minecraft.client.realms.task;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.URL;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerAddress;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.exception.RetryCallException;
import net.minecraft.client.realms.gui.screen.RealmsBrokenWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongConfirmationScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsTermsScreen;
import net.minecraft.text.Text;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsGetServerDetailsTask extends LongRunningTask {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final RealmsServer server;
   private final Screen lastScreen;
   private final RealmsMainScreen mainScreen;
   private final ReentrantLock connectLock;

   public RealmsGetServerDetailsTask(RealmsMainScreen mainScreen, Screen lastScreen, RealmsServer server, ReentrantLock connectLock) {
      this.lastScreen = lastScreen;
      this.mainScreen = mainScreen;
      this.server = server;
      this.connectLock = connectLock;
   }

   public void run() {
      this.setTitle(Text.translatable("mco.connect.connecting"));

      RealmsServerAddress lv;
      try {
         lv = this.join();
      } catch (CancellationException var4) {
         LOGGER.info("User aborted connecting to realms");
         return;
      } catch (RealmsServiceException var5) {
         switch (var5.getErrorCode(-1)) {
            case 6002:
               setScreen(new RealmsTermsScreen(this.lastScreen, this.mainScreen, this.server));
               return;
            case 6006:
               boolean bl = this.server.ownerUUID.equals(MinecraftClient.getInstance().getSession().getUuid());
               setScreen((Screen)(bl ? new RealmsBrokenWorldScreen(this.lastScreen, this.mainScreen, this.server.id, this.server.worldType == RealmsServer.WorldType.MINIGAME) : new RealmsGenericErrorScreen(Text.translatable("mco.brokenworld.nonowner.title"), Text.translatable("mco.brokenworld.nonowner.error"), this.lastScreen)));
               return;
            default:
               this.error(var5.toString());
               LOGGER.error("Couldn't connect to world", var5);
               return;
         }
      } catch (TimeoutException var6) {
         this.error(Text.translatable("mco.errorMessage.connectionFailure"));
         return;
      } catch (Exception var7) {
         LOGGER.error("Couldn't connect to world", var7);
         this.error(var7.getLocalizedMessage());
         return;
      }

      boolean bl2 = lv.resourcePackUrl != null && lv.resourcePackHash != null;
      Screen lv3 = bl2 ? this.createResourcePackConfirmationScreen(lv, this::createConnectingScreen) : this.createConnectingScreen(lv);
      setScreen((Screen)lv3);
   }

   private RealmsServerAddress join() throws RealmsServiceException, TimeoutException, CancellationException {
      RealmsClient lv = RealmsClient.create();
      int i = 0;

      while(i < 40) {
         if (this.aborted()) {
            throw new CancellationException();
         }

         try {
            return lv.join(this.server.id);
         } catch (RetryCallException var4) {
            pause((long)var4.delaySeconds);
            ++i;
         }
      }

      throw new TimeoutException();
   }

   public RealmsLongRunningMcoTaskScreen createConnectingScreen(RealmsServerAddress address) {
      return new RealmsLongRunningMcoTaskScreen(this.lastScreen, new RealmsConnectTask(this.lastScreen, this.server, address));
   }

   private RealmsLongConfirmationScreen createResourcePackConfirmationScreen(RealmsServerAddress address, Function connectingScreenCreator) {
      BooleanConsumer booleanConsumer = (confirmed) -> {
         try {
            if (!confirmed) {
               setScreen(this.lastScreen);
               return;
            }

            this.downloadResourcePack(address).thenRun(() -> {
               setScreen((Screen)connectingScreenCreator.apply(address));
            }).exceptionally((throwable) -> {
               MinecraftClient.getInstance().getServerResourcePackProvider().clear();
               LOGGER.error("Failed to download resource pack from {}", address, throwable);
               setScreen(new RealmsGenericErrorScreen(Text.literal("Failed to download resource pack!"), this.lastScreen));
               return null;
            });
         } finally {
            if (this.connectLock.isHeldByCurrentThread()) {
               this.connectLock.unlock();
            }

         }

      };
      return new RealmsLongConfirmationScreen(booleanConsumer, RealmsLongConfirmationScreen.Type.INFO, Text.translatable("mco.configure.world.resourcepack.question.line1"), Text.translatable("mco.configure.world.resourcepack.question.line2"), true);
   }

   private CompletableFuture downloadResourcePack(RealmsServerAddress address) {
      try {
         return MinecraftClient.getInstance().getServerResourcePackProvider().download(new URL(address.resourcePackUrl), address.resourcePackHash, false);
      } catch (Exception var4) {
         CompletableFuture completableFuture = new CompletableFuture();
         completableFuture.completeExceptionally(var4);
         return completableFuture;
      }
   }
}
