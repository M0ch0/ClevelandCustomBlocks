package io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity

internal object CustomBlockDefinitionsLoad {
    data class Warning(val key: String, val invalidFields: List<String>)
    data class Result(val changed: Int, val warnings: List<Warning>)
}
