package net.minecraft.server.chase;

import com.google.common.base.Charsets;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ChaseCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChaseClient {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int CONNECTION_RETRY_INTERVAL = 5;
   private final String ip;
   private final int port;
   private final MinecraftServer minecraftServer;
   private volatile boolean running;
   @Nullable
   private Socket socket;
   @Nullable
   private Thread thread;

   public ChaseClient(String ip, int port, MinecraftServer minecraftServer) {
      this.ip = ip;
      this.port = port;
      this.minecraftServer = minecraftServer;
   }

   public void start() {
      if (this.thread != null && this.thread.isAlive()) {
         LOGGER.warn("Remote control client was asked to start, but it is already running. Will ignore.");
      }

      this.running = true;
      this.thread = new Thread(this::run, "chase-client");
      this.thread.setDaemon(true);
      this.thread.start();
   }

   public void stop() {
      this.running = false;
      IOUtils.closeQuietly(this.socket);
      this.socket = null;
      this.thread = null;
   }

   public void run() {
      String string = this.ip + ":" + this.port;

      while(this.running) {
         try {
            LOGGER.info("Connecting to remote control server {}", string);
            this.socket = new Socket(this.ip, this.port);
            LOGGER.info("Connected to remote control server! Will continuously execute the command broadcasted by that server.");

            try {
               BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), Charsets.US_ASCII));

               try {
                  while(this.running) {
                     String string2 = bufferedReader.readLine();
                     if (string2 == null) {
                        LOGGER.warn("Lost connection to remote control server {}. Will retry in {}s.", string, 5);
                        break;
                     }

                     this.parseMessage(string2);
                  }
               } catch (Throwable var7) {
                  try {
                     bufferedReader.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }

                  throw var7;
               }

               bufferedReader.close();
            } catch (IOException var8) {
               LOGGER.warn("Lost connection to remote control server {}. Will retry in {}s.", string, 5);
            }
         } catch (IOException var9) {
            LOGGER.warn("Failed to connect to remote control server {}. Will retry in {}s.", string, 5);
         }

         if (this.running) {
            try {
               Thread.sleep(5000L);
            } catch (InterruptedException var5) {
            }
         }
      }

   }

   private void parseMessage(String message) {
      try {
         Scanner scanner = new Scanner(new StringReader(message));

         try {
            scanner.useLocale(Locale.ROOT);
            String string2 = scanner.next();
            if ("t".equals(string2)) {
               this.executeTeleportCommand(scanner);
            } else {
               LOGGER.warn("Unknown message type '{}'", string2);
            }
         } catch (Throwable var6) {
            try {
               scanner.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         scanner.close();
      } catch (NoSuchElementException var7) {
         LOGGER.warn("Could not parse message '{}', ignoring", message);
      }

   }

   private void executeTeleportCommand(Scanner scanner) {
      this.getTeleportPos(scanner).ifPresent((pos) -> {
         this.executeCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.3f %.3f %.3f %.3f %.3f", pos.dimension.getValue(), pos.pos.x, pos.pos.y, pos.pos.z, pos.rot.y, pos.rot.x));
      });
   }

   private Optional getTeleportPos(Scanner scanner) {
      RegistryKey lv = (RegistryKey)ChaseCommand.DIMENSIONS.get(scanner.next());
      if (lv == null) {
         return Optional.empty();
      } else {
         float f = scanner.nextFloat();
         float g = scanner.nextFloat();
         float h = scanner.nextFloat();
         float i = scanner.nextFloat();
         float j = scanner.nextFloat();
         return Optional.of(new TeleportPos(lv, new Vec3d((double)f, (double)g, (double)h), new Vec2f(j, i)));
      }
   }

   private void executeCommand(String command) {
      this.minecraftServer.execute(() -> {
         List list = this.minecraftServer.getPlayerManager().getPlayerList();
         if (!list.isEmpty()) {
            ServerPlayerEntity lv = (ServerPlayerEntity)list.get(0);
            ServerWorld lv2 = this.minecraftServer.getOverworld();
            ServerCommandSource lv3 = new ServerCommandSource(lv, Vec3d.of(lv2.getSpawnPos()), Vec2f.ZERO, lv2, 4, "", ScreenTexts.EMPTY, this.minecraftServer, lv);
            CommandManager lv4 = this.minecraftServer.getCommandManager();
            lv4.executeWithPrefix(lv3, command);
         }
      });
   }

   static record TeleportPos(RegistryKey dimension, Vec3d pos, Vec2f rot) {
      final RegistryKey dimension;
      final Vec3d pos;
      final Vec2f rot;

      TeleportPos(RegistryKey dimension, Vec3d pos, Vec2f rot) {
         this.dimension = dimension;
         this.pos = pos;
         this.rot = rot;
      }

      public RegistryKey dimension() {
         return this.dimension;
      }

      public Vec3d pos() {
         return this.pos;
      }

      public Vec2f rot() {
         return this.rot;
      }
   }
}
