package net.minecraft.command.argument;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.test.TestFunctions;
import net.minecraft.text.Text;

public class TestClassArgumentType implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("techtests", "mobtests");

   public String parse(StringReader stringReader) throws CommandSyntaxException {
      String string = stringReader.readUnquotedString();
      if (TestFunctions.testClassExists(string)) {
         return string;
      } else {
         Message message = Text.literal("No such test class: " + string);
         throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
      }
   }

   public static TestClassArgumentType testClass() {
      return new TestClassArgumentType();
   }

   public static String getTestClass(CommandContext context, String name) {
      return (String)context.getArgument(name, String.class);
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(TestFunctions.getTestClasses().stream(), builder);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }

   // $FF: synthetic method
   public Object parse(StringReader reader) throws CommandSyntaxException {
      return this.parse(reader);
   }
}
