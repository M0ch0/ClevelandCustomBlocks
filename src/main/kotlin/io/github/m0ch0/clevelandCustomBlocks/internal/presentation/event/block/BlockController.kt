package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.block

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.ChunkIndexStore
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

internal class BlockController @Inject constructor(
    private val plugin: ClevelandCustomBlocks,
    private val customBlocksService: ClevelandCustomBlocksService,
    private val chunkIndexStore: ChunkIndexStore,
) {

    @Suppress("LongParameterList")
    fun onCustomBlockPlace(
        player: Player,
        itemInHand: ItemStack,
        hand: EquipmentSlot,
        targetLocation: Location,
    ) {
        if (!customBlocksService.isCustomItem(itemInHand)) return
        plugin.launch {
            customBlocksService.placeFromItem(player, hand, targetLocation, itemInHand)
        }
    }

    fun onBarrierBreak(brokenBlock: Block) {
        chunkIndexStore.remove(
            brokenBlock.chunk,
            brokenBlock.x,
            brokenBlock.y,
            brokenBlock.z
        )

        val linkedItemDisplay = customBlocksService.linkedDisplayOf(brokenBlock) ?: return
        linkedItemDisplay.remove()
    }
}
