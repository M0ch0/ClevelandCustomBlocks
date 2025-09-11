package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.entity

import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.ForceRemoveByItemDisplayUseCase
import org.bukkit.entity.ItemDisplay
import javax.inject.Inject

internal class EntityController @Inject constructor(
    private val forceRemoveByItemDisplayUseCase: ForceRemoveByItemDisplayUseCase
) {

    fun onItemDisplayRemoved(itemDisplay: ItemDisplay) = forceRemoveByItemDisplayUseCase(itemDisplay)
}
