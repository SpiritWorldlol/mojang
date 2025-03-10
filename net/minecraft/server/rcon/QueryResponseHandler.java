package net.minecraft.server.rcon;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class QueryResponseHandler extends RconBase {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String GAME_TYPE = "SMP";
   private static final String GAME_ID = "MINECRAFT";
   private static final long CLEAN_UP_THRESHOLD = 30000L;
   private static final long field_29798 = 5000L;
   private long lastQueryTime;
   private final int queryPort;
   private final int port;
   private final int maxPlayerCount;
   private final String motd;
   private final String levelName;
   private DatagramSocket socket;
   private final byte[] packetBuffer = new byte[1460];
   private String ip;
   private String hostname;
   private final Map queries;
   private final DataStreamHelper data;
   private long lastResponseTime;
   private final DedicatedServer server;

   private QueryResponseHandler(DedicatedServer server, int queryPort) {
      super("Query Listener");
      this.server = server;
      this.queryPort = queryPort;
      this.hostname = server.getHostname();
      this.port = server.getPort();
      this.motd = server.getMotd();
      this.maxPlayerCount = server.getMaxPlayerCount();
      this.levelName = server.getLevelName();
      this.lastResponseTime = 0L;
      this.ip = "0.0.0.0";
      if (!this.hostname.isEmpty() && !this.ip.equals(this.hostname)) {
         this.ip = this.hostname;
      } else {
         this.hostname = "0.0.0.0";

         try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            this.ip = inetAddress.getHostAddress();
         } catch (UnknownHostException var4) {
            LOGGER.warn("Unable to determine local host IP, please set server-ip in server.properties", var4);
         }
      }

      this.data = new DataStreamHelper(1460);
      this.queries = Maps.newHashMap();
   }

   @Nullable
   public static QueryResponseHandler create(DedicatedServer server) {
      int i = server.getProperties().queryPort;
      if (0 < i && 65535 >= i) {
         QueryResponseHandler lv = new QueryResponseHandler(server, i);
         return !lv.start() ? null : lv;
      } else {
         LOGGER.warn("Invalid query port {} found in server.properties (queries disabled)", i);
         return null;
      }
   }

   private void reply(byte[] buf, DatagramPacket packet) throws IOException {
      this.socket.send(new DatagramPacket(buf, buf.length, packet.getSocketAddress()));
   }

   private boolean handle(DatagramPacket packet) throws IOException {
      byte[] bs = packet.getData();
      int i = packet.getLength();
      SocketAddress socketAddress = packet.getSocketAddress();
      LOGGER.debug("Packet len {} [{}]", i, socketAddress);
      if (3 <= i && -2 == bs[0] && -3 == bs[1]) {
         LOGGER.debug("Packet '{}' [{}]", BufferHelper.toHex(bs[2]), socketAddress);
         switch (bs[2]) {
            case 0:
               if (!this.isValidQuery(packet)) {
                  LOGGER.debug("Invalid challenge [{}]", socketAddress);
                  return false;
               } else if (15 == i) {
                  this.reply(this.createRulesReply(packet), packet);
                  LOGGER.debug("Rules [{}]", socketAddress);
               } else {
                  DataStreamHelper lv = new DataStreamHelper(1460);
                  lv.write(0);
                  lv.write(this.getMessageBytes(packet.getSocketAddress()));
                  lv.writeBytes(this.motd);
                  lv.writeBytes("SMP");
                  lv.writeBytes(this.levelName);
                  lv.writeBytes(Integer.toString(this.server.getCurrentPlayerCount()));
                  lv.writeBytes(Integer.toString(this.maxPlayerCount));
                  lv.writeShort((short)this.port);
                  lv.writeBytes(this.ip);
                  this.reply(lv.bytes(), packet);
                  LOGGER.debug("Status [{}]", socketAddress);
               }
            default:
               return true;
            case 9:
               this.createQuery(packet);
               LOGGER.debug("Challenge [{}]", socketAddress);
               return true;
         }
      } else {
         LOGGER.debug("Invalid packet [{}]", socketAddress);
         return false;
      }
   }

   private byte[] createRulesReply(DatagramPacket packet) throws IOException {
      long l = Util.getMeasuringTimeMs();
      if (l < this.lastResponseTime + 5000L) {
         byte[] bs = this.data.bytes();
         byte[] cs = this.getMessageBytes(packet.getSocketAddress());
         bs[1] = cs[0];
         bs[2] = cs[1];
         bs[3] = cs[2];
         bs[4] = cs[3];
         return bs;
      } else {
         this.lastResponseTime = l;
         this.data.reset();
         this.data.write(0);
         this.data.write(this.getMessageBytes(packet.getSocketAddress()));
         this.data.writeBytes("splitnum");
         this.data.write(128);
         this.data.write(0);
         this.data.writeBytes("hostname");
         this.data.writeBytes(this.motd);
         this.data.writeBytes("gametype");
         this.data.writeBytes("SMP");
         this.data.writeBytes("game_id");
         this.data.writeBytes("MINECRAFT");
         this.data.writeBytes("version");
         this.data.writeBytes(this.server.getVersion());
         this.data.writeBytes("plugins");
         this.data.writeBytes(this.server.getPlugins());
         this.data.writeBytes("map");
         this.data.writeBytes(this.levelName);
         this.data.writeBytes("numplayers");
         this.data.writeBytes("" + this.server.getCurrentPlayerCount());
         this.data.writeBytes("maxplayers");
         this.data.writeBytes("" + this.maxPlayerCount);
         this.data.writeBytes("hostport");
         this.data.writeBytes("" + this.port);
         this.data.writeBytes("hostip");
         this.data.writeBytes(this.ip);
         this.data.write(0);
         this.data.write(1);
         this.data.writeBytes("player_");
         this.data.write(0);
         String[] strings = this.server.getPlayerNames();
         String[] var5 = strings;
         int var6 = strings.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String string = var5[var7];
            this.data.writeBytes(string);
         }

         this.data.write(0);
         return this.data.bytes();
      }
   }

   private byte[] getMessageBytes(SocketAddress address) {
      return ((Query)this.queries.get(address)).getMessageBytes();
   }

   private Boolean isValidQuery(DatagramPacket packet) {
      SocketAddress socketAddress = packet.getSocketAddress();
      if (!this.queries.containsKey(socketAddress)) {
         return false;
      } else {
         byte[] bs = packet.getData();
         return ((Query)this.queries.get(socketAddress)).getId() == BufferHelper.getIntBE(bs, 7, packet.getLength());
      }
   }

   private void createQuery(DatagramPacket packet) throws IOException {
      Query lv = new Query(packet);
      this.queries.put(packet.getSocketAddress(), lv);
      this.reply(lv.getReplyBuf(), packet);
   }

   private void cleanUp() {
      if (this.running) {
         long l = Util.getMeasuringTimeMs();
         if (l >= this.lastQueryTime + 30000L) {
            this.lastQueryTime = l;
            this.queries.values().removeIf((query) -> {
               return query.startedBefore(l);
            });
         }
      }
   }

   public void run() {
      LOGGER.info("Query running on {}:{}", this.hostname, this.queryPort);
      this.lastQueryTime = Util.getMeasuringTimeMs();
      DatagramPacket datagramPacket = new DatagramPacket(this.packetBuffer, this.packetBuffer.length);

      try {
         while(this.running) {
            try {
               this.socket.receive(datagramPacket);
               this.cleanUp();
               this.handle(datagramPacket);
            } catch (SocketTimeoutException var8) {
               this.cleanUp();
            } catch (PortUnreachableException var9) {
            } catch (IOException var10) {
               this.handleIoException(var10);
            }
         }
      } finally {
         LOGGER.debug("closeSocket: {}:{}", this.hostname, this.queryPort);
         this.socket.close();
      }

   }

   public boolean start() {
      if (this.running) {
         return true;
      } else {
         return !this.initialize() ? false : super.start();
      }
   }

   private void handleIoException(Exception e) {
      if (this.running) {
         LOGGER.warn("Unexpected exception", e);
         if (!this.initialize()) {
            LOGGER.error("Failed to recover from exception, shutting down!");
            this.running = false;
         }

      }
   }

   private boolean initialize() {
      try {
         this.socket = new DatagramSocket(this.queryPort, InetAddress.getByName(this.hostname));
         this.socket.setSoTimeout(500);
         return true;
      } catch (Exception var2) {
         LOGGER.warn("Unable to initialise query system on {}:{}", new Object[]{this.hostname, this.queryPort, var2});
         return false;
      }
   }

   private static class Query {
      private final long startTime = (new Date()).getTime();
      private final int id;
      private final byte[] messageBytes;
      private final byte[] replyBuf;
      private final String message;

      public Query(DatagramPacket packet) {
         byte[] bs = packet.getData();
         this.messageBytes = new byte[4];
         this.messageBytes[0] = bs[3];
         this.messageBytes[1] = bs[4];
         this.messageBytes[2] = bs[5];
         this.messageBytes[3] = bs[6];
         this.message = new String(this.messageBytes, StandardCharsets.UTF_8);
         this.id = Random.create().nextInt(16777216);
         this.replyBuf = String.format(Locale.ROOT, "\t%s%d\u0000", this.message, this.id).getBytes(StandardCharsets.UTF_8);
      }

      public Boolean startedBefore(long lastQueryTime) {
         return this.startTime < lastQueryTime;
      }

      public int getId() {
         return this.id;
      }

      public byte[] getReplyBuf() {
         return this.replyBuf;
      }

      public byte[] getMessageBytes() {
         return this.messageBytes;
      }

      public String getMessage() {
         return this.message;
      }
   }
}
