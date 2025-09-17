package ink.radiant.command.command

import java.util.UUID

interface Command {
    val commandId: UUID

    val aggregateId: String
}
