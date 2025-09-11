package io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service

import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.CreateBaseCustomItemUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.FindLinkedBlockUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.FindLinkedItemDisplayUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.ForceRemoveByItemDisplayUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.ForceRemoveCustomBlockAtUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.ListRegisteredPositionsUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.PlaceCustomBlockFromItemUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase.RemoveCustomBlockAtUseCase
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
internal class ClevelandCustomBlocksServiceImpl @Inject constructor(
    private val createBaseCustomItemUseCase: CreateBaseCustomItemUseCase,
    private val placeCustomBlockFromItemUseCase: PlaceCustomBlockFromItemUseCase,
    private val removeCustomBlockAtUseCase: RemoveCustomBlockAtUseCase,
    private val forceRemoveCustomBlockAtUseCase: ForceRemoveCustomBlockAtUseCase,
    private val forceRemoveByItemDisplayUseCase: ForceRemoveByItemDisplayUseCase,
    private val findLinkedItemDisplayUseCase: FindLinkedItemDisplayUseCase,
    private val findLinkedBlockUseCase: FindLinkedBlockUseCase,
    private val listRegisteredPositionsUseCase: ListRegisteredPositionsUseCase,
    @Named("custom_block_id_key") private val customBlockIdKey: NamespacedKey
) : ClevelandCustomBlocksService {

    override fun createBaseItem(id: String): ItemStack? {
        return when (val result = createBaseCustomItemUseCase(id)) {
            CreateBaseCustomItemUseCase.Result.Failure.DefinitionNotFound -> null
            CreateBaseCustomItemUseCase.Result.Failure.InvalidMaterial -> null
            is CreateBaseCustomItemUseCase.Result.Success -> result.item
        }
    }

    override fun createItem(id: String, amount: Int): ItemStack? {
        val base = createBaseItem(id) ?: return null
        val quantity = amount.coerceIn(1, base.maxStackSize)
        return base.asQuantity(quantity)
    }

    override fun customIdOf(item: ItemStack?): String? {
        val meta = item?.itemMeta ?: return null
        return meta.persistentDataContainer.get(customBlockIdKey, PersistentDataType.STRING)
    }

    override fun placeFromItem(
        player: Player,
        hand: EquipmentSlot,
        target: Location,
        itemInHand: ItemStack
    ): Boolean {
        customIdOf(itemInHand) ?: return false
        return when (placeCustomBlockFromItemUseCase(player, hand, target, itemInHand)) {
            PlaceCustomBlockFromItemUseCase.Result.Failure.NotCustomItem -> false
            PlaceCustomBlockFromItemUseCase.Result.Failure.PlacementRejected -> false
            PlaceCustomBlockFromItemUseCase.Result.Success -> true
        }
    }

    override fun removeAt(block: Block, dropItem: Boolean): Boolean {
        return when (removeCustomBlockAtUseCase(block, dropItem)) {
            RemoveCustomBlockAtUseCase.Result.Failure.LinkMissing -> false
            RemoveCustomBlockAtUseCase.Result.Failure.NotCustomCollision -> false
            RemoveCustomBlockAtUseCase.Result.Success -> true
        }
    }

    override fun forceRemoveAt(block: Block) = forceRemoveCustomBlockAtUseCase(block)

    override fun forceRemoveBy(itemDisplay: ItemDisplay) = forceRemoveByItemDisplayUseCase(itemDisplay)

    override fun linkedDisplayOf(block: Block): ItemDisplay? = findLinkedItemDisplayUseCase(block)

    override fun linkedBlockOf(display: ItemDisplay): Block? = findLinkedBlockUseCase(display)

    override fun listRegisteredPositions(chunk: Chunk): Set<Location> = listRegisteredPositionsUseCase(chunk)
}
