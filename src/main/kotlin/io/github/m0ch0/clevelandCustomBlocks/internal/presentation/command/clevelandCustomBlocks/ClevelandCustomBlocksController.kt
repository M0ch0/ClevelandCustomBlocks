package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.command.clevelandCustomBlocks

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.CreateBaseCustomItemUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.ForceRemoveCustomBlockAtUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.ListRegisteredPositionsUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.LoadCustomBlockDefinitionsUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.i18n.Msg
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

internal class ClevelandCustomBlocksController @Inject constructor(
    private val plugin: ClevelandCustomBlocks,
    private val logger: ComponentLogger,
    private val loadCustomBlockDefinitionsUseCase: LoadCustomBlockDefinitionsUseCase,
    private val getBaseCustomItemUseCase: CreateBaseCustomItemUseCase,
    private val listRegisteredPositionsUseCase: ListRegisteredPositionsUseCase,
    private val forceRemoveCustomBlockAtUseCase: ForceRemoveCustomBlockAtUseCase
) {

    fun reload(sender: CommandSender) {
        plugin.launch {
            sender.sendMessage(
                Msg.Reload.starting()
            )
            val result = loadCustomBlockDefinitionsUseCase()
            when (result) {
                is LoadCustomBlockDefinitionsUseCase.Result.Success -> {
                    if (result.warnings.isNotEmpty()) {
                        logger.info("${sender.name} reloads config with warnings, ${result.warnings.size}")
                        sender.sendMessage(
                            Msg.Reload.resultWithWarn(
                                result.changed,
                                result.warnings.size
                            )
                        )
                    } else {
                        sender.sendMessage(
                            Msg.Reload.result(
                                result.changed
                            )
                        )
                    }
                }

                is LoadCustomBlockDefinitionsUseCase.Result.Failure.Unknown -> {
                    logger.error("Runtime error when loading define.yml: ${result.cause}")
                    sender.sendMessage(
                        Msg.Common.runtimeError()
                    )
                }
            }
        }
    }

    fun give(sender: CommandSender, target: String, itemId: String, amount: Int) {
        if (target.startsWith("@")) {
            val matches = org.bukkit.Bukkit.selectEntities(sender, target).filterIsInstance<Player>()
            if (matches.isEmpty()) {
                sender.sendMessage(Msg.Common.playerNotFound(target))
                return
            }
            matches.forEach { player -> give(sender, player.name, itemId, amount) }
            return
        }

        val targetPlayer = plugin.server.getPlayer(target) ?: return run {
            sender.sendMessage(Msg.Common.playerNotFound(target))
        }

        val baseItem: ItemStack = when (val result = getBaseCustomItemUseCase(itemId)) {
            CreateBaseCustomItemUseCase.Result.Failure.DefinitionNotFound
                -> return targetPlayer.sendMessage(Msg.Give.definitionNotFound(itemId))
            CreateBaseCustomItemUseCase.Result.Failure.InvalidMaterial
                -> return targetPlayer.sendMessage(Msg.Give.invalidMaterial(itemId))
            is CreateBaseCustomItemUseCase.Result.Success -> result.item
        }

        val maxPerStack = baseItem.maxStackSize

        val fullStacks = amount / maxPerStack
        val remainder = amount % maxPerStack

        val items = List(fullStacks) { baseItem.asQuantity(maxPerStack) } +
                listOfNotNull(remainder.takeIf { it > 0 }?.let(baseItem::asQuantity))

        when (items.size) {
            1 -> targetPlayer.give(items[0])
            else -> targetPlayer.give(items)
        } // Thanks latest paper api!
        sender.sendMessage(Msg.Give.gave(target, itemId, amount))
    }

    fun getChunkBlocks(player: Player) {
        val chunk = player.chunk
        val locations = listRegisteredPositionsUseCase(chunk)
        if (locations.isEmpty()) {
            player.sendMessage(Msg.Chunk.emptyRegistry())
            return
        }

        var valid = 0
        var invalid = 0
        for (loc in locations) {
            val block = loc.block
            if (block.type == CollisionBlock.material) valid++ else invalid++
        }
        player.sendMessage(Msg.Chunk.validSummary(valid))
        player.sendMessage(Msg.Chunk.invalidSummary(invalid))
    }

    fun cleanupChunkBlocks(player: Player) {
        val chunk = player.chunk
        val locations = listRegisteredPositionsUseCase(chunk)
        for (loc in locations) forceRemoveCustomBlockAtUseCase(loc.block)
        player.sendMessage(Msg.Chunk.cleanup(locations.size))
    }
}
