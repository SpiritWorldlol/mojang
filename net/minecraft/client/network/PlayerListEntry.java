package net.minecraft.client.network;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.message.MessageVerifier;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PlayerListEntry {
   private final GameProfile profile;
   private final Map textures = Maps.newEnumMap(MinecraftProfileTexture.Type.class);
   private GameMode gameMode;
   private int latency;
   private boolean texturesLoaded;
   @Nullable
   private String model;
   @Nullable
   private Text displayName;
   @Nullable
   private PublicPlayerSession session;
   private MessageVerifier messageVerifier;

   public PlayerListEntry(GameProfile profile, boolean secureChatEnforced) {
      this.gameMode = GameMode.DEFAULT;
      this.profile = profile;
      this.messageVerifier = getInitialVerifier(secureChatEnforced);
   }

   public GameProfile getProfile() {
      return this.profile;
   }

   @Nullable
   public PublicPlayerSession getSession() {
      return this.session;
   }

   public MessageVerifier getMessageVerifier() {
      return this.messageVerifier;
   }

   public boolean hasPublicKey() {
      return this.session != null;
   }

   protected void setSession(PublicPlayerSession session) {
      this.session = session;
      this.messageVerifier = session.createVerifier();
   }

   protected void resetSession(boolean secureChatEnforced) {
      this.session = null;
      this.messageVerifier = getInitialVerifier(secureChatEnforced);
   }

   private static MessageVerifier getInitialVerifier(boolean secureChatEnforced) {
      return secureChatEnforced ? MessageVerifier.UNVERIFIED : MessageVerifier.NO_SIGNATURE;
   }

   public GameMode getGameMode() {
      return this.gameMode;
   }

   protected void setGameMode(GameMode gameMode) {
      this.gameMode = gameMode;
   }

   public int getLatency() {
      return this.latency;
   }

   protected void setLatency(int latency) {
      this.latency = latency;
   }

   public boolean hasCape() {
      return this.getCapeTexture() != null;
   }

   public boolean hasSkinTexture() {
      return this.getSkinTexture() != null;
   }

   public String getModel() {
      return this.model == null ? DefaultSkinHelper.getModel(this.profile.getId()) : this.model;
   }

   public Identifier getSkinTexture() {
      this.loadTextures();
      return (Identifier)MoreObjects.firstNonNull((Identifier)this.textures.get(Type.SKIN), DefaultSkinHelper.getTexture(this.profile.getId()));
   }

   @Nullable
   public Identifier getCapeTexture() {
      this.loadTextures();
      return (Identifier)this.textures.get(Type.CAPE);
   }

   @Nullable
   public Identifier getElytraTexture() {
      this.loadTextures();
      return (Identifier)this.textures.get(Type.ELYTRA);
   }

   @Nullable
   public Team getScoreboardTeam() {
      return MinecraftClient.getInstance().world.getScoreboard().getPlayerTeam(this.getProfile().getName());
   }

   protected void loadTextures() {
      synchronized(this) {
         if (!this.texturesLoaded) {
            this.texturesLoaded = true;
            MinecraftClient.getInstance().getSkinProvider().loadSkin(this.profile, (type, id, texture) -> {
               this.textures.put(type, id);
               if (type == Type.SKIN) {
                  this.model = texture.getMetadata("model");
                  if (this.model == null) {
                     this.model = "default";
                  }
               }

            }, true);
         }

      }
   }

   public void setDisplayName(@Nullable Text displayName) {
      this.displayName = displayName;
   }

   @Nullable
   public Text getDisplayName() {
      return this.displayName;
   }
}
