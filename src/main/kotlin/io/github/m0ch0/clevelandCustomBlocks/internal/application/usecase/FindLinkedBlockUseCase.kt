package io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.LinkQueryPort
import org.bukkit.block.Block
import org.bukkit.entity.ItemDisplay
import javax.inject.Inject

internal class FindLinkedBlockUseCase @Inject constructor(
    private val linkQuery: LinkQueryPort
) {
    operator fun invoke(display: ItemDisplay): Block? = linkQuery.linkedBlockOf(display)
}
