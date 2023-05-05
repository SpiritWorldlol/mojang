package net.minecraft.client.util.telemetry;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class TimeRecorder {
   public static final TimeRecorder INSTANCE = new TimeRecorder(Ticker.systemTicker());
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Ticker ticker;
   private final Map steps = new HashMap();
   private OptionalLong bootstrapLoadTime = OptionalLong.empty();

   protected TimeRecorder(Ticker ticker) {
      this.ticker = ticker;
   }

   public synchronized void start(TelemetryEventProperty property) {
      this.addStep(property, (prop) -> {
         return Stopwatch.createStarted(this.ticker);
      });
   }

   public synchronized void start(TelemetryEventProperty property, Stopwatch stopWatch) {
      this.addStep(property, (prop) -> {
         return stopWatch;
      });
   }

   private synchronized void addStep(TelemetryEventProperty property, Function factory) {
      this.steps.computeIfAbsent(property, factory);
   }

   public synchronized void end(TelemetryEventProperty property) {
      Stopwatch stopwatch = (Stopwatch)this.steps.get(property);
      if (stopwatch == null) {
         LOGGER.warn("Attempted to end step for {} before starting it", property.id());
      } else {
         if (stopwatch.isRunning()) {
            stopwatch.stop();
         }

      }
   }

   public void collect(TelemetrySender sender) {
      sender.send(TelemetryEventType.GAME_LOAD_TIMES, (builder) -> {
         synchronized(this) {
            this.steps.forEach((property, stopwatch) -> {
               if (!stopwatch.isRunning()) {
                  long l = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                  builder.put(property, new Measurement((int)l));
               } else {
                  LOGGER.warn("Measurement {} was discarded since it was still ongoing when the event {} was sent.", property.id(), TelemetryEventType.GAME_LOAD_TIMES.getId());
               }

            });
            this.bootstrapLoadTime.ifPresent((value) -> {
               builder.put(TelemetryEventProperty.LOAD_TIME_BOOTSTRAP_MS, new Measurement((int)value));
            });
            this.steps.clear();
         }
      });
   }

   public synchronized void setBootstrapLoadTime(long time) {
      this.bootstrapLoadTime = OptionalLong.of(time);
   }

   @Environment(EnvType.CLIENT)
   public static record Measurement(int millis) {
      public static final Codec CODEC;

      public Measurement(int i) {
         this.millis = i;
      }

      public int millis() {
         return this.millis;
      }

      static {
         CODEC = Codec.INT.xmap(Measurement::new, (measurement) -> {
            return measurement.millis;
         });
      }
   }
}
