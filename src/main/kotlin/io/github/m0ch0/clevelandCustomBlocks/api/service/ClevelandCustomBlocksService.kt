package io.github.m0ch0.clevelandCustomBlocks.api.service

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

interface ClevelandCustomBlocksService {

    suspend fun createItem(id: String, amount: Int = 1): ItemStack?

    suspend fun createBaseItem(id: String): ItemStack?

    fun customIdOf(item: ItemStack?): String?

    fun isCustomItem(item: ItemStack?): Boolean = customIdOf(item) != null

    suspend fun placeFromItem(
        player: Player,
        hand: EquipmentSlot,
        target: Location,
        itemInHand: ItemStack
    ): Boolean

    fun removeAt(block: Block, dropItem: Boolean = true): Boolean

    fun linkedDisplayOf(block: Block): ItemDisplay?

    fun linkedBlockOf(display: ItemDisplay): Block?

    fun listRegisteredPositions(chunk: Chunk): Set<Location>
}
