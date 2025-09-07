package io.github.m0ch0.clevelandCustomBlocks.internal.integration.worldguard

import org.bukkit.block.Block
import org.bukkit.entity.Player

internal interface BreakProtection {
    /** @return true if the player is allowed to "break" the given block at its location. */
    fun canBreak(player: Player, block: Block): Boolean
}

internal class NoopBreakProtection : BreakProtection {
    override fun canBreak(player: Player, block: Block) = true
}
