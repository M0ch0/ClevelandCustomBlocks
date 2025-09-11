package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.entity

import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import org.bukkit.entity.ItemDisplay
import javax.inject.Inject

internal class EntityController @Inject constructor(
    private val customBlocksService: ClevelandCustomBlocksService,
) {

    fun onItemDisplayRemoved(itemDisplay: ItemDisplay) {
        customBlocksService.forceRemoveBy(itemDisplay)
    }
}
