package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.block

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import javax.inject.Inject

internal class BlockListener @Inject constructor(
    private val blockController: BlockController
) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCustomBlockPlaceEvent(event: BlockPlaceEvent) {
        if (event.canBuild().not()) { return }

        val item = event.itemInHand
        if (item.itemMeta == null) return

        // val cancel: CancelHandle = CancelHandle(event::setCancelled)
        /*
          Why not cancel this event? Because canceling the event will cause the server to revert the state of the Block,
          which means that the Block.type changed in this business logic will also be reverted.
          Also, changing the block type automatically disables placement, so item stacks are not reduced.
          So we need to manually decrement itemstack in the controller.
          (It may look like it's being double-consumed, but it's not.)
         */

        val targetLocation = event.blockReplacedState.location
        blockController.onCustomBlockPlace(
            player = event.player,
            itemInHand = event.itemInHand.clone(),
            hand = event.hand,
            targetLocation = targetLocation
        )
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBarrierBreakEvent(event: BlockBreakEvent) {
        if (event.block.type != CollisionBlock.material) return

        blockController.onBarrierBreak(event.block)
    }
}
