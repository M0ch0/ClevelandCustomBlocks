package io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.ChunkIndexPort
import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.LinkQueryPort
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import org.bukkit.Material
import org.bukkit.block.Block
import javax.inject.Inject

internal class ForceRemoveCustomBlockAtUseCase @Inject constructor(
    private val linkQuery: LinkQueryPort,
    private val chunkIndex: ChunkIndexPort
) {
    operator fun invoke(block: Block) {
        linkQuery.linkedDisplayOf(block)?.remove()
        if (block.type == CollisionBlock.material) block.type = Material.AIR
        chunkIndex.remove(block.chunk, block.x, block.y, block.z)
    }
}
