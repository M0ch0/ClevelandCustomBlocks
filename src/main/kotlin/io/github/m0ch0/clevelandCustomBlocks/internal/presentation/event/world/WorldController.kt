package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.world

import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.FindLinkedBlockUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.FindLinkedItemDisplayUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.ForceRemoveCustomBlockAtUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.IsItemDisplayForCustomBlocksUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.ListRegisteredPositionsUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import org.bukkit.Chunk
import org.bukkit.entity.ItemDisplay
import javax.inject.Inject

internal class WorldController @Inject constructor(
    private val findLinkedBlockUseCase: FindLinkedBlockUseCase,
    private val findLinkedItemDisplayUseCase: FindLinkedItemDisplayUseCase,
    private val listRegisteredPositionsUseCase: ListRegisteredPositionsUseCase,
    private val isItemDisplayForCustomBlocksUseCase: IsItemDisplayForCustomBlocksUseCase,
    private val forceRemoveCustomBlockAtUseCase: ForceRemoveCustomBlockAtUseCase,
) {

    fun onItemDisplayLoad(itemDisplay: ItemDisplay) {
        val linkedBlock = findLinkedBlockUseCase(itemDisplay)

        if (linkedBlock == null && isItemDisplayForCustomBlocksUseCase(itemDisplay)) {
            itemDisplay.remove()
            return
        }
    }

    fun onChunkLoad(chunk: Chunk) {
        val registeredPositions = listRegisteredPositionsUseCase(chunk)
        if (registeredPositions.isEmpty()) return

        val world = chunk.world

        for (location in registeredPositions.toList()) {
            val block = world.getBlockAt(location)

            if (block.type != CollisionBlock.material) {
                forceRemoveCustomBlockAtUseCase(block)
                continue
            }

            val display = findLinkedItemDisplayUseCase(block)
            if (display == null) {
                forceRemoveCustomBlockAtUseCase(block)
            }
        }
    }
}
