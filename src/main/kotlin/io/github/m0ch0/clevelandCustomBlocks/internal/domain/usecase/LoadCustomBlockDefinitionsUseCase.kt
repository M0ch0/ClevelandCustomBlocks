package io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockDefinitionsLoad
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.repository.CustomBlocksRepository
import javax.inject.Inject

internal class LoadCustomBlockDefinitionsUseCase @Inject constructor(
    private val customBlocksRepository: CustomBlocksRepository
) {

    @Suppress("detekt:TooGenericExceptionCaught")
    suspend operator fun invoke(): Result {
        try {
            val result = customBlocksRepository.load()
            return Result.Success(result.changed, result.warnings)
        } catch (throwable: Throwable) {
            // The only errors that can occur here are runtime-related errors, so Generic is fine.
            return Result.Failure.Unknown(throwable)
        }
    }

    sealed interface Result {
        data class Success(val changed: Int, val warnings: List<CustomBlockDefinitionsLoad.Warning>) : Result

        sealed interface Failure : Result {
            data class Unknown(val cause: Throwable) : Failure
        }
    }
}
