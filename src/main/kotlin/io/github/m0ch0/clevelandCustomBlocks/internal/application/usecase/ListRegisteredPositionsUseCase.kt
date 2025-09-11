package io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.ChunkIndexPort
import org.bukkit.Chunk
import org.bukkit.Location
import javax.inject.Inject

internal class ListRegisteredPositionsUseCase @Inject constructor(
    private val chunkIndex: ChunkIndexPort
) {
    operator fun invoke(chunk: Chunk): Set<Location> = chunkIndex.listRegisteredPositions(chunk)
}
