package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.worldEdit

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.event.extent.EditSessionEvent
import com.sk89q.worldedit.util.eventbus.Subscribe
import javax.inject.Inject

internal class WorldEditListener @Inject constructor(
    private val controller: WorldEditController
) {

    @Subscribe
    fun onEditSession(event: EditSessionEvent) {
        if (event.stage != EditSession.Stage.BEFORE_REORDER) return
        val world = event.world ?: return

        val wrapped = controller.onEditSessionBeforeReorder(world, event.extent)
        event.extent = wrapped
    }
}
