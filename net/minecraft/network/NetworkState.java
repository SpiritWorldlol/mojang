package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.packet.BundleSplitterPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.JigsawGeneratingC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.MessageAcknowledgmentC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerSessionC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryBlockNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryEntityNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyLockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateJigsawC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkBiomeDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.FeaturesS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayPingS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.network.packet.s2c.play.ServerMetadataS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.network.packet.s2c.play.UnlockRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryPongS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public enum NetworkState implements PacketBundleHandler.BundlerGetter {
   HANDSHAKING(-1, createPacketHandlerInitializer().setup(NetworkSide.SERVERBOUND, (new PacketHandler()).register(HandshakeC2SPacket.class, HandshakeC2SPacket::new))),
   PLAY(0, createPacketHandlerInitializer().setup(NetworkSide.CLIENTBOUND, (new PacketHandler()).registerBundlePacket(BundleS2CPacket.class, BundleS2CPacket::new).register(EntitySpawnS2CPacket.class, EntitySpawnS2CPacket::new).register(ExperienceOrbSpawnS2CPacket.class, ExperienceOrbSpawnS2CPacket::new).register(PlayerSpawnS2CPacket.class, PlayerSpawnS2CPacket::new).register(EntityAnimationS2CPacket.class, EntityAnimationS2CPacket::new).register(StatisticsS2CPacket.class, StatisticsS2CPacket::new).register(PlayerActionResponseS2CPacket.class, PlayerActionResponseS2CPacket::new).register(BlockBreakingProgressS2CPacket.class, BlockBreakingProgressS2CPacket::new).register(BlockEntityUpdateS2CPacket.class, BlockEntityUpdateS2CPacket::new).register(BlockEventS2CPacket.class, BlockEventS2CPacket::new).register(BlockUpdateS2CPacket.class, BlockUpdateS2CPacket::new).register(BossBarS2CPacket.class, BossBarS2CPacket::new).register(DifficultyS2CPacket.class, DifficultyS2CPacket::new).register(ChunkBiomeDataS2CPacket.class, ChunkBiomeDataS2CPacket::new).register(ClearTitleS2CPacket.class, ClearTitleS2CPacket::new).register(CommandSuggestionsS2CPacket.class, CommandSuggestionsS2CPacket::new).register(CommandTreeS2CPacket.class, CommandTreeS2CPacket::new).register(CloseScreenS2CPacket.class, CloseScreenS2CPacket::new).register(InventoryS2CPacket.class, InventoryS2CPacket::new).register(ScreenHandlerPropertyUpdateS2CPacket.class, ScreenHandlerPropertyUpdateS2CPacket::new).register(ScreenHandlerSlotUpdateS2CPacket.class, ScreenHandlerSlotUpdateS2CPacket::new).register(CooldownUpdateS2CPacket.class, CooldownUpdateS2CPacket::new).register(ChatSuggestionsS2CPacket.class, ChatSuggestionsS2CPacket::new).register(CustomPayloadS2CPacket.class, CustomPayloadS2CPacket::new).register(EntityDamageS2CPacket.class, EntityDamageS2CPacket::new).register(RemoveMessageS2CPacket.class, RemoveMessageS2CPacket::new).register(DisconnectS2CPacket.class, DisconnectS2CPacket::new).register(ProfilelessChatMessageS2CPacket.class, ProfilelessChatMessageS2CPacket::new).register(EntityStatusS2CPacket.class, EntityStatusS2CPacket::new).register(ExplosionS2CPacket.class, ExplosionS2CPacket::new).register(UnloadChunkS2CPacket.class, UnloadChunkS2CPacket::new).register(GameStateChangeS2CPacket.class, GameStateChangeS2CPacket::new).register(OpenHorseScreenS2CPacket.class, OpenHorseScreenS2CPacket::new).register(DamageTiltS2CPacket.class, DamageTiltS2CPacket::new).register(WorldBorderInitializeS2CPacket.class, WorldBorderInitializeS2CPacket::new).register(KeepAliveS2CPacket.class, KeepAliveS2CPacket::new).register(ChunkDataS2CPacket.class, ChunkDataS2CPacket::new).register(WorldEventS2CPacket.class, WorldEventS2CPacket::new).register(ParticleS2CPacket.class, ParticleS2CPacket::new).register(LightUpdateS2CPacket.class, LightUpdateS2CPacket::new).register(GameJoinS2CPacket.class, GameJoinS2CPacket::new).register(MapUpdateS2CPacket.class, MapUpdateS2CPacket::new).register(SetTradeOffersS2CPacket.class, SetTradeOffersS2CPacket::new).register(EntityS2CPacket.MoveRelative.class, EntityS2CPacket.MoveRelative::read).register(EntityS2CPacket.RotateAndMoveRelative.class, EntityS2CPacket.RotateAndMoveRelative::read).register(EntityS2CPacket.Rotate.class, EntityS2CPacket.Rotate::read).register(VehicleMoveS2CPacket.class, VehicleMoveS2CPacket::new).register(OpenWrittenBookS2CPacket.class, OpenWrittenBookS2CPacket::new).register(OpenScreenS2CPacket.class, OpenScreenS2CPacket::new).register(SignEditorOpenS2CPacket.class, SignEditorOpenS2CPacket::new).register(PlayPingS2CPacket.class, PlayPingS2CPacket::new).register(CraftFailedResponseS2CPacket.class, CraftFailedResponseS2CPacket::new).register(PlayerAbilitiesS2CPacket.class, PlayerAbilitiesS2CPacket::new).register(ChatMessageS2CPacket.class, ChatMessageS2CPacket::new).register(EndCombatS2CPacket.class, EndCombatS2CPacket::new).register(EnterCombatS2CPacket.class, EnterCombatS2CPacket::new).register(DeathMessageS2CPacket.class, DeathMessageS2CPacket::new).register(PlayerRemoveS2CPacket.class, PlayerRemoveS2CPacket::new).register(PlayerListS2CPacket.class, PlayerListS2CPacket::new).register(LookAtS2CPacket.class, LookAtS2CPacket::new).register(PlayerPositionLookS2CPacket.class, PlayerPositionLookS2CPacket::new).register(UnlockRecipesS2CPacket.class, UnlockRecipesS2CPacket::new).register(EntitiesDestroyS2CPacket.class, EntitiesDestroyS2CPacket::new).register(RemoveEntityStatusEffectS2CPacket.class, RemoveEntityStatusEffectS2CPacket::new).register(ResourcePackSendS2CPacket.class, ResourcePackSendS2CPacket::new).register(PlayerRespawnS2CPacket.class, PlayerRespawnS2CPacket::new).register(EntitySetHeadYawS2CPacket.class, EntitySetHeadYawS2CPacket::new).register(ChunkDeltaUpdateS2CPacket.class, ChunkDeltaUpdateS2CPacket::new).register(SelectAdvancementTabS2CPacket.class, SelectAdvancementTabS2CPacket::new).register(ServerMetadataS2CPacket.class, ServerMetadataS2CPacket::new).register(OverlayMessageS2CPacket.class, OverlayMessageS2CPacket::new).register(WorldBorderCenterChangedS2CPacket.class, WorldBorderCenterChangedS2CPacket::new).register(WorldBorderInterpolateSizeS2CPacket.class, WorldBorderInterpolateSizeS2CPacket::new).register(WorldBorderSizeChangedS2CPacket.class, WorldBorderSizeChangedS2CPacket::new).register(WorldBorderWarningTimeChangedS2CPacket.class, WorldBorderWarningTimeChangedS2CPacket::new).register(WorldBorderWarningBlocksChangedS2CPacket.class, WorldBorderWarningBlocksChangedS2CPacket::new).register(SetCameraEntityS2CPacket.class, SetCameraEntityS2CPacket::new).register(UpdateSelectedSlotS2CPacket.class, UpdateSelectedSlotS2CPacket::new).register(ChunkRenderDistanceCenterS2CPacket.class, ChunkRenderDistanceCenterS2CPacket::new).register(ChunkLoadDistanceS2CPacket.class, ChunkLoadDistanceS2CPacket::new).register(PlayerSpawnPositionS2CPacket.class, PlayerSpawnPositionS2CPacket::new).register(ScoreboardDisplayS2CPacket.class, ScoreboardDisplayS2CPacket::new).register(EntityTrackerUpdateS2CPacket.class, EntityTrackerUpdateS2CPacket::new).register(EntityAttachS2CPacket.class, EntityAttachS2CPacket::new).register(EntityVelocityUpdateS2CPacket.class, EntityVelocityUpdateS2CPacket::new).register(EntityEquipmentUpdateS2CPacket.class, EntityEquipmentUpdateS2CPacket::new).register(ExperienceBarUpdateS2CPacket.class, ExperienceBarUpdateS2CPacket::new).register(HealthUpdateS2CPacket.class, HealthUpdateS2CPacket::new).register(ScoreboardObjectiveUpdateS2CPacket.class, ScoreboardObjectiveUpdateS2CPacket::new).register(EntityPassengersSetS2CPacket.class, EntityPassengersSetS2CPacket::new).register(TeamS2CPacket.class, TeamS2CPacket::new).register(ScoreboardPlayerUpdateS2CPacket.class, ScoreboardPlayerUpdateS2CPacket::new).register(SimulationDistanceS2CPacket.class, SimulationDistanceS2CPacket::new).register(SubtitleS2CPacket.class, SubtitleS2CPacket::new).register(WorldTimeUpdateS2CPacket.class, WorldTimeUpdateS2CPacket::new).register(TitleS2CPacket.class, TitleS2CPacket::new).register(TitleFadeS2CPacket.class, TitleFadeS2CPacket::new).register(PlaySoundFromEntityS2CPacket.class, PlaySoundFromEntityS2CPacket::new).register(PlaySoundS2CPacket.class, PlaySoundS2CPacket::new).register(StopSoundS2CPacket.class, StopSoundS2CPacket::new).register(GameMessageS2CPacket.class, GameMessageS2CPacket::new).register(PlayerListHeaderS2CPacket.class, PlayerListHeaderS2CPacket::new).register(NbtQueryResponseS2CPacket.class, NbtQueryResponseS2CPacket::new).register(ItemPickupAnimationS2CPacket.class, ItemPickupAnimationS2CPacket::new).register(EntityPositionS2CPacket.class, EntityPositionS2CPacket::new).register(AdvancementUpdateS2CPacket.class, AdvancementUpdateS2CPacket::new).register(EntityAttributesS2CPacket.class, EntityAttributesS2CPacket::new).register(FeaturesS2CPacket.class, FeaturesS2CPacket::new).register(EntityStatusEffectS2CPacket.class, EntityStatusEffectS2CPacket::new).register(SynchronizeRecipesS2CPacket.class, SynchronizeRecipesS2CPacket::new).register(SynchronizeTagsS2CPacket.class, SynchronizeTagsS2CPacket::new)).setup(NetworkSide.SERVERBOUND, (new PacketHandler()).register(TeleportConfirmC2SPacket.class, TeleportConfirmC2SPacket::new).register(QueryBlockNbtC2SPacket.class, QueryBlockNbtC2SPacket::new).register(UpdateDifficultyC2SPacket.class, UpdateDifficultyC2SPacket::new).register(MessageAcknowledgmentC2SPacket.class, MessageAcknowledgmentC2SPacket::new).register(CommandExecutionC2SPacket.class, CommandExecutionC2SPacket::new).register(ChatMessageC2SPacket.class, ChatMessageC2SPacket::new).register(PlayerSessionC2SPacket.class, PlayerSessionC2SPacket::new).register(ClientStatusC2SPacket.class, ClientStatusC2SPacket::new).register(ClientSettingsC2SPacket.class, ClientSettingsC2SPacket::new).register(RequestCommandCompletionsC2SPacket.class, RequestCommandCompletionsC2SPacket::new).register(ButtonClickC2SPacket.class, ButtonClickC2SPacket::new).register(ClickSlotC2SPacket.class, ClickSlotC2SPacket::new).register(CloseHandledScreenC2SPacket.class, CloseHandledScreenC2SPacket::new).register(CustomPayloadC2SPacket.class, CustomPayloadC2SPacket::new).register(BookUpdateC2SPacket.class, BookUpdateC2SPacket::new).register(QueryEntityNbtC2SPacket.class, QueryEntityNbtC2SPacket::new).register(PlayerInteractEntityC2SPacket.class, PlayerInteractEntityC2SPacket::new).register(JigsawGeneratingC2SPacket.class, JigsawGeneratingC2SPacket::new).register(KeepAliveC2SPacket.class, KeepAliveC2SPacket::new).register(UpdateDifficultyLockC2SPacket.class, UpdateDifficultyLockC2SPacket::new).register(PlayerMoveC2SPacket.PositionAndOnGround.class, PlayerMoveC2SPacket.PositionAndOnGround::read).register(PlayerMoveC2SPacket.Full.class, PlayerMoveC2SPacket.Full::read).register(PlayerMoveC2SPacket.LookAndOnGround.class, PlayerMoveC2SPacket.LookAndOnGround::read).register(PlayerMoveC2SPacket.OnGroundOnly.class, PlayerMoveC2SPacket.OnGroundOnly::read).register(VehicleMoveC2SPacket.class, VehicleMoveC2SPacket::new).register(BoatPaddleStateC2SPacket.class, BoatPaddleStateC2SPacket::new).register(PickFromInventoryC2SPacket.class, PickFromInventoryC2SPacket::new).register(CraftRequestC2SPacket.class, CraftRequestC2SPacket::new).register(UpdatePlayerAbilitiesC2SPacket.class, UpdatePlayerAbilitiesC2SPacket::new).register(PlayerActionC2SPacket.class, PlayerActionC2SPacket::new).register(ClientCommandC2SPacket.class, ClientCommandC2SPacket::new).register(PlayerInputC2SPacket.class, PlayerInputC2SPacket::new).register(PlayPongC2SPacket.class, PlayPongC2SPacket::new).register(RecipeCategoryOptionsC2SPacket.class, RecipeCategoryOptionsC2SPacket::new).register(RecipeBookDataC2SPacket.class, RecipeBookDataC2SPacket::new).register(RenameItemC2SPacket.class, RenameItemC2SPacket::new).register(ResourcePackStatusC2SPacket.class, ResourcePackStatusC2SPacket::new).register(AdvancementTabC2SPacket.class, AdvancementTabC2SPacket::new).register(SelectMerchantTradeC2SPacket.class, SelectMerchantTradeC2SPacket::new).register(UpdateBeaconC2SPacket.class, UpdateBeaconC2SPacket::new).register(UpdateSelectedSlotC2SPacket.class, UpdateSelectedSlotC2SPacket::new).register(UpdateCommandBlockC2SPacket.class, UpdateCommandBlockC2SPacket::new).register(UpdateCommandBlockMinecartC2SPacket.class, UpdateCommandBlockMinecartC2SPacket::new).register(CreativeInventoryActionC2SPacket.class, CreativeInventoryActionC2SPacket::new).register(UpdateJigsawC2SPacket.class, UpdateJigsawC2SPacket::new).register(UpdateStructureBlockC2SPacket.class, UpdateStructureBlockC2SPacket::new).register(UpdateSignC2SPacket.class, UpdateSignC2SPacket::new).register(HandSwingC2SPacket.class, HandSwingC2SPacket::new).register(SpectatorTeleportC2SPacket.class, SpectatorTeleportC2SPacket::new).register(PlayerInteractBlockC2SPacket.class, PlayerInteractBlockC2SPacket::new).register(PlayerInteractItemC2SPacket.class, PlayerInteractItemC2SPacket::new))),
   STATUS(1, createPacketHandlerInitializer().setup(NetworkSide.SERVERBOUND, (new PacketHandler()).register(QueryRequestC2SPacket.class, QueryRequestC2SPacket::new).register(QueryPingC2SPacket.class, QueryPingC2SPacket::new)).setup(NetworkSide.CLIENTBOUND, (new PacketHandler()).register(QueryResponseS2CPacket.class, QueryResponseS2CPacket::new).register(QueryPongS2CPacket.class, QueryPongS2CPacket::new))),
   LOGIN(2, createPacketHandlerInitializer().setup(NetworkSide.CLIENTBOUND, (new PacketHandler()).register(LoginDisconnectS2CPacket.class, LoginDisconnectS2CPacket::new).register(LoginHelloS2CPacket.class, LoginHelloS2CPacket::new).register(LoginSuccessS2CPacket.class, LoginSuccessS2CPacket::new).register(LoginCompressionS2CPacket.class, LoginCompressionS2CPacket::new).register(LoginQueryRequestS2CPacket.class, LoginQueryRequestS2CPacket::new)).setup(NetworkSide.SERVERBOUND, (new PacketHandler()).register(LoginHelloC2SPacket.class, LoginHelloC2SPacket::new).register(LoginKeyC2SPacket.class, LoginKeyC2SPacket::new).register(LoginQueryResponseC2SPacket.class, LoginQueryResponseC2SPacket::new)));

   public static final int field_41866 = -1;
   private static final int NULL_PACKET_ID_OR_MIN_STATE_ID = -1;
   private static final int MAX_STATE_ID = 2;
   private static final NetworkState[] STATES = new NetworkState[4];
   private static final Map HANDLER_STATE_MAP = Maps.newHashMap();
   private final int stateId;
   private final Map packetHandlers;

   private static PacketHandlerInitializer createPacketHandlerInitializer() {
      return new PacketHandlerInitializer();
   }

   private NetworkState(int id, PacketHandlerInitializer initializer) {
      this.stateId = id;
      this.packetHandlers = initializer.packetHandlers;
   }

   public int getPacketId(NetworkSide side, Packet packet) {
      return ((PacketHandler)this.packetHandlers.get(side)).getId(packet.getClass());
   }

   public PacketBundleHandler getBundler(NetworkSide side) {
      return ((PacketHandler)this.packetHandlers.get(side)).getBundler();
   }

   @Debug
   public Int2ObjectMap getPacketIdToPacketMap(NetworkSide side) {
      Int2ObjectMap int2ObjectMap = new Int2ObjectOpenHashMap();
      PacketHandler lv = (PacketHandler)this.packetHandlers.get(side);
      if (lv == null) {
         return Int2ObjectMaps.emptyMap();
      } else {
         lv.packetIds.forEach((packetId, integer) -> {
            int2ObjectMap.put(integer, packetId);
         });
         return int2ObjectMap;
      }
   }

   @Nullable
   public Packet getPacketHandler(NetworkSide side, int packetId, PacketByteBuf buf) {
      return ((PacketHandler)this.packetHandlers.get(side)).createPacket(packetId, buf);
   }

   public int getId() {
      return this.stateId;
   }

   @Nullable
   public static NetworkState byId(int id) {
      return id >= -1 && id <= 2 ? STATES[id - -1] : null;
   }

   @Nullable
   public static NetworkState getPacketHandlerState(Packet handler) {
      return (NetworkState)HANDLER_STATE_MAP.get(handler.getClass());
   }

   // $FF: synthetic method
   private static NetworkState[] method_36943() {
      return new NetworkState[]{HANDSHAKING, PLAY, STATUS, LOGIN};
   }

   static {
      NetworkState[] var0 = values();
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         NetworkState lv = var0[var2];
         int i = lv.getId();
         if (i < -1 || i > 2) {
            throw new Error("Invalid protocol ID " + i);
         }

         STATES[i - -1] = lv;
         lv.packetHandlers.forEach((side, handler) -> {
            handler.forEachPacketType((packetClass) -> {
               if (HANDLER_STATE_MAP.containsKey(packetClass) && HANDLER_STATE_MAP.get(packetClass) != lv) {
                  throw new IllegalStateException("Packet " + packetClass + " is already assigned to protocol " + HANDLER_STATE_MAP.get(packetClass) + " - can't reassign to " + lv);
               } else {
                  HANDLER_STATE_MAP.put(packetClass, lv);
               }
            });
         });
      }

   }

   static class PacketHandlerInitializer {
      final Map packetHandlers = Maps.newEnumMap(NetworkSide.class);

      public PacketHandlerInitializer setup(NetworkSide side, PacketHandler handler) {
         this.packetHandlers.put(side, handler);
         return this;
      }
   }

   static class PacketHandler {
      private static final Logger LOGGER = LogUtils.getLogger();
      final Object2IntMap packetIds = (Object2IntMap)Util.make(new Object2IntOpenHashMap(), (map) -> {
         map.defaultReturnValue(-1);
      });
      private final List packetFactories = Lists.newArrayList();
      private PacketBundleHandler bundler;
      private final Set bundlePacketTypes;

      PacketHandler() {
         this.bundler = PacketBundleHandler.NOOP;
         this.bundlePacketTypes = new HashSet();
      }

      public PacketHandler register(Class type, Function packetFactory) {
         int i = this.packetFactories.size();
         int j = this.packetIds.put(type, i);
         if (j != -1) {
            String string = "Packet " + type + " is already registered to ID " + j;
            LOGGER.error(LogUtils.FATAL_MARKER, string);
            throw new IllegalArgumentException(string);
         } else {
            this.packetFactories.add(packetFactory);
            return this;
         }
      }

      public PacketHandler registerBundlePacket(Class bundlePacketType, Function bundleFunction) {
         if (this.bundler != PacketBundleHandler.NOOP) {
            throw new IllegalStateException("Bundle packet already configured");
         } else {
            BundleSplitterPacket lv = new BundleSplitterPacket();
            this.register(BundleSplitterPacket.class, (buf) -> {
               return lv;
            });
            this.bundler = PacketBundleHandler.create(bundlePacketType, bundleFunction, lv);
            this.bundlePacketTypes.add(bundlePacketType);
            return this;
         }
      }

      public int getId(Class packet) {
         return this.packetIds.getInt(packet);
      }

      @Nullable
      public Packet createPacket(int id, PacketByteBuf buf) {
         Function function = (Function)this.packetFactories.get(id);
         return function != null ? (Packet)function.apply(buf) : null;
      }

      public void forEachPacketType(Consumer consumer) {
         this.packetIds.keySet().stream().filter((type) -> {
            return type != BundleSplitterPacket.class;
         }).forEach(consumer);
         this.bundlePacketTypes.forEach(consumer);
      }

      public PacketBundleHandler getBundler() {
         return this.bundler;
      }
   }
}
