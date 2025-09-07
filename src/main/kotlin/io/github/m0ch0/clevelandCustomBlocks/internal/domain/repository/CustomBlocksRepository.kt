package io.github.m0ch0.clevelandCustomBlocks.internal.domain.repository

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockDefinition
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockDefinitionsLoad

internal interface CustomBlocksRepository {
    fun getAll(): Map<String, CustomBlockDefinition>

    suspend fun load(): CustomBlockDefinitionsLoad.Result

    fun get(id: String): CustomBlockDefinition?
}
