package net.minecraft.server.command;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.io.IOException;
import net.minecraft.server.chase.ChaseClient;
import net.minecraft.server.chase.ChaseServer;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ChaseCommand {
   private static final String LOCALHOST = "localhost";
   private static final String BIND_ALL = "0.0.0.0";
   private static final int DEFAULT_PORT = 10000;
   private static final int INTERVAL = 100;
   public static BiMap DIMENSIONS;
   @Nullable
   private static ChaseServer server;
   @Nullable
   private static ChaseClient client;

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("chase").then(((LiteralArgumentBuilder)CommandManager.literal("follow").then(((RequiredArgumentBuilder)CommandManager.argument("host", StringArgumentType.string()).executes((context) -> {
         return startClient((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "host"), 10000);
      })).then(CommandManager.argument("port", IntegerArgumentType.integer(1, 65535)).executes((context) -> {
         return startClient((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "host"), IntegerArgumentType.getInteger(context, "port"));
      })))).executes((context) -> {
         return startClient((ServerCommandSource)context.getSource(), "localhost", 10000);
      }))).then(((LiteralArgumentBuilder)CommandManager.literal("lead").then(((RequiredArgumentBuilder)CommandManager.argument("bind_address", StringArgumentType.string()).executes((context) -> {
         return startServer((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "bind_address"), 10000);
      })).then(CommandManager.argument("port", IntegerArgumentType.integer(1024, 65535)).executes((context) -> {
         return startServer((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "bind_address"), IntegerArgumentType.getInteger(context, "port"));
      })))).executes((context) -> {
         return startServer((ServerCommandSource)context.getSource(), "0.0.0.0", 10000);
      }))).then(CommandManager.literal("stop").executes((context) -> {
         return stop((ServerCommandSource)context.getSource());
      })));
   }

   private static int stop(ServerCommandSource source) {
      if (client != null) {
         client.stop();
         source.sendFeedback(Text.literal("You have now stopped chasing"), false);
         client = null;
      }

      if (server != null) {
         server.stop();
         source.sendFeedback(Text.literal("You are no longer being chased"), false);
         server = null;
      }

      return 0;
   }

   private static boolean isRunning(ServerCommandSource source) {
      if (server != null) {
         source.sendError(Text.literal("Chase server is already running. Stop it using /chase stop"));
         return true;
      } else if (client != null) {
         source.sendError(Text.literal("You are already chasing someone. Stop it using /chase stop"));
         return true;
      } else {
         return false;
      }
   }

   private static int startServer(ServerCommandSource source, String ip, int port) {
      if (isRunning(source)) {
         return 0;
      } else {
         server = new ChaseServer(ip, port, source.getServer().getPlayerManager(), 100);

         try {
            server.start();
            source.sendFeedback(Text.literal("Chase server is now running on port " + port + ". Clients can follow you using /chase follow <ip> <port>"), false);
         } catch (IOException var4) {
            var4.printStackTrace();
            source.sendError(Text.literal("Failed to start chase server on port " + port));
            server = null;
         }

         return 0;
      }
   }

   private static int startClient(ServerCommandSource source, String ip, int port) {
      if (isRunning(source)) {
         return 0;
      } else {
         client = new ChaseClient(ip, port, source.getServer());
         client.start();
         source.sendFeedback(Text.literal("You are now chasing " + ip + ":" + port + ". If that server does '/chase lead' then you will automatically go to the same position. Use '/chase stop' to stop chasing."), false);
         return 0;
      }
   }

   static {
      DIMENSIONS = ImmutableBiMap.of("o", World.OVERWORLD, "n", World.NETHER, "e", World.END);
   }
}
