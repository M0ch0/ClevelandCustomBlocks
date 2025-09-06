package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.player

import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.ChunkIndexStore
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.CustomBlockLinkFinder
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import javax.inject.Inject

internal class PlayerController @Inject constructor(
    private val customBlockLinkFinder: CustomBlockLinkFinder,
    private val chunkIndexStore: ChunkIndexStore
) {

    fun onBarrierInteract(player: Player, clickedBlock: Block) {
        val linkedDisplay = customBlockLinkFinder.findItemDisplayByBlock(
            block = clickedBlock
        ) ?: return
        clickedBlock.type = Material.AIR
        if (player.gameMode != GameMode.CREATIVE) {
            clickedBlock.world.dropItem(clickedBlock.location, linkedDisplay.itemStack)
        }
        linkedDisplay.remove()
        chunkIndexStore.remove(
            clickedBlock.chunk,
            clickedBlock.x,
            clickedBlock.y,
            clickedBlock.z
        )
    }
}
