package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.player

import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.CanBreakUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.CanInteractUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.InteractCustomBlockUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.RemoveCustomBlockAtUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.i18n.Msg
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.entity.Player
import javax.inject.Inject

internal class PlayerController @Inject constructor(
    private val logger: ComponentLogger,
    private val canBreakUseCase: CanBreakUseCase,
    private val canInteractUseCase: CanInteractUseCase,
    private val interactCustomBlock: InteractCustomBlockUseCase,
    private val removeCustomBlockAt: RemoveCustomBlockAtUseCase
) {

    fun onBarrierLeftClick(player: Player, clickedBlock: Block) {
        if (canBreakUseCase(player, clickedBlock).not()) return
        val drop = player.gameMode != GameMode.CREATIVE
        when (removeCustomBlockAt(clickedBlock, drop)) {
            RemoveCustomBlockAtUseCase.Result.Success -> Unit
            RemoveCustomBlockAtUseCase.Result.Failure.NotCustomCollision -> Unit
            RemoveCustomBlockAtUseCase.Result.Failure.LinkMissing -> Unit
        }
    }

    fun onBarrierRightClick(player: Player, clickedBlock: Block) {
        if (canInteractUseCase(player, clickedBlock).not()) return
        when (val result = interactCustomBlock(player, clickedBlock)) {
            InteractCustomBlockUseCase.Result.Ran,
            InteractCustomBlockUseCase.Result.NoAction,
            InteractCustomBlockUseCase.Result.Skipped -> Unit

            is InteractCustomBlockUseCase.Result.Failure.CommandError -> {
                logger.error(result.message)
                player.sendMessage(Msg.Action.invalidCommand())
                if (player.isOp) player.sendMessage(Msg.Action.invalidCommandForOp())
            }
            InteractCustomBlockUseCase.Result.Failure.DefinitionMissing ->
                player.sendMessage(Msg.Action.invalidDefinition())
        }
    }
}
