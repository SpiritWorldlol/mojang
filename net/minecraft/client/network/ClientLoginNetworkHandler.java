package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserBannedException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.security.PublicKey;
import java.time.Duration;
import java.util.function.Consumer;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientLoginNetworkHandler implements ClientLoginPacketListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final MinecraftClient client;
   @Nullable
   private final ServerInfo serverInfo;
   @Nullable
   private final Screen parentScreen;
   private final Consumer statusConsumer;
   private final ClientConnection connection;
   private GameProfile profile;
   private final boolean newWorld;
   @Nullable
   private final Duration worldLoadTime;
   @Nullable
   private String realmsMinigameName;

   public ClientLoginNetworkHandler(ClientConnection connection, MinecraftClient client, @Nullable ServerInfo serverInfo, @Nullable Screen parentScreen, boolean newWorld, @Nullable Duration worldLoadTime, Consumer statusConsumer) {
      this.connection = connection;
      this.client = client;
      this.serverInfo = serverInfo;
      this.parentScreen = parentScreen;
      this.statusConsumer = statusConsumer;
      this.newWorld = newWorld;
      this.worldLoadTime = worldLoadTime;
   }

   public void onHello(LoginHelloS2CPacket packet) {
      Cipher cipher;
      Cipher cipher2;
      String string;
      LoginKeyC2SPacket lv;
      try {
         SecretKey secretKey = NetworkEncryptionUtils.generateSecretKey();
         PublicKey publicKey = packet.getPublicKey();
         string = (new BigInteger(NetworkEncryptionUtils.computeServerId(packet.getServerId(), publicKey, secretKey))).toString(16);
         cipher = NetworkEncryptionUtils.cipherFromKey(2, secretKey);
         cipher2 = NetworkEncryptionUtils.cipherFromKey(1, secretKey);
         byte[] bs = packet.getNonce();
         lv = new LoginKeyC2SPacket(secretKey, publicKey, bs);
      } catch (Exception var9) {
         throw new IllegalStateException("Protocol error", var9);
      }

      this.statusConsumer.accept(Text.translatable("connect.authorizing"));
      NetworkUtils.EXECUTOR.submit(() -> {
         Text lvx = this.joinServerSession(string);
         if (lvx != null) {
            if (this.serverInfo == null || !this.serverInfo.isLocal()) {
               this.connection.disconnect(lvx);
               return;
            }

            LOGGER.warn(lvx.getString());
         }

         this.statusConsumer.accept(Text.translatable("connect.encrypting"));
         this.connection.send(lv, PacketCallbacks.always(() -> {
            this.connection.setupEncryption(cipher, cipher2);
         }));
      });
   }

   @Nullable
   private Text joinServerSession(String serverId) {
      try {
         this.getSessionService().joinServer(this.client.getSession().getProfile(), this.client.getSession().getAccessToken(), serverId);
         return null;
      } catch (AuthenticationUnavailableException var3) {
         return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.serversUnavailable"));
      } catch (InvalidCredentialsException var4) {
         return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.invalidSession"));
      } catch (InsufficientPrivilegesException var5) {
         return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.insufficientPrivileges"));
      } catch (UserBannedException var6) {
         return Text.translatable("disconnect.loginFailedInfo", Text.translatable("disconnect.loginFailedInfo.userBanned"));
      } catch (AuthenticationException var7) {
         return Text.translatable("disconnect.loginFailedInfo", var7.getMessage());
      }
   }

   private MinecraftSessionService getSessionService() {
      return this.client.getSessionService();
   }

   public void onSuccess(LoginSuccessS2CPacket packet) {
      this.statusConsumer.accept(Text.translatable("connect.joining"));
      this.profile = packet.getProfile();
      this.connection.setState(NetworkState.PLAY);
      this.connection.setPacketListener(new ClientPlayNetworkHandler(this.client, this.parentScreen, this.connection, this.serverInfo, this.profile, this.client.getTelemetryManager().createWorldSession(this.newWorld, this.worldLoadTime, this.realmsMinigameName)));
   }

   public void onDisconnected(Text reason) {
      if (this.parentScreen != null && this.parentScreen instanceof RealmsScreen) {
         this.client.setScreen(new DisconnectedRealmsScreen(this.parentScreen, ScreenTexts.CONNECT_FAILED, reason));
      } else {
         this.client.setScreen(new DisconnectedScreen(this.parentScreen, ScreenTexts.CONNECT_FAILED, reason));
      }

   }

   public boolean isConnectionOpen() {
      return this.connection.isOpen();
   }

   public void onDisconnect(LoginDisconnectS2CPacket packet) {
      this.connection.disconnect(packet.getReason());
   }

   public void onCompression(LoginCompressionS2CPacket packet) {
      if (!this.connection.isLocal()) {
         this.connection.setCompressionThreshold(packet.getCompressionThreshold(), false);
      }

   }

   public void onQueryRequest(LoginQueryRequestS2CPacket packet) {
      this.statusConsumer.accept(Text.translatable("connect.negotiating"));
      this.connection.send(new LoginQueryResponseC2SPacket(packet.getQueryId(), (PacketByteBuf)null));
   }

   public void setRealmsMinigameName(String realmsMinigameName) {
      this.realmsMinigameName = realmsMinigameName;
   }
}
