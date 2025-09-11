package io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.ChunkIndexPort
import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.LinkQueryPort
import org.bukkit.Material
import org.bukkit.entity.ItemDisplay
import javax.inject.Inject

internal class ForceRemoveByItemDisplayUseCase @Inject constructor(
    private val linkQuery: LinkQueryPort,
    private val chunkIndex: ChunkIndexPort
) {
    operator fun invoke(display: ItemDisplay) {
        val block = linkQuery.linkedBlockOf(display) ?: return
        block.type = Material.AIR
        chunkIndex.remove(block.chunk, block.x, block.y, block.z)
    }
}
