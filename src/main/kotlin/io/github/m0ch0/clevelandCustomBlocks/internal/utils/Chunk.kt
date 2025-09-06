package io.github.m0ch0.clevelandCustomBlocks.internal.utils

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.ChunkKey

fun org.bukkit.Chunk.toKey(): ChunkKey =
    ChunkKey(world.uid, x, z)
