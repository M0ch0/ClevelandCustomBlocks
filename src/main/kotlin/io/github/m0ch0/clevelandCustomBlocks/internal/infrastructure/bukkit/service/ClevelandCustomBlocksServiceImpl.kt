package io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service

import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.GetCustomBlockDefinitionByIdUseCase
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.adaptor.ChunkIndexStore
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.adaptor.CustomBlockLinkFinder
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.adaptor.CustomBlockPlacementAdaptor
import net.kyori.adventure.text.Component
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
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
    private val placement: CustomBlockPlacementAdaptor,
    private val linkFinder: CustomBlockLinkFinder,
    private val chunkIndexStore: ChunkIndexStore,
    private val getCustomBlockDefinitionById: GetCustomBlockDefinitionByIdUseCase,
    @Named("custom_block_id_key") private val customBlockIdKey: NamespacedKey
) : ClevelandCustomBlocksService {

    @Suppress("UnstableApiUsage", "ReturnCount")
    override fun createBaseItem(id: String): ItemStack? {
        val result = getCustomBlockDefinitionById(id)
        val definition = (result as? GetCustomBlockDefinitionByIdUseCase.Result.Success)?.customBlock ?: return null

        val material = Material.getMaterial(definition.originalBlock) ?: return null
        return ItemStack(material, 1).also { stack ->
            stack.editMeta { meta ->
                meta.itemName(Component.text(definition.displayName))
                val cmd = meta.customModelDataComponent
                cmd.strings = listOf(definition.id)
                meta.setCustomModelDataComponent(cmd)
                meta.persistentDataContainer.set(
                    customBlockIdKey,
                    PersistentDataType.STRING,
                    definition.id
                )
            }
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
        val id = customIdOf(itemInHand) ?: return false
        return placement.place(player, hand, target, itemInHand, id)
    }

    override fun removeAt(block: Block, dropItem: Boolean): Boolean {
        val linked = linkFinder.findItemDisplayByBlock(block) ?: return false

        if (dropItem) block.world.dropItem(block.location, linked.itemStack)

        if (block.type == CollisionBlock.material) block.type = Material.AIR

        linked.remove()
        return chunkIndexStore.remove(block.chunk, block.x, block.y, block.z)
    }

    override fun forceRemoveAt(block: Block) {
        linkFinder.findItemDisplayByBlock(block)?.remove()
        if (block.type == CollisionBlock.material) block.type = Material.AIR
        chunkIndexStore.remove(block.chunk, block.x, block.y, block.z)
    }

    override fun forceRemoveBy(itemDisplay: ItemDisplay) {
        val linkedCollisionBlock = linkedBlockOf(itemDisplay) ?: return

        linkedCollisionBlock.type = Material.AIR

        chunkIndexStore.remove(
            linkedCollisionBlock.chunk,
            linkedCollisionBlock.x,
            linkedCollisionBlock.y,
            linkedCollisionBlock.z
        )
    }

    override fun linkedDisplayOf(block: Block): ItemDisplay? = linkFinder.findItemDisplayByBlock(block)

    override fun linkedBlockOf(display: ItemDisplay): Block? = linkFinder.findBlockByItemDisplay(display)

    override fun listRegisteredPositions(chunk: Chunk): Set<Location> {
        val packed = chunkIndexStore.list(chunk)
        if (packed.isEmpty()) return emptySet()
        return packed.mapTo(LinkedHashSet(packed.size)) { pos ->
            ChunkIndexStore.relativeToWorldLocation(chunk, pos)
        }
    }
}
