package ink.radiant.infrastructure.monitoring

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

@Component
class EventMetrics(
    private val meterRegistry: MeterRegistry,
) {
    private val eventPublishedCounter: Counter = Counter.builder("radiant.events.published")
        .description("Number of events published")
        .register(meterRegistry)

    private val eventProcessedCounter: Counter = Counter.builder("radiant.events.processed")
        .description("Number of events processed")
        .register(meterRegistry)

    private val eventProcessingTimer: Timer = Timer.builder("radiant.events.processing.time")
        .description("Time taken to process events")
        .register(meterRegistry)

    fun incrementEventPublished(eventType: String) {
        Counter.builder("radiant.events.published")
            .tag("event.type", eventType)
            .register(meterRegistry)
            .increment()
    }

    fun incrementEventProcessed(eventType: String) {
        Counter.builder("radiant.events.processed")
            .tag("event.type", eventType)
            .register(meterRegistry)
            .increment()
    }

    fun recordEventProcessingTime(eventType: String, duration: Long) {
        Timer.builder("radiant.events.processing.time")
            .tag("event.type", eventType)
            .register(meterRegistry)
            .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    fun startEventProcessingTimer(): Timer.Sample {
        return Timer.start(meterRegistry)
    }
}
