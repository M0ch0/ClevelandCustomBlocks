package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.block

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.ChunkIndexStore
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.CustomBlockLinkFinder
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.CustomBlockPlacementService
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

internal class BlockController @Inject constructor(
    private val plugin: ClevelandCustomBlocks,
    private val placementService: CustomBlockPlacementService,
    private val customBlockLinkFinder: CustomBlockLinkFinder,
    private val chunkIndexStore: ChunkIndexStore,
) {

    @Suppress("LongParameterList")
    fun onCustomBlockPlace(
        player: Player,
        itemInHand: ItemStack,
        hand: EquipmentSlot,
        targetLocation: Location,
        customBlockId: String,
    ) {
        plugin.launch {
            placementService.place(player, hand, targetLocation, itemInHand, customBlockId)
        }
    }

    fun onBarrierBreak(brokenBlock: Block) {
        chunkIndexStore.remove(
            brokenBlock.chunk,
            brokenBlock.x,
            brokenBlock.y,
            brokenBlock.z
        )

        val linkedItemDisplay = customBlockLinkFinder.findItemDisplayByBlock(brokenBlock) ?: return
        linkedItemDisplay.remove()
    }
}
