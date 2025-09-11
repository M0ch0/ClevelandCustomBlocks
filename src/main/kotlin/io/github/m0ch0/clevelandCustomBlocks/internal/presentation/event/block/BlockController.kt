package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.block

import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

internal class BlockController @Inject constructor(
    private val customBlocksService: ClevelandCustomBlocksService,
) {

    @Suppress("LongParameterList")
    fun onCustomBlockPlace(
        player: Player,
        itemInHand: ItemStack,
        hand: EquipmentSlot,
        targetLocation: Location,
    ) {
        if (!customBlocksService.isCustomItem(itemInHand)) return
        customBlocksService.placeFromItem(player, hand, targetLocation, itemInHand)
    }

    fun onBarrierBreak(brokenBlock: Block) {
        customBlocksService.forceRemoveAt(brokenBlock)
    }
}
