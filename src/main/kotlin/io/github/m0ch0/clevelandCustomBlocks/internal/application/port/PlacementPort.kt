package io.github.m0ch0.clevelandCustomBlocks.internal.application.port

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

internal interface PlacementPort {
    fun place(
        player: Player,
        hand: EquipmentSlot,
        targetLocation: Location,
        itemInHand: ItemStack,
        customBlockId: String
    ): Boolean
}
