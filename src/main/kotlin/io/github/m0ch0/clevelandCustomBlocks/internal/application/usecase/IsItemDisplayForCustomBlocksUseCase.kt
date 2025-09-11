package io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase

import org.bukkit.NamespacedKey
import org.bukkit.entity.ItemDisplay
import javax.inject.Inject
import javax.inject.Named

internal class IsItemDisplayForCustomBlocksUseCase @Inject constructor(
    @Named("custom_block_id_key") private val customBlockIdKey: NamespacedKey
) {

    operator fun invoke(itemDisplay: ItemDisplay): Boolean = itemDisplay.persistentDataContainer.has(customBlockIdKey)
}
