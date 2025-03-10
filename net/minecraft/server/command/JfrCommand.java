package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.SharedConstants;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.util.profiling.jfr.InstanceType;

public class JfrCommand {
   private static final SimpleCommandExceptionType JFR_START_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.jfr.start.failed"));
   private static final DynamicCommandExceptionType JFR_DUMP_FAILED_EXCEPTION = new DynamicCommandExceptionType((message) -> {
      return Text.translatable("commands.jfr.dump.failed", message);
   });

   private JfrCommand() {
   }

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("jfr").requires((source) -> {
         return source.hasPermissionLevel(4);
      })).then(CommandManager.literal("start").executes((context) -> {
         return executeStart((ServerCommandSource)context.getSource());
      }))).then(CommandManager.literal("stop").executes((context) -> {
         return executeStop((ServerCommandSource)context.getSource());
      })));
   }

   private static int executeStart(ServerCommandSource source) throws CommandSyntaxException {
      InstanceType lv = InstanceType.get(source.getServer());
      if (!FlightProfiler.INSTANCE.start(lv)) {
         throw JFR_START_FAILED_EXCEPTION.create();
      } else {
         source.sendFeedback(Text.translatable("commands.jfr.started"), false);
         return 1;
      }
   }

   private static int executeStop(ServerCommandSource source) throws CommandSyntaxException {
      try {
         Path path = Paths.get(".").relativize(FlightProfiler.INSTANCE.stop().normalize());
         Path path2 = source.getServer().isRemote() && !SharedConstants.isDevelopment ? path : path.toAbsolutePath();
         Text lv = Text.literal(path.toString()).formatted(Formatting.UNDERLINE).styled((style) -> {
            return style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, path2.toString())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.copy.click")));
         });
         source.sendFeedback(Text.translatable("commands.jfr.stopped", lv), false);
         return 1;
      } catch (Throwable var4) {
         throw JFR_DUMP_FAILED_EXCEPTION.create(var4.getMessage());
      }
   }
}
