package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.command.clevelandCustomBlocks

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.GetCustomBlockDefinitionByIdUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.LoadCustomBlockDefinitionsUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.ChunkIndexStore
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.i18n.Msg
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import javax.inject.Inject

internal class ClevelandCustomBlocksController @Inject constructor(
    private val plugin: ClevelandCustomBlocks,
    private val logger: ComponentLogger,
    private val customBlocksService: ClevelandCustomBlocksService,
    private val chunkIndexStore: ChunkIndexStore,
    private val loadCustomBlockDefinitionsUseCase: LoadCustomBlockDefinitionsUseCase,
    private val getCustomBlockDefinitionsUseCase: GetCustomBlockDefinitionByIdUseCase
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

    @Suppress("ReturnCount")
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

        plugin.launch {
            if (getCustomBlockDefinitionsUseCase(itemId) !is GetCustomBlockDefinitionByIdUseCase.Result.Success) {
                sender.sendMessage(Msg.Give.definitionNotFound(itemId))
                return@launch
            }

            // Someday createBaseItem will have a Result

            val baseItem = customBlocksService.createBaseItem(itemId)
                ?: return@launch sender.sendMessage(Msg.Give.invalidDefinition(itemId))

            val maxPerStack = minOf(64, baseItem.maxStackSize)

            val fullStacks = amount / maxPerStack
            val remainder = amount % maxPerStack

            val items = List(fullStacks) { baseItem.asQuantity(maxPerStack) } +
                    listOfNotNull(remainder.takeIf { it > 0 }?.let(baseItem::asQuantity))

            when (items.size) {
                1 -> targetPlayer.give(items[0])
                else -> targetPlayer.give(items)
            }

            sender.sendMessage(Msg.Give.gave(target, itemId, amount))
        }
    }

    fun getChunkBlocks(player: Player) {
        val chunk = player.chunk
        val blockRegistry = chunkIndexStore.list(chunk)
        if (blockRegistry.isEmpty()) {
            player.sendMessage(Msg.Chunk.emptyRegistry())
            return
        }

        var validBlockCount = 0
        var invalidBlockCount = 0

        for (registeredBlock in blockRegistry) {
            val block = chunk.getBlock(
                registeredBlock.x,
                registeredBlock.y,
                registeredBlock.z
            )

            if (block.type == CollisionBlock.material) {
                validBlockCount += 1
            } else {
                invalidBlockCount += 1
            }
        }

        player.sendMessage(Msg.Chunk.validSummary(validBlockCount))
        player.sendMessage(Msg.Chunk.invalidSummary(invalidBlockCount))
    }

    fun cleanupChunkBlocks(player: Player) {
        /*
        val chunk = player.chunk
        val itemDisplays = chunk.entities.filterIsInstance<>().filter(it.persistentDataContainer.has(~~~))
        val registeredBlocks = chunkIndexStore.~~~
         */
        player.sendMessage("TODO_NOT_IMPLEMENTED_YET")
    }
}
