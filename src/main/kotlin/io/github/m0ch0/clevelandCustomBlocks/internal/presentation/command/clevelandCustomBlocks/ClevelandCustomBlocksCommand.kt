package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.command.clevelandCustomBlocks

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Name
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import javax.inject.Inject

@CommandAlias("clevelandcustomblocks|clevecb")
@CommandPermission("clevelandcustomblocks.use")
internal class ClevelandCustomBlocksCommand @Inject constructor(
    private val clevelandCustomBlocksController: ClevelandCustomBlocksController
) : BaseCommand() {

    @Subcommand("reload")
    fun onReload(sender: CommandSender) = clevelandCustomBlocksController.reload(sender)

    @Subcommand("give")
    @Syntax("<player> <itemId> [amount]")
    @CommandCompletion("@players")
    fun onGive(
        sender: CommandSender,
        @Name("target") target: String,
        @Name("itemId") itemId: String,
        @Name("amount") @Optional @Default("1") amount: Int
    ) {
        clevelandCustomBlocksController.give(sender, target, itemId, amount)
    }

    @Subcommand("get-chunk-blocks")
    fun onGetChunkBlocks(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage("TODO_ONLY_PLAYER_CAN_RUN_COMMAND")
        return
        }
        clevelandCustomBlocksController.getChunkBlocks(sender)
    }
}
