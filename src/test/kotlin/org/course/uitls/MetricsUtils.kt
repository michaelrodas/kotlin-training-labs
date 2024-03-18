package org.course.uitls

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.distribution.HistogramSnapshot
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import io.micrometer.core.instrument.logging.LoggingRegistryConfig
import io.micrometer.core.instrument.util.DoubleFormat
import io.micrometer.core.instrument.util.TimeUtils
import org.course.utils.logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.ln
import kotlin.math.pow


//Micrometer utils

fun loggingMeterRegistry(): LoggingMeterRegistry {
    return LoggingMeterRegistry(object : LoggingRegistryConfig {
        override fun step(): Duration {
            return Duration.ofSeconds(1)
        }

        override fun get(key: String): String? {
            return null
        }
    }, Clock.SYSTEM){it}//prevents logging, intended since we want to log via printMetrics()
}

fun MeterRegistry.createTimer(name:String) = Timer
    .builder(name)
    .publishPercentiles(0.5, 0.9, 0.95, 0.99)
    .register(this)


val PERF_LOGGER = LoggerFactory.getLogger("perftest")

fun MeterRegistry.printMetrics() {
    this.meters.filterIsInstance<Timer>().forEach { timer ->
        val snapshot: HistogramSnapshot = timer.takeSnapshot()
        val count = snapshot.count()
        if (count != 0L) {
            val msg = timer.id.name +
                    " throughput=" + unitlessRate(count.toDouble()) +
                    " mean=" + time(snapshot.mean(TimeUnit.MILLISECONDS)) +
                    " max=" + time(snapshot.max(TimeUnit.MILLISECONDS)) +
                    " percentiles=${snapshot.percentileValues().joinToString()}"
            PERF_LOGGER.info(msg)
            logger.info(msg)
        }

    }
}


private fun time(time: Double): String = format(
    Duration.ofNanos(
        TimeUtils.convert(time, TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS).toLong()))


private fun humanReadableByteCount(bytes: Double): String {
    val unit = 1024
    if (!(bytes < unit.toDouble()) && !java.lang.Double.isNaN(bytes)) {
        val exp = (ln(bytes) / ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString() + "i"
        return DoubleFormat.decimalOrNan(bytes / unit.toDouble().pow(exp.toDouble())) + " " + pre + "B"
    } else {
        return DoubleFormat.decimalOrNan(bytes) + " B"
    }
}

private fun unitlessRate(rate: Double): String = DoubleFormat.decimalOrNan(rate / 1) + "/s"


private fun format(duration: Duration): String {
    val totalSeconds = (duration.toMillis() / 1000L).toInt()
    val seconds = totalSeconds % 60
    val totalMinutes = totalSeconds / 60
    val minutes = totalMinutes % 60
    val hours = totalMinutes / 60
    val sb = StringBuilder()
    if (hours > 0) {
        sb.append(hours)
        sb.append('h')
    }

    if (minutes > 0) {
        if (sb.length > 0) {
            sb.append(' ')
        }

        sb.append(minutes)
        sb.append('m')
    }

    val nanos = duration.nano
    if (seconds > 0 || nanos > 0) {
        if (sb.length > 0) {
            sb.append(' ')
        }

        sb.append(seconds)
        if (nanos > 0) {
            sb.append('.')
            sb.append(String.format("%04d", nanos / 100000))
        }

        sb.append('s')
    }

    return sb.toString()
}