package ink.radiant.command.handler

import ink.radiant.command.command.Command

interface CommandHandler<T : Command> {
    fun handle(command: T)

    fun getCommandType(): Class<T>
}
