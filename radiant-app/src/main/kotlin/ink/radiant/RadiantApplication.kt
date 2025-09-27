package ink.radiant

import ink.radiant.infrastructure.config.AsyncConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Import

@SpringBootApplication(
    scanBasePackages = [
        "ink.radiant",
        "ink.radiant.core",
        "ink.radiant.eventstore",
        "ink.radiant.command",
        "ink.radiant.query",
        "ink.radiant.infrastructure",
        "ink.radiant.web",
    ],
)
@EnableCaching
@Import(AsyncConfig::class)
class RadiantApplication

fun main(args: Array<String>) {
    runApplication<RadiantApplication>(*args)
}
