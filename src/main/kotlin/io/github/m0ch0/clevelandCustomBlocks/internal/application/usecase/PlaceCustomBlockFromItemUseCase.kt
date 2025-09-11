package io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.PlacementPort
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import javax.inject.Inject
import javax.inject.Named

internal class PlaceCustomBlockFromItemUseCase @Inject constructor(
    private val placement: PlacementPort,
    @Named("custom_block_id_key") private val customBlockIdKey: NamespacedKey
) {
    sealed interface Result {
        data object Success : Result
        sealed interface Failure : Result {
            data object NotCustomItem : Failure
            data object PlacementRejected : Failure
        }
    }

    operator fun invoke(
        player: Player,
        hand: EquipmentSlot,
        targetLocation: Location,
        itemInHand: ItemStack
    ): Result {
        val id = itemInHand.itemMeta.persistentDataContainer.get(customBlockIdKey, PersistentDataType.STRING)
            ?: return Result.Failure.NotCustomItem
        val ok = placement.place(player, hand, targetLocation, itemInHand, id)
        return if (ok) Result.Success else Result.Failure.PlacementRejected
    }
}
