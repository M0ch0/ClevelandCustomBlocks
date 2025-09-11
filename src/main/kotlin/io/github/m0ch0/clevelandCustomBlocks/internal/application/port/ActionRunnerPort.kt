package io.github.m0ch0.clevelandCustomBlocks.internal.application.port

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockAction
import org.bukkit.block.Block
import org.bukkit.command.CommandException
import org.bukkit.entity.Player

internal interface ActionRunnerPort {
    @Throws(CommandException::class)
    fun runAll(clicker: Player, clickedBlock: Block, actions: List<CustomBlockAction>)
}
