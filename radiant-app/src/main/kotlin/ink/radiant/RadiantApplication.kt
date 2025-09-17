package ink.radiant

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

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
class RadiantApplication

fun main(args: Array<String>) {
    runApplication<RadiantApplication>(*args)
}
