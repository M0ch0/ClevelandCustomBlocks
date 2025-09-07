package io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity

internal data class CustomBlockDefinition(
    val id: String,
    val displayName: String,
    val originalBlock: String,
    val actions: List<CustomBlockAction> = emptyList(),
    val orientation: Orientation = Orientation.NONE
)

internal enum class Orientation {
    NONE,
    FACE,
    STAIRS_LIKE
}
