package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.player

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.GetCustomBlockDefinitionByIdUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.ActionRunner
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.i18n.Msg
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.command.CommandException
import org.bukkit.entity.Player
import javax.inject.Inject

internal class PlayerController @Inject constructor(
    private val plugin: ClevelandCustomBlocks,
    private val logger: ComponentLogger,
    private val customBlocksService: ClevelandCustomBlocksService,
    private val actionRunner: ActionRunner,
    private val getCustomBlockDefinitionByIdUseCase: GetCustomBlockDefinitionByIdUseCase
) {

    fun onBarrierLeftClick(player: Player, clickedBlock: Block) {
        if (player.gameMode == GameMode.CREATIVE) {
            customBlocksService.removeAt(clickedBlock, false)
        } else {
            customBlocksService.removeAt(clickedBlock, true)
        }
    }

    fun onBarrierRightClick(player: Player, clickedBlock: Block) {
        val display = customBlocksService.linkedDisplayOf(clickedBlock) ?: return
        val id = customBlocksService.customIdOf(display.itemStack) ?: return

        plugin.launch {
            when (val result = getCustomBlockDefinitionByIdUseCase(id)) {
                is GetCustomBlockDefinitionByIdUseCase.Result.Success -> {
                    val actions = result.customBlock.actions
                    if (actions.isNotEmpty()) {
                        try {
                            actionRunner.runAll(player, actions)
                        } catch (exception: CommandException) {
                            logger.error(exception.toString())
                            player.sendMessage(Msg.Action.invalidCommand())
                            if (player.isOp) player.sendMessage(Msg.Action.invalidCommandForOp())
                        }
                    }
                }
                is GetCustomBlockDefinitionByIdUseCase.Result.Failure.NotFound -> {
                    player.sendMessage(Msg.Action.invalidDefinition())
                }
            }
        }
    }
}
