package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.world

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.ChunkIndexStore
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.CustomBlockLinkFinder
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.entity.ItemDisplay
import javax.inject.Inject

internal class WorldController @Inject constructor(
    private val linkFinder: CustomBlockLinkFinder,
    private val chunkIndexStore: ChunkIndexStore
) {

    fun onItemDisplayLoad(itemDisplay: ItemDisplay) {
        val linkedBlock = linkFinder.findBlockByItemDisplay(itemDisplay)

        if (linkedBlock == null) {
            itemDisplay.remove()
            return
        }
    }

    fun onChunkLoad(chunk: Chunk) {
        val registeredPositions = chunkIndexStore.list(chunk)
        if (registeredPositions.isEmpty()) return

        val world = chunk.world

        for (packedPos in registeredPositions.toList()) {
            val worldLoc = ChunkIndexStore.relativeToWorldLocation(chunk, packedPos)
            val block = world.getBlockAt(worldLoc)

            if (block.type != CollisionBlock.material) {
                linkFinder.findItemDisplayByBlock(block)?.remove()
                chunkIndexStore.remove(chunk, worldLoc.blockX, worldLoc.blockY, worldLoc.blockZ)
                continue
            }

            val display = linkFinder.findItemDisplayByBlock(block)
            if (display == null) {
                block.type = Material.AIR
                chunkIndexStore.remove(chunk, worldLoc.blockX, worldLoc.blockY, worldLoc.blockZ)
            }
        }
    }
}
