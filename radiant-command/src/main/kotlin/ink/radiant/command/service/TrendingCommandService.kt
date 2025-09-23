package ink.radiant.command.service

import ink.radiant.core.domain.event.PostViewedEvent

interface TrendingCommandService {
    fun handlePostViewed(event: PostViewedEvent)
}
