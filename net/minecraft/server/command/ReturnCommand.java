package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class ReturnCommand {
   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("return").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("value", IntegerArgumentType.integer()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "value"));
      })));
   }

   private static int execute(ServerCommandSource source, int value) {
      source.getReturnValueConsumer().accept(value);
      return value;
   }
}
