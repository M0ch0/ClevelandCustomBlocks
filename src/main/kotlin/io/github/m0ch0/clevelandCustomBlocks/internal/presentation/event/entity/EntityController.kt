package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.entity

import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.ChunkIndexStore
import org.bukkit.Material
import org.bukkit.entity.ItemDisplay
import javax.inject.Inject

internal class EntityController @Inject constructor(
    private val customBlocksService: ClevelandCustomBlocksService,
    private val chunkIndexStore: ChunkIndexStore
) {

    fun onItemDisplayRemoved(itemDisplay: ItemDisplay) {
        val linkedCollisionBlock = customBlocksService.linkedBlockOf(itemDisplay) ?: return

        linkedCollisionBlock.type = Material.AIR

        chunkIndexStore.remove(
            linkedCollisionBlock.chunk,
            linkedCollisionBlock.x,
            linkedCollisionBlock.y,
            linkedCollisionBlock.z
        )
    }
}
