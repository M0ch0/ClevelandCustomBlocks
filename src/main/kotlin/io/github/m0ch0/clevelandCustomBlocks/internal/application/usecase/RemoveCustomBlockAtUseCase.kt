package io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.ChunkIndexPort
import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.LinkQueryPort
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import org.bukkit.Material
import org.bukkit.block.Block
import javax.inject.Inject

internal class RemoveCustomBlockAtUseCase @Inject constructor(
    private val linkQuery: LinkQueryPort,
    private val chunkIndex: ChunkIndexPort
) {
    sealed interface Result {
        data object Success : Result
        sealed interface Failure : Result {
            data object NotCustomCollision : Failure
            data object LinkMissing : Failure
        }
    }

    operator fun invoke(block: Block, dropItem: Boolean): Result {
        if (block.type != CollisionBlock.material) return Result.Failure.NotCustomCollision

        val display = linkQuery.linkedDisplayOf(block) ?: return Result.Failure.LinkMissing

        if (dropItem) block.world.dropItem(block.location, display.itemStack)
        if (block.type == CollisionBlock.material) block.type = Material.AIR
        display.remove()

        chunkIndex.remove(block.chunk, block.x, block.y, block.z)
        return Result.Success
    }
}
