package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public class SpectateCommand {
   private static final SimpleCommandExceptionType SPECTATE_SELF_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.spectate.self"));
   private static final DynamicCommandExceptionType NOT_SPECTATOR_EXCEPTION = new DynamicCommandExceptionType((playerName) -> {
      return Text.translatable("commands.spectate.not_spectator", playerName);
   });

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spectate").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), (Entity)null, ((ServerCommandSource)context.getSource()).getPlayerOrThrow());
      })).then(((RequiredArgumentBuilder)CommandManager.argument("target", EntityArgumentType.entity()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), ((ServerCommandSource)context.getSource()).getPlayerOrThrow());
      })).then(CommandManager.argument("player", EntityArgumentType.player()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), EntityArgumentType.getPlayer(context, "player"));
      }))));
   }

   private static int execute(ServerCommandSource source, @Nullable Entity entity, ServerPlayerEntity player) throws CommandSyntaxException {
      if (player == entity) {
         throw SPECTATE_SELF_EXCEPTION.create();
      } else if (player.interactionManager.getGameMode() != GameMode.SPECTATOR) {
         throw NOT_SPECTATOR_EXCEPTION.create(player.getDisplayName());
      } else {
         player.setCameraEntity(entity);
         if (entity != null) {
            source.sendFeedback(Text.translatable("commands.spectate.success.started", entity.getDisplayName()), false);
         } else {
            source.sendFeedback(Text.translatable("commands.spectate.success.stopped"), false);
         }

         return 1;
      }
   }
}
