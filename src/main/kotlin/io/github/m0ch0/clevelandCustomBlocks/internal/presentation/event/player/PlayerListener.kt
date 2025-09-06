package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.player

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import javax.inject.Inject

internal class PlayerListener @Inject constructor(
    private val playerController: PlayerController
) : Listener {

    @Suppress("ReturnCount")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerInteractBarrierEvent(event: PlayerInteractEvent) {
        if (event.action != Action.LEFT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (block.type != CollisionBlock.material) return

        playerController.onBarrierInteract(event.player, block)
    }
}
