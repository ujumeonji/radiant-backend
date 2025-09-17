package ink.radiant.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {

    @Bean("eventProcessingExecutor")
    fun eventProcessingExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("EventProcessing-")
        executor.initialize()
        return executor
    }

    @Bean("projectionUpdateExecutor")
    fun projectionUpdateExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 3
        executor.maxPoolSize = 6
        executor.queueCapacity = 50
        executor.setThreadNamePrefix("ProjectionUpdate-")
        executor.initialize()
        return executor
    }
}
