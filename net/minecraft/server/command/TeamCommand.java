package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

public class TeamCommand {
   private static final SimpleCommandExceptionType ADD_DUPLICATE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.team.add.duplicate"));
   private static final SimpleCommandExceptionType EMPTY_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.team.empty.unchanged"));
   private static final SimpleCommandExceptionType OPTION_NAME_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.team.option.name.unchanged"));
   private static final SimpleCommandExceptionType OPTION_COLOR_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.team.option.color.unchanged"));
   private static final SimpleCommandExceptionType OPTION_FRIENDLY_FIRE_ALREADY_ENABLED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.team.option.friendlyfire.alreadyEnabled"));
   private static final SimpleCommandExceptionType OPTION_FRIENDLY_FIRE_ALREADY_DISABLED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.team.option.friendlyfire.alreadyDisabled"));
   private static final SimpleCommandExceptionType OPTION_SEE_FRIENDLY_INVISIBLES_ALREADY_ENABLED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.team.option.seeFriendlyInvisibles.alreadyEnabled"));
   private static final SimpleCommandExceptionType OPTION_SEE_FRIENDLY_INVISIBLES_ALREADY_DISABLED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.team.option.seeFriendlyInvisibles.alreadyDisabled"));
   private static final SimpleCommandExceptionType OPTION_NAMETAG_VISIBILITY_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.team.option.nametagVisibility.unchanged"));
   private static final SimpleCommandExceptionType OPTION_DEATH_MESSAGE_VISIBILITY_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.team.option.deathMessageVisibility.unchanged"));
   private static final SimpleCommandExceptionType OPTION_COLLISION_RULE_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.team.option.collisionRule.unchanged"));

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("team").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(((LiteralArgumentBuilder)CommandManager.literal("list").executes((context) -> {
         return executeListTeams((ServerCommandSource)context.getSource());
      })).then(CommandManager.argument("team", TeamArgumentType.team()).executes((context) -> {
         return executeListMembers((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"));
      })))).then(CommandManager.literal("add").then(((RequiredArgumentBuilder)CommandManager.argument("team", StringArgumentType.word()).executes((context) -> {
         return executeAdd((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "team"));
      })).then(CommandManager.argument("displayName", TextArgumentType.text()).executes((context) -> {
         return executeAdd((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "team"), TextArgumentType.getTextArgument(context, "displayName"));
      }))))).then(CommandManager.literal("remove").then(CommandManager.argument("team", TeamArgumentType.team()).executes((context) -> {
         return executeRemove((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"));
      })))).then(CommandManager.literal("empty").then(CommandManager.argument("team", TeamArgumentType.team()).executes((context) -> {
         return executeEmpty((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"));
      })))).then(CommandManager.literal("join").then(((RequiredArgumentBuilder)CommandManager.argument("team", TeamArgumentType.team()).executes((context) -> {
         return executeJoin((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), Collections.singleton(((ServerCommandSource)context.getSource()).getEntityOrThrow().getEntityName()));
      })).then(CommandManager.argument("members", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).executes((context) -> {
         return executeJoin((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "members"));
      }))))).then(CommandManager.literal("leave").then(CommandManager.argument("members", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).executes((context) -> {
         return executeLeave((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "members"));
      })))).then(CommandManager.literal("modify").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("team", TeamArgumentType.team()).then(CommandManager.literal("displayName").then(CommandManager.argument("displayName", TextArgumentType.text()).executes((context) -> {
         return executeModifyDisplayName((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), TextArgumentType.getTextArgument(context, "displayName"));
      })))).then(CommandManager.literal("color").then(CommandManager.argument("value", ColorArgumentType.color()).executes((context) -> {
         return executeModifyColor((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), ColorArgumentType.getColor(context, "value"));
      })))).then(CommandManager.literal("friendlyFire").then(CommandManager.argument("allowed", BoolArgumentType.bool()).executes((context) -> {
         return executeModifyFriendlyFire((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), BoolArgumentType.getBool(context, "allowed"));
      })))).then(CommandManager.literal("seeFriendlyInvisibles").then(CommandManager.argument("allowed", BoolArgumentType.bool()).executes((context) -> {
         return executeModifySeeFriendlyInvisibles((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), BoolArgumentType.getBool(context, "allowed"));
      })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("nametagVisibility").then(CommandManager.literal("never").executes((context) -> {
         return executeModifyNametagVisibility((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.VisibilityRule.NEVER);
      }))).then(CommandManager.literal("hideForOtherTeams").executes((context) -> {
         return executeModifyNametagVisibility((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS);
      }))).then(CommandManager.literal("hideForOwnTeam").executes((context) -> {
         return executeModifyNametagVisibility((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.VisibilityRule.HIDE_FOR_OWN_TEAM);
      }))).then(CommandManager.literal("always").executes((context) -> {
         return executeModifyNametagVisibility((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.VisibilityRule.ALWAYS);
      })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("deathMessageVisibility").then(CommandManager.literal("never").executes((context) -> {
         return executeModifyDeathMessageVisibility((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.VisibilityRule.NEVER);
      }))).then(CommandManager.literal("hideForOtherTeams").executes((context) -> {
         return executeModifyDeathMessageVisibility((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS);
      }))).then(CommandManager.literal("hideForOwnTeam").executes((context) -> {
         return executeModifyDeathMessageVisibility((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.VisibilityRule.HIDE_FOR_OWN_TEAM);
      }))).then(CommandManager.literal("always").executes((context) -> {
         return executeModifyDeathMessageVisibility((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.VisibilityRule.ALWAYS);
      })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("collisionRule").then(CommandManager.literal("never").executes((context) -> {
         return executeModifyCollisionRule((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.CollisionRule.NEVER);
      }))).then(CommandManager.literal("pushOwnTeam").executes((context) -> {
         return executeModifyCollisionRule((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.CollisionRule.PUSH_OWN_TEAM);
      }))).then(CommandManager.literal("pushOtherTeams").executes((context) -> {
         return executeModifyCollisionRule((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS);
      }))).then(CommandManager.literal("always").executes((context) -> {
         return executeModifyCollisionRule((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), AbstractTeam.CollisionRule.ALWAYS);
      })))).then(CommandManager.literal("prefix").then(CommandManager.argument("prefix", TextArgumentType.text()).executes((context) -> {
         return executeModifyPrefix((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), TextArgumentType.getTextArgument(context, "prefix"));
      })))).then(CommandManager.literal("suffix").then(CommandManager.argument("suffix", TextArgumentType.text()).executes((context) -> {
         return executeModifySuffix((ServerCommandSource)context.getSource(), TeamArgumentType.getTeam(context, "team"), TextArgumentType.getTextArgument(context, "suffix"));
      }))))));
   }

   private static int executeLeave(ServerCommandSource source, Collection members) {
      Scoreboard lv = source.getServer().getScoreboard();
      Iterator var3 = members.iterator();

      while(var3.hasNext()) {
         String string = (String)var3.next();
         lv.clearPlayerTeam(string);
      }

      if (members.size() == 1) {
         source.sendFeedback(Text.translatable("commands.team.leave.success.single", members.iterator().next()), true);
      } else {
         source.sendFeedback(Text.translatable("commands.team.leave.success.multiple", members.size()), true);
      }

      return members.size();
   }

   private static int executeJoin(ServerCommandSource source, Team team, Collection members) {
      Scoreboard lv = source.getServer().getScoreboard();
      Iterator var4 = members.iterator();

      while(var4.hasNext()) {
         String string = (String)var4.next();
         lv.addPlayerToTeam(string, team);
      }

      if (members.size() == 1) {
         source.sendFeedback(Text.translatable("commands.team.join.success.single", members.iterator().next(), team.getFormattedName()), true);
      } else {
         source.sendFeedback(Text.translatable("commands.team.join.success.multiple", members.size(), team.getFormattedName()), true);
      }

      return members.size();
   }

   private static int executeModifyNametagVisibility(ServerCommandSource source, Team team, AbstractTeam.VisibilityRule visibility) throws CommandSyntaxException {
      if (team.getNameTagVisibilityRule() == visibility) {
         throw OPTION_NAMETAG_VISIBILITY_UNCHANGED_EXCEPTION.create();
      } else {
         team.setNameTagVisibilityRule(visibility);
         source.sendFeedback(Text.translatable("commands.team.option.nametagVisibility.success", team.getFormattedName(), visibility.getDisplayName()), true);
         return 0;
      }
   }

   private static int executeModifyDeathMessageVisibility(ServerCommandSource source, Team team, AbstractTeam.VisibilityRule visibility) throws CommandSyntaxException {
      if (team.getDeathMessageVisibilityRule() == visibility) {
         throw OPTION_DEATH_MESSAGE_VISIBILITY_UNCHANGED_EXCEPTION.create();
      } else {
         team.setDeathMessageVisibilityRule(visibility);
         source.sendFeedback(Text.translatable("commands.team.option.deathMessageVisibility.success", team.getFormattedName(), visibility.getDisplayName()), true);
         return 0;
      }
   }

   private static int executeModifyCollisionRule(ServerCommandSource source, Team team, AbstractTeam.CollisionRule collisionRule) throws CommandSyntaxException {
      if (team.getCollisionRule() == collisionRule) {
         throw OPTION_COLLISION_RULE_UNCHANGED_EXCEPTION.create();
      } else {
         team.setCollisionRule(collisionRule);
         source.sendFeedback(Text.translatable("commands.team.option.collisionRule.success", team.getFormattedName(), collisionRule.getDisplayName()), true);
         return 0;
      }
   }

   private static int executeModifySeeFriendlyInvisibles(ServerCommandSource source, Team team, boolean allowed) throws CommandSyntaxException {
      if (team.shouldShowFriendlyInvisibles() == allowed) {
         if (allowed) {
            throw OPTION_SEE_FRIENDLY_INVISIBLES_ALREADY_ENABLED_EXCEPTION.create();
         } else {
            throw OPTION_SEE_FRIENDLY_INVISIBLES_ALREADY_DISABLED_EXCEPTION.create();
         }
      } else {
         team.setShowFriendlyInvisibles(allowed);
         source.sendFeedback(Text.translatable("commands.team.option.seeFriendlyInvisibles." + (allowed ? "enabled" : "disabled"), team.getFormattedName()), true);
         return 0;
      }
   }

   private static int executeModifyFriendlyFire(ServerCommandSource source, Team team, boolean allowed) throws CommandSyntaxException {
      if (team.isFriendlyFireAllowed() == allowed) {
         if (allowed) {
            throw OPTION_FRIENDLY_FIRE_ALREADY_ENABLED_EXCEPTION.create();
         } else {
            throw OPTION_FRIENDLY_FIRE_ALREADY_DISABLED_EXCEPTION.create();
         }
      } else {
         team.setFriendlyFireAllowed(allowed);
         source.sendFeedback(Text.translatable("commands.team.option.friendlyfire." + (allowed ? "enabled" : "disabled"), team.getFormattedName()), true);
         return 0;
      }
   }

   private static int executeModifyDisplayName(ServerCommandSource source, Team team, Text displayName) throws CommandSyntaxException {
      if (team.getDisplayName().equals(displayName)) {
         throw OPTION_NAME_UNCHANGED_EXCEPTION.create();
      } else {
         team.setDisplayName(displayName);
         source.sendFeedback(Text.translatable("commands.team.option.name.success", team.getFormattedName()), true);
         return 0;
      }
   }

   private static int executeModifyColor(ServerCommandSource source, Team team, Formatting color) throws CommandSyntaxException {
      if (team.getColor() == color) {
         throw OPTION_COLOR_UNCHANGED_EXCEPTION.create();
      } else {
         team.setColor(color);
         source.sendFeedback(Text.translatable("commands.team.option.color.success", team.getFormattedName(), color.getName()), true);
         return 0;
      }
   }

   private static int executeEmpty(ServerCommandSource source, Team team) throws CommandSyntaxException {
      Scoreboard lv = source.getServer().getScoreboard();
      Collection collection = Lists.newArrayList(team.getPlayerList());
      if (collection.isEmpty()) {
         throw EMPTY_UNCHANGED_EXCEPTION.create();
      } else {
         Iterator var4 = collection.iterator();

         while(var4.hasNext()) {
            String string = (String)var4.next();
            lv.removePlayerFromTeam(string, team);
         }

         source.sendFeedback(Text.translatable("commands.team.empty.success", collection.size(), team.getFormattedName()), true);
         return collection.size();
      }
   }

   private static int executeRemove(ServerCommandSource source, Team team) {
      Scoreboard lv = source.getServer().getScoreboard();
      lv.removeTeam(team);
      source.sendFeedback(Text.translatable("commands.team.remove.success", team.getFormattedName()), true);
      return lv.getTeams().size();
   }

   private static int executeAdd(ServerCommandSource source, String team) throws CommandSyntaxException {
      return executeAdd(source, team, Text.literal(team));
   }

   private static int executeAdd(ServerCommandSource source, String team, Text displayName) throws CommandSyntaxException {
      Scoreboard lv = source.getServer().getScoreboard();
      if (lv.getTeam(team) != null) {
         throw ADD_DUPLICATE_EXCEPTION.create();
      } else {
         Team lv2 = lv.addTeam(team);
         lv2.setDisplayName(displayName);
         source.sendFeedback(Text.translatable("commands.team.add.success", lv2.getFormattedName()), true);
         return lv.getTeams().size();
      }
   }

   private static int executeListMembers(ServerCommandSource source, Team team) {
      Collection collection = team.getPlayerList();
      if (collection.isEmpty()) {
         source.sendFeedback(Text.translatable("commands.team.list.members.empty", team.getFormattedName()), false);
      } else {
         source.sendFeedback(Text.translatable("commands.team.list.members.success", team.getFormattedName(), collection.size(), Texts.joinOrdered(collection)), false);
      }

      return collection.size();
   }

   private static int executeListTeams(ServerCommandSource source) {
      Collection collection = source.getServer().getScoreboard().getTeams();
      if (collection.isEmpty()) {
         source.sendFeedback(Text.translatable("commands.team.list.teams.empty"), false);
      } else {
         source.sendFeedback(Text.translatable("commands.team.list.teams.success", collection.size(), Texts.join(collection, Team::getFormattedName)), false);
      }

      return collection.size();
   }

   private static int executeModifyPrefix(ServerCommandSource source, Team team, Text prefix) {
      team.setPrefix(prefix);
      source.sendFeedback(Text.translatable("commands.team.option.prefix.success", prefix), false);
      return 1;
   }

   private static int executeModifySuffix(ServerCommandSource source, Team team, Text suffix) {
      team.setSuffix(suffix);
      source.sendFeedback(Text.translatable("commands.team.option.suffix.success", suffix), false);
      return 1;
   }
}
