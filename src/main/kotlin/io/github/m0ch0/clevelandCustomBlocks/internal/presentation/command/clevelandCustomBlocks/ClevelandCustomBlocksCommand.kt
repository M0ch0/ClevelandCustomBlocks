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
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.i18n.Msg
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import javax.inject.Inject

@CommandAlias("clevelandcustomblocks|ccbs")
@CommandPermission("clevelandcustomblocks.use")
internal class ClevelandCustomBlocksCommand @Inject constructor(
    private val clevelandCustomBlocksController: ClevelandCustomBlocksController
) : BaseCommand() {

    @Subcommand("reload")
    fun onReload(sender: CommandSender) = clevelandCustomBlocksController.reload(sender)

    @Subcommand("give")
    @Syntax("<player> <itemId> [amount]")
    @CommandCompletion("@players @ccb_ids")
    fun onGive(
        sender: CommandSender,
        @Name("target") target: String,
        @Name("itemId") itemId: String,
        @Name("amount") @Optional @Default("1") amount: Int
    ) {
        if (amount !in 1..2304) {
            sender.sendMessage(Msg.Give.invalidAmount())
            return
        }
        clevelandCustomBlocksController.give(sender, target, itemId, amount)
    }

    @Subcommand("chunk")
    @Syntax("<operation>")
    fun onGetChunkBlocks(
        sender: CommandSender,
        @Name("operation") operation: ChunkOperation
    ) {
        if (sender !is Player) {
            sender.sendMessage(Msg.Command.playerOnlyCommand())
            return
        }

        when (operation) {
            ChunkOperation.GET -> clevelandCustomBlocksController.getChunkBlocks(sender)
            ChunkOperation.CLEANUP -> clevelandCustomBlocksController.cleanupChunkBlocks(sender)
        }
    }
}

enum class ChunkOperation {
    GET,
    CLEANUP
}
