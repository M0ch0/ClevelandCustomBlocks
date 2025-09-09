package io.github.m0ch0.clevelandCustomBlocks.internal.integration.worldguard

import org.bukkit.block.Block
import org.bukkit.entity.Player

internal interface InteractProtection {
    /** @return true if the player is allowed to "Interact" the given block at its location. */
    fun canInteract(player: Player, block: Block): Boolean
}

internal class NoopInteractProtection : InteractProtection {
    override fun canInteract(player: Player, block: Block) = true
}
