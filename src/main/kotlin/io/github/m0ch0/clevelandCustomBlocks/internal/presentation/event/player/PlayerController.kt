package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.player

import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.entity.Player
import javax.inject.Inject

internal class PlayerController @Inject constructor(
    private val customBlocksService: ClevelandCustomBlocksService
) {

    fun onBarrierInteract(player: Player, clickedBlock: Block) {
        if (player.gameMode != GameMode.CREATIVE) {
            customBlocksService.removeAt(clickedBlock, false)
        } else {
            customBlocksService.removeAt(clickedBlock, true)
        }
    }
}
