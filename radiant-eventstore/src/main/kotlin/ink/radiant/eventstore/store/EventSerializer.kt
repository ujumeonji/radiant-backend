package ink.radiant.eventstore.store

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import ink.radiant.core.common.exception.EventStoreException
import ink.radiant.core.domain.event.DomainEvent
import ink.radiant.core.domain.event.EventMetadata
import org.springframework.stereotype.Component

@Component
class EventSerializer {
    private val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
    }

    fun serialize(event: DomainEvent): String {
        return try {
            objectMapper.writeValueAsString(event)
        } catch (e: Exception) {
            throw EventStoreException("Failed to serialize event: ${event.eventType}", e)
        }
    }

    fun serializeMetadata(metadata: EventMetadata): String {
        return try {
            objectMapper.writeValueAsString(metadata)
        } catch (e: Exception) {
            throw EventStoreException("Failed to serialize metadata", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : DomainEvent> deserialize(eventData: String, eventType: String): T {
        return try {
            val clazz = Class.forName(eventType) as Class<T>
            objectMapper.readValue(eventData, clazz)
        } catch (e: ClassNotFoundException) {
            throw EventStoreException("Unknown event type: $eventType", e)
        } catch (e: Exception) {
            throw EventStoreException("Failed to deserialize event: $eventType", e)
        }
    }

    fun deserializeMetadata(metadataJson: String): EventMetadata {
        return try {
            objectMapper.readValue(metadataJson, EventMetadata::class.java)
        } catch (e: Exception) {
            throw EventStoreException("Failed to deserialize metadata", e)
        }
    }
}
