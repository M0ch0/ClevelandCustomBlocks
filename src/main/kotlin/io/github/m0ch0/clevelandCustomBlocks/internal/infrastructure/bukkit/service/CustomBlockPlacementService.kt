package io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.GetCustomBlockDefinitionByIdUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
internal class CustomBlockPlacementService @Inject constructor(
    private val chunkIndexStore: ChunkIndexStore,
    private val getCustomBlockById: GetCustomBlockDefinitionByIdUseCase,
    @Named("link_world_uuid_key") private val linkWorldUuidKey: NamespacedKey,
    @Named("link_block_xyz_key") private val linkBlockXYZKey: NamespacedKey,
) {

    @Suppress("ReturnCount")
    suspend fun place(
        player: Player,
        hand: EquipmentSlot,
        targetLocation: Location,
        itemInHand: ItemStack,
        customBlockId: String
    ): Boolean {
        val def = when (val result = getCustomBlockById(customBlockId)) {
            is GetCustomBlockDefinitionByIdUseCase.Result.Success -> result.customBlock
            is GetCustomBlockDefinitionByIdUseCase.Result.Failure.NotFound -> return false
        }

        val base = Material.getMaterial(def.originalBlock) ?: return false
        if (itemInHand.type != base) return false

        val location = targetLocation.toBlockLocation()

        chunkIndexStore.addIfMissing(
            location.chunk,
            location.blockX,
            location.blockY,
            location.blockZ
        )

        /*
            The intention of not returning even if addIfMissing is false:
             a no-op user can clean up accidental orphans by simply replace and breaking block.
         */

        location.block.type = CollisionBlock.material

        val display = location.world.spawnEntity(
            location.toCenterLocation(),
            EntityType.ITEM_DISPLAY,
            CreatureSpawnEvent.SpawnReason.CUSTOM
        ) as ItemDisplay

        display.setGravity(false)
        display.isPersistent = true
        display.setItemStack(itemInHand.clone().also { it.amount = 1 })

        display.persistentDataContainer.also {
            it.set(linkWorldUuidKey, PersistentDataType.STRING, location.world.uid.toString())
            it.set(
                linkBlockXYZKey,
                PersistentDataType.INTEGER_ARRAY,
                intArrayOf(
                    location.blockX,
                    location.blockY,
                    location.blockZ
                )
            )
        }

        if (player.gameMode != GameMode.CREATIVE) {
            decrementOneFromHand(player, hand)
            // I don't know why, but Bukkit doesn't subtract items when change the block.type in a BlockPlaceEvent.
            // So have to do it manually.
        }
        return true
    }

    private fun decrementOneFromHand(player: Player, hand: EquipmentSlot) {
        val inventory = player.inventory
        when (hand) {
            EquipmentSlot.HAND -> {
                val stack = inventory.itemInMainHand
                if (stack.type == Material.AIR) return
                if (stack.amount <= 1) inventory.setItemInMainHand(null) else stack.amount = stack.amount - 1
            }
            EquipmentSlot.OFF_HAND -> {
                val stack = inventory.itemInOffHand
                if (stack.type == Material.AIR) return
                if (stack.amount <= 1) inventory.setItemInOffHand(null) else stack.amount = stack.amount - 1
            }
            else -> Unit
        }
    }
}
