package io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockDefinition
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.repository.CustomBlocksRepository
import javax.inject.Inject

internal class GetCustomBlockDefinitionByIdUseCase @Inject constructor(
    private val customBlocksRepository: CustomBlocksRepository
) {
    suspend operator fun invoke(id: String): Result {
        return customBlocksRepository.get(id)
            ?.let { Result.Success(it) }
            ?: Result.Failure.NotFound
    }

    sealed interface Result {
        data class Success(val customBlock: CustomBlockDefinition) : Result

        sealed interface Failure : Result {
            data object NotFound : Failure
        }
    }
}
