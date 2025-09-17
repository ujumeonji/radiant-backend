package ink.radiant.command.service

import ink.radiant.command.command.Command
import ink.radiant.command.handler.CommandHandler
import org.springframework.stereotype.Service

@Service
class CommandBus(
    private val handlers: List<CommandHandler<*>>,
) {
    private val handlerMap: Map<Class<*>, CommandHandler<*>> =
        handlers.associateBy { it.getCommandType() }

    @Suppress("UNCHECKED_CAST")
    fun <T : Command> send(command: T) {
        val handler = handlerMap[command::class.java] as? CommandHandler<T>
            ?: throw IllegalArgumentException("No handler found for command: ${command::class.java.simpleName}")

        handler.handle(command)
    }
}
