package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.player

import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.ClevelandCustomBlocksServiceImpl
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.entity.Player
import javax.inject.Inject

internal class PlayerController @Inject constructor(
    private val customBlocksService: ClevelandCustomBlocksServiceImpl
) {

    fun onBarrierInteract(player: Player, clickedBlock: Block) {
        if (player.gameMode != GameMode.CREATIVE) {
            customBlocksService.removeAt(clickedBlock, false)
        } else {
            customBlocksService.removeAt(clickedBlock, true)
        }
    }
}
