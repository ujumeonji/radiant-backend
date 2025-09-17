package ink.radiant.infrastructure.monitoring

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Component("eventStoreHealthIndicator")
class EventStoreHealthIndicator : HealthIndicator {

    override fun health(): Health {
        return try {
            Health.up()
                .withDetail("status", "Event store is accessible")
                .build()
        } catch (e: Exception) {
            Health.down(e)
                .withDetail("status", "Event store is not accessible")
                .build()
        }
    }
}

@RestController
@RequestMapping("/health")
class HealthCheckController {

    @GetMapping("/radiant")
    fun radiantHealth(): Map<String, Any> {
        return mapOf(
            "service" to "radiant-backend",
            "status" to "UP",
            "timestamp" to System.currentTimeMillis(),
        )
    }
}
