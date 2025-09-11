package io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.adapter

import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.ActionRunnerPort
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockAction
import org.bukkit.block.Block
import org.bukkit.command.CommandException
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ActionRunnerAdapter @Inject constructor(
    private val plugin: ClevelandCustomBlocks
) : ActionRunnerPort {

    @Throws(CommandException::class)
    override fun runAll(clicker: Player, clickedBlock: Block, actions: List<CustomBlockAction>) {
        for (action in actions) {
            val command = substitute(action.command, clicker, clickedBlock)
            val normalized = normalize(command)

            when (action.executeAs) {
                CustomBlockAction.ExecuteAs.PLAYER -> dispatch(clicker, normalized)
                CustomBlockAction.ExecuteAs.SERVER -> dispatch(plugin.server.consoleSender, normalized)
            }
        }
    }

    private fun substitute(command: String, clicker: Player, clickedBlock: Block): String {
        return command
            .replace("\$clicker", clicker.name)
            .replace("\$x", clickedBlock.x.toString())
            .replace("\$y", clickedBlock.y.toString())
            .replace("\$z", clickedBlock.z.toString())
    }

    private fun normalize(command: String): String =
        if (command.startsWith("/")) command.substring(1) else command

    private fun dispatch(sender: CommandSender, command: String) {
        if (plugin.server.dispatchCommand(sender, command).not()) {
            throw CommandException("Command not found or wrong usage: '/$command'")
        }
    }
}
