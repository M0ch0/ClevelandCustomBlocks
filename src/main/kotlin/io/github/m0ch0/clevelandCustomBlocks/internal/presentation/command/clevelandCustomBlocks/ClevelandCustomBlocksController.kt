package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.command.clevelandCustomBlocks

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockDefinition
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.GetCustomBlockDefinitionByIdUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.LoadCustomBlockDefinitionsUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.ChunkIndexStore
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.i18n.Msg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import javax.inject.Inject
import javax.inject.Named

internal class ClevelandCustomBlocksController @Inject constructor(
    private val plugin: ClevelandCustomBlocks,
    private val logger: ComponentLogger,
    private val chunkIndexStore: ChunkIndexStore,
    @Named("custom_block_id_key") private val customBlockIdKey: NamespacedKey,
    private val loadCustomBlockDefinitionsUseCase: LoadCustomBlockDefinitionsUseCase,
    private val getCustomBlockDefinitionByIdUseCase: GetCustomBlockDefinitionByIdUseCase
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

    @Suppress("UnstableApiUsage")
    fun give(sender: CommandSender, target: String, itemId: String, amount: Int) {
        val targetPlayer = plugin.server.getPlayer(target) ?: return run {
            sender.sendMessage(Msg.Common.playerNotFound(target))
        }

        plugin.launch {
            val customBlockDefinition: CustomBlockDefinition = run {
                val result = getCustomBlockDefinitionByIdUseCase(itemId)
                when (result) {
                    is GetCustomBlockDefinitionByIdUseCase.Result.Success -> {
                        result.customBlock
                    }

                    is GetCustomBlockDefinitionByIdUseCase.Result.Failure.NotFound -> {
                        sender.sendMessage(Msg.Give.definitionNotFound(itemId))
                        return@launch
                    }
                }
            }

            val originalMaterial = Material.getMaterial(customBlockDefinition.originalBlock) ?: run {
                sender.sendMessage(Msg.Give.invalidDefinition(itemId)) /* Since the Repository also calls `getMaterial`,
                this message should never be fired except when a bit is flipped by cosmic ray */
                return@launch
            }

            val givenItem = ItemStack(originalMaterial, amount).also {
                it.editMeta { itemMeta ->

                    itemMeta.itemName(Component.text(customBlockDefinition.displayName))

                    val customModelDataComponent = itemMeta.customModelDataComponent
                    customModelDataComponent.strings = listOf(customBlockDefinition.id)
                    itemMeta.setCustomModelDataComponent(customModelDataComponent)

                    itemMeta.persistentDataContainer.set(
                        customBlockIdKey,
                        PersistentDataType.STRING,
                        customBlockDefinition.id
                    )
                }
            }

            targetPlayer.give(givenItem)
            targetPlayer.sendMessage(Msg.Give.gave(target, itemId, amount))
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
}
