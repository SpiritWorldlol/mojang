package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import jdk.jfr.Configuration;
import jdk.jfr.FlightRecorder;
import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import net.minecraft.SharedConstants;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.PathUtil;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiling.jfr.event.ChunkGenerationEvent;
import net.minecraft.util.profiling.jfr.event.NetworkSummaryEvent;
import net.minecraft.util.profiling.jfr.event.PacketReceivedEvent;
import net.minecraft.util.profiling.jfr.event.PacketSentEvent;
import net.minecraft.util.profiling.jfr.event.ServerTickTimeEvent;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class JfrProfiler implements FlightProfiler {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String MINECRAFT = "Minecraft";
   public static final String WORLD_GENERATION = "World Generation";
   public static final String TICKING = "Ticking";
   public static final String NETWORK = "Network";
   private static final List EVENTS = List.of(ChunkGenerationEvent.class, PacketReceivedEvent.class, PacketSentEvent.class, NetworkSummaryEvent.class, ServerTickTimeEvent.class, WorldLoadFinishedEvent.class);
   private static final String CONFIG_PATH = "/flightrecorder-config.jfc";
   private static final DateTimeFormatter DATE_TIME_FORMAT = (new DateTimeFormatterBuilder()).appendPattern("yyyy-MM-dd-HHmmss").toFormatter().withZone(ZoneId.systemDefault());
   private static final JfrProfiler INSTANCE = new JfrProfiler();
   @Nullable
   Recording currentRecording;
   private float tickTime;
   private final Map summaryRecorderByAddress = new ConcurrentHashMap();

   private JfrProfiler() {
      EVENTS.forEach(FlightRecorder::register);
      FlightRecorder.addPeriodicEvent(ServerTickTimeEvent.class, () -> {
         (new ServerTickTimeEvent(this.tickTime)).commit();
      });
      FlightRecorder.addPeriodicEvent(NetworkSummaryEvent.class, () -> {
         Iterator iterator = this.summaryRecorderByAddress.values().iterator();

         while(iterator.hasNext()) {
            ((NetworkSummaryEvent.Recorder)iterator.next()).commit();
            iterator.remove();
         }

      });
   }

   public static JfrProfiler getInstance() {
      return INSTANCE;
   }

   public boolean start(InstanceType instanceType) {
      URL uRL = JfrProfiler.class.getResource("/flightrecorder-config.jfc");
      if (uRL == null) {
         LOGGER.warn("Could not find default flight recorder config at {}", "/flightrecorder-config.jfc");
         return false;
      } else {
         try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uRL.openStream()));

            boolean var4;
            try {
               var4 = this.start(bufferedReader, instanceType);
            } catch (Throwable var7) {
               try {
                  bufferedReader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            bufferedReader.close();
            return var4;
         } catch (IOException var8) {
            LOGGER.warn("Failed to start flight recorder using configuration at {}", uRL, var8);
            return false;
         }
      }
   }

   public Path stop() {
      if (this.currentRecording == null) {
         throw new IllegalStateException("Not currently profiling");
      } else {
         this.summaryRecorderByAddress.clear();
         Path path = this.currentRecording.getDestination();
         this.currentRecording.stop();
         return path;
      }
   }

   public boolean isProfiling() {
      return this.currentRecording != null;
   }

   public boolean isAvailable() {
      return FlightRecorder.isAvailable();
   }

   private boolean start(Reader reader, InstanceType instanceType) {
      if (this.isProfiling()) {
         LOGGER.warn("Profiling already in progress");
         return false;
      } else {
         try {
            Configuration configuration = Configuration.create(reader);
            String string = DATE_TIME_FORMAT.format(Instant.now());
            this.currentRecording = (Recording)Util.make(new Recording(configuration), (recording) -> {
               List var10000 = EVENTS;
               Objects.requireNonNull(recording);
               var10000.forEach(recording::enable);
               recording.setDumpOnExit(true);
               recording.setToDisk(true);
               recording.setName(String.format(Locale.ROOT, "%s-%s-%s", instanceType.getName(), SharedConstants.getGameVersion().getName(), string));
            });
            Path path = Paths.get(String.format(Locale.ROOT, "debug/%s-%s.jfr", instanceType.getName(), string));
            PathUtil.createDirectories(path.getParent());
            this.currentRecording.setDestination(path);
            this.currentRecording.start();
            this.addListener();
         } catch (ParseException | IOException var6) {
            LOGGER.warn("Failed to start jfr profiling", var6);
            return false;
         }

         LOGGER.info("Started flight recorder profiling id({}):name({}) - will dump to {} on exit or stop command", new Object[]{this.currentRecording.getId(), this.currentRecording.getName(), this.currentRecording.getDestination()});
         return true;
      }
   }

   private void addListener() {
      FlightRecorder.addListener(new FlightRecorderListener() {
         final JfrListener innerListener = new JfrListener(() -> {
            JfrProfiler.this.currentRecording = null;
         });

         public void recordingStateChanged(Recording recording) {
            if (recording == JfrProfiler.this.currentRecording && recording.getState() == RecordingState.STOPPED) {
               this.innerListener.stop(recording.getDestination());
               FlightRecorder.removeListener(this);
            }
         }
      });
   }

   public void onTick(float tickTime) {
      if (ServerTickTimeEvent.TYPE.isEnabled()) {
         this.tickTime = tickTime;
      }

   }

   public void onPacketReceived(int protocolId, int packetId, SocketAddress remoteAddress, int bytes) {
      if (PacketReceivedEvent.TYPE.isEnabled()) {
         (new PacketReceivedEvent(protocolId, packetId, remoteAddress, bytes)).commit();
      }

      if (NetworkSummaryEvent.TYPE.isEnabled()) {
         this.getOrCreateSummaryRecorder(remoteAddress).addReceivedPacket(bytes);
      }

   }

   public void onPacketSent(int protocolId, int packetId, SocketAddress remoteAddress, int bytes) {
      if (PacketSentEvent.TYPE.isEnabled()) {
         (new PacketSentEvent(protocolId, packetId, remoteAddress, bytes)).commit();
      }

      if (NetworkSummaryEvent.TYPE.isEnabled()) {
         this.getOrCreateSummaryRecorder(remoteAddress).addSentPacket(bytes);
      }

   }

   private NetworkSummaryEvent.Recorder getOrCreateSummaryRecorder(SocketAddress address) {
      return (NetworkSummaryEvent.Recorder)this.summaryRecorderByAddress.computeIfAbsent(address.toString(), NetworkSummaryEvent.Recorder::new);
   }

   @Nullable
   public Finishable startWorldLoadProfiling() {
      if (!WorldLoadFinishedEvent.TYPE.isEnabled()) {
         return null;
      } else {
         WorldLoadFinishedEvent worldLoadFinishedEvent = new WorldLoadFinishedEvent();
         worldLoadFinishedEvent.begin();
         Objects.requireNonNull(worldLoadFinishedEvent);
         return worldLoadFinishedEvent::commit;
      }
   }

   @Nullable
   public Finishable startChunkGenerationProfiling(ChunkPos chunkPos, RegistryKey world, String targetStatus) {
      if (!ChunkGenerationEvent.TYPE.isEnabled()) {
         return null;
      } else {
         ChunkGenerationEvent chunkGenerationEvent = new ChunkGenerationEvent(chunkPos, world, targetStatus);
         chunkGenerationEvent.begin();
         Objects.requireNonNull(chunkGenerationEvent);
         return chunkGenerationEvent::commit;
      }
   }
}
