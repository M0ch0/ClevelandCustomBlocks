package io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity

internal data class CustomBlockAction(
    val executeAs: ExecuteAs,
    val command: String
) {
    enum class ExecuteAs { PLAYER, SERVER }
}
