package io.github.m0ch0.clevelandCustomBlocks.internal.application.port

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.PackedRelativePos
import org.bukkit.Chunk
import org.bukkit.Location

internal interface ChunkIndexPort {
    fun listRegisteredPositions(chunk: Chunk): Set<Location>
    fun remove(chunk: Chunk, worldX: Int, worldY: Int, worldZ: Int): Boolean
    fun addIfMissing(chunk: Chunk, position: PackedRelativePos): Boolean
    fun addIfMissing(chunk: Chunk, worldX: Int, worldY: Int, worldZ: Int): Boolean
}
