package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.block

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.GetCustomBlockDefinitionByIdUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.ChunkIndexStore
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.CustomBlockLinkFinder
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import javax.inject.Inject
import javax.inject.Named

internal class BlockController @Inject constructor(
    private val plugin: ClevelandCustomBlocks,
    private val customBlockLinkFinder: CustomBlockLinkFinder,
    private val chunkIndexStore: ChunkIndexStore,
    @Named("link_world_uuid_key") private val linkWorldUuidKey: NamespacedKey,
    @Named("link_block_xyz_key") private val linkBlockXYZKey: NamespacedKey,
    private val getCustomBlockDefinitionByIdUseCase: GetCustomBlockDefinitionByIdUseCase
) {

    @Suppress("LongParameterList")
    fun onCustomBlockPlace(
        player: Player,
        itemInHand: ItemStack,
        hand: EquipmentSlot,
        targetLocation: Location,
        customBlockId: String,
    ) {
        // If this function becomes too large, create `infrastructure.bukkit.service.CustomBlockPlacementService`

        plugin.launch {
            val customBlockDefinition = getCustomBlockDefinitionByIdUseCase(customBlockId)

            when (customBlockDefinition) {
                is GetCustomBlockDefinitionByIdUseCase.Result.Failure.NotFound -> return@launch

                is GetCustomBlockDefinitionByIdUseCase.Result.Success -> {
                    val customBlock = customBlockDefinition.customBlock
                    val material: Material = Material.getMaterial(customBlock.originalBlock) ?: return@launch
                    if (itemInHand.type != material) { return@launch }

                    val targetLocation: Location = targetLocation.toBlockLocation()

                    chunkIndexStore.addIfMissing(
                            targetLocation.chunk,
                            targetLocation.blockX,
                            targetLocation.blockY,
                            targetLocation.blockZ
                        )

                    targetLocation.block.type = CollisionBlock.material

                    val itemDisplay: ItemDisplay = targetLocation.world.spawnEntity(
                        targetLocation.toCenterLocation(),
                        EntityType.ITEM_DISPLAY,
                        CreatureSpawnEvent.SpawnReason.CUSTOM
                    ) as ItemDisplay

                    itemDisplay.also {
                        it.setGravity(false)
                        it.setItemStack(itemInHand.clone().also { toShow -> toShow.amount = 1 })
                        it.isPersistent = true
                    }

                    itemDisplay.persistentDataContainer.also {
                        it.set(
                            linkWorldUuidKey,
                            PersistentDataType.STRING,
                            targetLocation.world.uid.toString()
                        )
                        it.set(
                            linkBlockXYZKey,
                            PersistentDataType.INTEGER_ARRAY,
                            intArrayOf(targetLocation.blockX, targetLocation.blockY, targetLocation.blockZ)
                        )
                    }

                    if (player.gameMode != GameMode.CREATIVE) {
                        decrementOneFromHand(player, hand)
                    }
                }
            }
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

    private fun decrementOneFromHand(player: Player, hand: EquipmentSlot) {
        val inv = player.inventory
        when (hand) {
            EquipmentSlot.HAND -> {
                val stack = inv.itemInMainHand
                if (stack.type == Material.AIR) return
                if (stack.amount <= 1) inv.setItemInMainHand(null) else stack.amount = stack.amount - 1
            }
            EquipmentSlot.OFF_HAND -> {
                val stack = inv.itemInOffHand
                if (stack.type == Material.AIR) return
                if (stack.amount <= 1) inv.setItemInOffHand(null) else stack.amount = stack.amount - 1
            }
            else -> { /* Nothing */ }
        }
    }
}
