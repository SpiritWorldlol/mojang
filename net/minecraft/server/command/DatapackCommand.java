package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

public class DatapackCommand {
   private static final DynamicCommandExceptionType UNKNOWN_DATAPACK_EXCEPTION = new DynamicCommandExceptionType((name) -> {
      return Text.translatable("commands.datapack.unknown", name);
   });
   private static final DynamicCommandExceptionType ALREADY_ENABLED_EXCEPTION = new DynamicCommandExceptionType((name) -> {
      return Text.translatable("commands.datapack.enable.failed", name);
   });
   private static final DynamicCommandExceptionType ALREADY_DISABLED_EXCEPTION = new DynamicCommandExceptionType((name) -> {
      return Text.translatable("commands.datapack.disable.failed", name);
   });
   private static final Dynamic2CommandExceptionType NO_FLAGS_EXCEPTION = new Dynamic2CommandExceptionType((name, flags) -> {
      return Text.translatable("commands.datapack.enable.failed.no_flags", name, flags);
   });
   private static final SuggestionProvider ENABLED_CONTAINERS_SUGGESTION_PROVIDER = (context, builder) -> {
      return CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getServer().getDataPackManager().getEnabledNames().stream().map(StringArgumentType::escapeIfRequired), builder);
   };
   private static final SuggestionProvider DISABLED_CONTAINERS_SUGGESTION_PROVIDER = (context, builder) -> {
      ResourcePackManager lv = ((ServerCommandSource)context.getSource()).getServer().getDataPackManager();
      Collection collection = lv.getEnabledNames();
      FeatureSet lv2 = ((ServerCommandSource)context.getSource()).getEnabledFeatures();
      return CommandSource.suggestMatching(lv.getProfiles().stream().filter((profile) -> {
         return profile.getRequestedFeatures().isSubsetOf(lv2);
      }).map(ResourcePackProfile::getName).filter((name) -> {
         return !collection.contains(name);
      }).map(StringArgumentType::escapeIfRequired), builder);
   };

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("datapack").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.literal("enable").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("name", StringArgumentType.string()).suggests(DISABLED_CONTAINERS_SUGGESTION_PROVIDER).executes((context) -> {
         return executeEnable((ServerCommandSource)context.getSource(), getPackContainer(context, "name", true), (profiles, profile) -> {
            profile.getInitialPosition().insert(profiles, profile, (profilex) -> {
               return profilex;
            }, false);
         });
      })).then(CommandManager.literal("after").then(CommandManager.argument("existing", StringArgumentType.string()).suggests(ENABLED_CONTAINERS_SUGGESTION_PROVIDER).executes((context) -> {
         return executeEnable((ServerCommandSource)context.getSource(), getPackContainer(context, "name", true), (profiles, profile) -> {
            profiles.add(profiles.indexOf(getPackContainer(context, "existing", false)) + 1, profile);
         });
      })))).then(CommandManager.literal("before").then(CommandManager.argument("existing", StringArgumentType.string()).suggests(ENABLED_CONTAINERS_SUGGESTION_PROVIDER).executes((context) -> {
         return executeEnable((ServerCommandSource)context.getSource(), getPackContainer(context, "name", true), (profiles, profile) -> {
            profiles.add(profiles.indexOf(getPackContainer(context, "existing", false)), profile);
         });
      })))).then(CommandManager.literal("last").executes((context) -> {
         return executeEnable((ServerCommandSource)context.getSource(), getPackContainer(context, "name", true), List::add);
      }))).then(CommandManager.literal("first").executes((context) -> {
         return executeEnable((ServerCommandSource)context.getSource(), getPackContainer(context, "name", true), (profiles, profile) -> {
            profiles.add(0, profile);
         });
      }))))).then(CommandManager.literal("disable").then(CommandManager.argument("name", StringArgumentType.string()).suggests(ENABLED_CONTAINERS_SUGGESTION_PROVIDER).executes((context) -> {
         return executeDisable((ServerCommandSource)context.getSource(), getPackContainer(context, "name", false));
      })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("list").executes((context) -> {
         return executeList((ServerCommandSource)context.getSource());
      })).then(CommandManager.literal("available").executes((context) -> {
         return executeListAvailable((ServerCommandSource)context.getSource());
      }))).then(CommandManager.literal("enabled").executes((context) -> {
         return executeListEnabled((ServerCommandSource)context.getSource());
      }))));
   }

   private static int executeEnable(ServerCommandSource source, ResourcePackProfile container, PackAdder packAdder) throws CommandSyntaxException {
      ResourcePackManager lv = source.getServer().getDataPackManager();
      List list = Lists.newArrayList(lv.getEnabledProfiles());
      packAdder.apply(list, container);
      source.sendFeedback(Text.translatable("commands.datapack.modify.enable", container.getInformationText(true)), true);
      ReloadCommand.tryReloadDataPacks((Collection)list.stream().map(ResourcePackProfile::getName).collect(Collectors.toList()), source);
      return list.size();
   }

   private static int executeDisable(ServerCommandSource source, ResourcePackProfile container) {
      ResourcePackManager lv = source.getServer().getDataPackManager();
      List list = Lists.newArrayList(lv.getEnabledProfiles());
      list.remove(container);
      source.sendFeedback(Text.translatable("commands.datapack.modify.disable", container.getInformationText(true)), true);
      ReloadCommand.tryReloadDataPacks((Collection)list.stream().map(ResourcePackProfile::getName).collect(Collectors.toList()), source);
      return list.size();
   }

   private static int executeList(ServerCommandSource source) {
      return executeListEnabled(source) + executeListAvailable(source);
   }

   private static int executeListAvailable(ServerCommandSource source) {
      ResourcePackManager lv = source.getServer().getDataPackManager();
      lv.scanPacks();
      Collection collection = lv.getEnabledProfiles();
      Collection collection2 = lv.getProfiles();
      FeatureSet lv2 = source.getEnabledFeatures();
      List list = collection2.stream().filter((profile) -> {
         return !collection.contains(profile) && profile.getRequestedFeatures().isSubsetOf(lv2);
      }).toList();
      if (list.isEmpty()) {
         source.sendFeedback(Text.translatable("commands.datapack.list.available.none"), false);
      } else {
         source.sendFeedback(Text.translatable("commands.datapack.list.available.success", list.size(), Texts.join(list, (Function)((profile) -> {
            return profile.getInformationText(false);
         }))), false);
      }

      return list.size();
   }

   private static int executeListEnabled(ServerCommandSource source) {
      ResourcePackManager lv = source.getServer().getDataPackManager();
      lv.scanPacks();
      Collection collection = lv.getEnabledProfiles();
      if (collection.isEmpty()) {
         source.sendFeedback(Text.translatable("commands.datapack.list.enabled.none"), false);
      } else {
         source.sendFeedback(Text.translatable("commands.datapack.list.enabled.success", collection.size(), Texts.join(collection, (profile) -> {
            return profile.getInformationText(true);
         })), false);
      }

      return collection.size();
   }

   private static ResourcePackProfile getPackContainer(CommandContext context, String name, boolean enable) throws CommandSyntaxException {
      String string2 = StringArgumentType.getString(context, name);
      ResourcePackManager lv = ((ServerCommandSource)context.getSource()).getServer().getDataPackManager();
      ResourcePackProfile lv2 = lv.getProfile(string2);
      if (lv2 == null) {
         throw UNKNOWN_DATAPACK_EXCEPTION.create(string2);
      } else {
         boolean bl2 = lv.getEnabledProfiles().contains(lv2);
         if (enable && bl2) {
            throw ALREADY_ENABLED_EXCEPTION.create(string2);
         } else if (!enable && !bl2) {
            throw ALREADY_DISABLED_EXCEPTION.create(string2);
         } else {
            FeatureSet lv3 = ((ServerCommandSource)context.getSource()).getEnabledFeatures();
            FeatureSet lv4 = lv2.getRequestedFeatures();
            if (!lv4.isSubsetOf(lv3)) {
               throw NO_FLAGS_EXCEPTION.create(string2, FeatureFlags.printMissingFlags(lv3, lv4));
            } else {
               return lv2;
            }
         }
      }
   }

   private interface PackAdder {
      void apply(List profiles, ResourcePackProfile profile) throws CommandSyntaxException;
   }
}
