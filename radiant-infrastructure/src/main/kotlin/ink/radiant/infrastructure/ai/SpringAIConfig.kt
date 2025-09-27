package ink.radiant.infrastructure.ai

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringAIConfig {

    @Bean
    fun translationChatClient(openAiChatModel: OpenAiChatModel): ChatModel = openAiChatModel
}
