package io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockDefinition
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.repository.CustomBlocksRepository
import javax.inject.Inject

internal class GetAllCustomBlockDefinitionsUseCase @Inject constructor(
    private val repository: CustomBlocksRepository
) {
    suspend operator fun invoke(): Map<String, CustomBlockDefinition> = repository.getAll()
}
