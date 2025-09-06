package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.world

import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.EntitiesLoadEvent
import javax.inject.Inject

internal class WorldListener @Inject constructor(
    private val worldController: WorldController
) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntitiesLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            val itemDisplay = entity as? ItemDisplay ?: continue
            worldController.onItemDisplayLoad(itemDisplay)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChunkLoad(event: ChunkLoadEvent) {
        worldController.onChunkLoad(event.chunk)
    }
}
