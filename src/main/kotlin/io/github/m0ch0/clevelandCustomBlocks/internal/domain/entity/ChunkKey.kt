package io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity

import java.util.UUID

data class ChunkKey(val worldId: UUID, val x: Int, val z: Int)
