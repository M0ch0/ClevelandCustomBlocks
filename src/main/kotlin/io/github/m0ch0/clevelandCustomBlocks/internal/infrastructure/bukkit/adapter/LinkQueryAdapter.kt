package io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.adapter

import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.LinkQueryPort
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.ItemDisplay
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
internal class LinkQueryAdapter @Inject constructor(
    @Named("link_world_uuid_key") private val linkWorldUuidKey: NamespacedKey,
    @Named("link_block_xyz_key") private val linkBlockXYZKey: NamespacedKey,
    private val plugin: ClevelandCustomBlocks // it may be better to provide a server instead of a plugin.
) : LinkQueryPort {

    /*
    Ideally, this should depend on the domain.repository interface, so Bukkit API objects
    shouldn't appear in the parameters. However, we're compromising here because doing otherwise
    would be verbose and re-fetching is costly (admittedly, this deviates from the principle of least privilege).
    Since this code aims to improve performance by being Bukkit-native without relying on an external DB,
    it's acceptable to pay development cost in exchange for performance. So I invented an excuse called application.port
     */

    override fun linkedDisplayOf(block: Block): ItemDisplay? {
        val world = block.world
        val x = block.x
        val y = block.y
        val z = block.z

        val center = Location(world, x.toDouble(), y.toDouble(), z.toDouble()).toCenterLocation()
        val worldUuidStr = world.uid.toString()

        return world.getNearbyEntities(center, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS)
            .asSequence()
            .filterIsInstance<ItemDisplay>()
            .firstOrNull { entity ->
                val pdc = entity.persistentDataContainer

                val linkedWorldUuid = pdc.get(linkWorldUuidKey, PersistentDataType.STRING)
                    ?: return@firstOrNull false
                if (linkedWorldUuid != worldUuidStr) return@firstOrNull false

                val xyz = pdc.get(linkBlockXYZKey, PersistentDataType.INTEGER_ARRAY)
                    ?: return@firstOrNull false

                if (xyz.size != VECTOR_ARGUMENT_SIZE) return@firstOrNull false

                xyz[0] == x && xyz[1] == y && xyz[2] == z
            }
    }

    override fun linkedBlockOf(itemDisplay: ItemDisplay): Block? {
        val worldUuidStr = itemDisplay.persistentDataContainer
            .get(linkWorldUuidKey, PersistentDataType.STRING) ?: return null

        val linkWorldUuid = runCatching { UUID.fromString(worldUuidStr) }
            .getOrNull() ?: return null
        val linkBlockXYZ = itemDisplay.persistentDataContainer
            .get(linkBlockXYZKey, PersistentDataType.INTEGER_ARRAY) ?: return null

        if (linkBlockXYZ.size != VECTOR_ARGUMENT_SIZE) return null

        val world = plugin.server.getWorld(linkWorldUuid) ?: return null

        val block = world.getBlockAt(linkBlockXYZ[0], linkBlockXYZ[1], linkBlockXYZ[2])

        if (block.type != CollisionBlock.material) return null

        return block
    }

    private companion object {
        const val SEARCH_RADIUS = 0.5
        const val VECTOR_ARGUMENT_SIZE = 3
    }
}
