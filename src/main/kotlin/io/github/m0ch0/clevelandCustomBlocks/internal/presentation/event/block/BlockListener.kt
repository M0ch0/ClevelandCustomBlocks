package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.block

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.CollisionBlock
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType
import javax.inject.Inject
import javax.inject.Named

internal class BlockListener @Inject constructor(
    private val blockController: BlockController,
    @Named("custom_block_id_key") private val customBlockIdKey: NamespacedKey
) : Listener {

    @Suppress("ReturnCount")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCustomBlockPlaceEvent(event: BlockPlaceEvent) {
        if (event.canBuild().not()) { return }

        val item = event.itemInHand
        val meta = item.itemMeta ?: return

        val customBlockId: String =
            meta.persistentDataContainer.get(customBlockIdKey, PersistentDataType.STRING) ?: return

        // val cancel: CancelHandle = CancelHandle(event::setCancelled)
        /*
          Why not cancel this event? Because canceling the event will cause the server to revert the state of the Block,
          which means that the Block.type changed in this business logic will also be reverted.
         */
        val targetLocation = event.blockReplacedState.location
        blockController.onCustomBlockPlace(
            player = event.player,
            itemInHand = event.itemInHand.clone(),
            hand = event.hand,
            targetLocation = targetLocation,
            customBlockId = customBlockId,
        )
    }

    @Suppress("ReturnCount")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBarrierBreakEvent(event: BlockBreakEvent) {
        if (event.block.type != CollisionBlock.material) return

        blockController.onBarrierBreak(event.block)
    }
}
