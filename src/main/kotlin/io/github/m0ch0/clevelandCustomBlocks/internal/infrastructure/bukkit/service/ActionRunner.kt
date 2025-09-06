package io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service

import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockAction
import org.bukkit.command.CommandException
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.Throws

@Singleton
internal class ActionRunner @Inject constructor(
    private val plugin: ClevelandCustomBlocks
) {

    @Throws(CommandException::class)
    fun runAll(clicker: Player, actions: List<CustomBlockAction>) {
        for (action in actions) {
            val command = substitute(action.command, clicker)
            val normalized = normalize(command)

            when (action.executeAs) {
                CustomBlockAction.ExecuteAs.PLAYER -> dispatch(clicker, normalized)
                CustomBlockAction.ExecuteAs.SERVER -> dispatch(plugin.server.consoleSender, normalized)
            }
        }
    }

    private fun substitute(command: String, clicker: Player): String {
        // Simple variable expansion for now; can be extended later.
        return command.replace("\$clicker", clicker.name)
    }

    private fun normalize(command: String): String =
        if (command.startsWith("/")) command.substring(1) else command

    private fun dispatch(sender: CommandSender, command: String) {
        if (plugin.server.dispatchCommand(sender, command).not()) {
            throw CommandException("Command not found or wrong usage: '/$command'")
        }
    }
}
