package ink.radiant.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

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

    @Bean(name = ["translationTaskExecutor"])
    fun translationTaskExecutor(
        @Value("\${radiant.translation.executor.core-pool-size:4}") corePoolSize: Int,
        @Value("\${radiant.translation.executor.max-pool-size:8}") maxPoolSize: Int,
        @Value("\${radiant.translation.executor.queue-capacity:100}") queueCapacity: Int,
        @Value("\${radiant.translation.executor.keep-alive-seconds:60}") keepAliveSeconds: Int,
        @Value("\${radiant.translation.executor.await-termination-seconds:30}") awaitTerminationSeconds: Int,
    ): Executor = ThreadPoolTaskExecutor().apply {
        this.corePoolSize = corePoolSize
        this.maxPoolSize = maxPoolSize
        this.queueCapacity = queueCapacity
        this.keepAliveSeconds = keepAliveSeconds
        this.setThreadNamePrefix("translation-exec-")
        this.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        this.setWaitForTasksToCompleteOnShutdown(true)
        this.setAwaitTerminationSeconds(awaitTerminationSeconds)
        this.initialize()
    }
}
