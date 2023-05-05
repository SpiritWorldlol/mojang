package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.OptionalInt;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableObject;

public class FunctionCommand {
   public static final SuggestionProvider SUGGESTION_PROVIDER = (context, builder) -> {
      CommandFunctionManager lv = ((ServerCommandSource)context.getSource()).getServer().getCommandFunctionManager();
      CommandSource.suggestIdentifiers(lv.getFunctionTags(), builder, "#");
      return CommandSource.suggestIdentifiers(lv.getAllFunctions(), builder);
   };

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("function").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("name", CommandFunctionArgumentType.commandFunction()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), CommandFunctionArgumentType.getFunctions(context, "name"));
      })));
   }

   private static int execute(ServerCommandSource source, Collection functions) {
      int i = 0;
      boolean bl = false;

      OptionalInt optionalInt;
      for(Iterator var4 = functions.iterator(); var4.hasNext(); bl |= optionalInt.isPresent()) {
         CommandFunction lv = (CommandFunction)var4.next();
         MutableObject mutableObject = new MutableObject(OptionalInt.empty());
         int j = source.getServer().getCommandFunctionManager().execute(lv, source.withSilent().withMaxLevel(2).withReturnValueConsumer((value) -> {
            mutableObject.setValue(OptionalInt.of(value));
         }));
         optionalInt = (OptionalInt)mutableObject.getValue();
         i += optionalInt.orElse(j);
      }

      if (functions.size() == 1) {
         if (bl) {
            source.sendFeedback(Text.translatable("commands.function.success.single.result", i, ((CommandFunction)functions.iterator().next()).getId()), true);
         } else {
            source.sendFeedback(Text.translatable("commands.function.success.single", i, ((CommandFunction)functions.iterator().next()).getId()), true);
         }
      } else if (bl) {
         source.sendFeedback(Text.translatable("commands.function.success.multiple.result", functions.size()), true);
      } else {
         source.sendFeedback(Text.translatable("commands.function.success.multiple", i, functions.size()), true);
      }

      return i;
   }
}
