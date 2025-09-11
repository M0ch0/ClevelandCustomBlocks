package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.world

import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import org.bukkit.Chunk
import org.bukkit.entity.ItemDisplay
import javax.inject.Inject

internal class WorldController @Inject constructor(
    private val customBlocksService: ClevelandCustomBlocksService,
) {

    fun onItemDisplayLoad(itemDisplay: ItemDisplay) {
        val linkedBlock = customBlocksService.linkedBlockOf(itemDisplay)

        if (linkedBlock == null) {
            itemDisplay.remove()
            return
        }
    }

    fun onChunkLoad(chunk: Chunk) {
        val registeredPositions = customBlocksService.listRegisteredPositions(chunk)
        if (registeredPositions.isEmpty()) return

        val world = chunk.world

        for (location in registeredPositions.toList()) {
            val block = world.getBlockAt(location)

            if (block.type != CollisionBlock.material) {
                customBlocksService.forceRemoveAt(block)
                continue
            }

            val display = customBlocksService.linkedDisplayOf(block)
            if (display == null) {
                customBlocksService.forceRemoveAt(block)
            }
        }
    }
}
