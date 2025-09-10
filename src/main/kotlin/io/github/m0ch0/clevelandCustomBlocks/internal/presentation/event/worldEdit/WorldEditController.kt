package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.worldEdit

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.AbstractDelegateExtent
import com.sk89q.worldedit.extent.Extent
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.world.World
import com.sk89q.worldedit.world.block.BlockStateHolder
import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import org.bukkit.Bukkit
import javax.inject.Inject

internal class WorldEditController @Inject constructor(
    private val customBlocksService: ClevelandCustomBlocksService
) {

    fun onEditSessionBeforeReorder(worldEditWorld: World, previousExtent: Extent): Extent {
        val bukkitWorld = BukkitAdapter.adapt(worldEditWorld)

        return object : AbstractDelegateExtent(previousExtent) {

            override fun <T : BlockStateHolder<T>> setBlock(location: BlockVector3, block: T): Boolean {
                if (Bukkit.isPrimaryThread()) {
                    val current = bukkitWorld.getBlockAt(location.x(), location.y(), location.z())
                    if (current.type == CollisionBlock.material) {
                        customBlocksService.forceRemoveAt(current)
                    }
                }

                return super.setBlock(location, block)
            }
        }
    }
}
