package io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.Orientation
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
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.round

@Singleton
internal class CustomBlockPlacementService @Inject constructor(
    private val chunkIndexStore: ChunkIndexStore,
    private val getCustomBlockById: GetCustomBlockDefinitionByIdUseCase,
    @Named("link_world_uuid_key") private val linkWorldUuidKey: NamespacedKey,
    @Named("link_block_xyz_key") private val linkBlockXYZKey: NamespacedKey,
) {

    @Suppress("ReturnCount")
    fun place(
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

        applyDisplayOrientation(display, player, def.orientation)

        if (player.gameMode != GameMode.CREATIVE) {
            decrementOneFromHand(player, hand)
            // I don't know why, but Bukkit doesn't subtract items when change the block.type in a BlockPlaceEvent.
            // So have to do it manually.
        }
        return true
    }

    private fun applyDisplayOrientation(display: ItemDisplay, player: Player, orientation: Orientation) {
        when (orientation) {
            Orientation.NONE -> return

            Orientation.FACE -> {
                val snappedYaw = snapYawToRightAngle(player.location.yaw)
                display.setRotation(snappedYaw + YAW_OFFSET_FACE, 0f)
            }

            Orientation.STAIRS_LIKE -> {
                val snappedYaw = snapYawToRightAngle(player.location.yaw)

                val flip = if (player.eyeLocation.y < display.location.y) true else false

                val offset = if (flip) YAW_OFFSET_FLIPPED else YAW_OFFSET_NO_FLIP
                val correctedYaw = wrapYawDeg(snappedYaw + offset)
                display.setRotation(correctedYaw, 0f)

                if (flip) {
                    val transformation = display.transformation
                    val flippedRoll = Quaternionf(transformation.leftRotation).rotateZ(Math.PI.toFloat())
                    display.transformation = Transformation(
                        transformation.translation,
                        flippedRoll,
                        transformation.scale,
                        transformation.rightRotation
                    )
                }
            }
        }
    }

    private fun snapYawToRightAngle(yaw: Float): Float {
        val normalized = normalizeYawDeg(yaw)
        val snapped = round(normalized / 90f) * 90f
        return wrapYawDeg(snapped)
    }

    private fun normalizeYawDeg(yaw: Float): Float {
        var y = yaw % 360f
        if (y >= 180f) y -= 360f
        if (y < -180f) y += 360f
        return y
    }

    private fun wrapYawDeg(yaw: Float): Float {
        var y = yaw % 360f
        if (y <= -180f) y += 360f
        if (y > 180f) y -= 360f
        return y
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

    companion object {
        private const val YAW_OFFSET_NO_FLIP: Float = -90f
        private const val YAW_OFFSET_FLIPPED: Float = 90f
        private const val YAW_OFFSET_FACE: Float = -180f
    }
}
