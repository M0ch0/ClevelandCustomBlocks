package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.entity

import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.event.entity.EntityRemoveEvent.Cause
import javax.inject.Inject

internal class EntityListener @Inject constructor(
    private val entityController: EntityController,
) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemDisplayRemove(event: EntityRemoveEvent) {
        val entity = event.entity as? ItemDisplay ?: return
        when (event.cause) {
            Cause.DEATH, Cause.PLUGIN -> {
                entityController.onItemDisplayRemoved(entity)
            } // ItemDisplay doesn't die permanently otherwise, so 2 causes is fine.
            else -> return
        }
    }
}
