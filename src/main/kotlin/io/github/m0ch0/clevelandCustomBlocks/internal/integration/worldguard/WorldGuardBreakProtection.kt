package io.github.m0ch0.clevelandCustomBlocks.internal.integration.worldguard

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flags
import org.bukkit.block.Block
import org.bukkit.entity.Player

internal class WorldGuardBreakProtection : BreakProtection {
    override fun canBreak(player: Player, block: Block): Boolean {
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        val weWorld = BukkitAdapter.adapt(block.world)

        if (WorldGuard.getInstance().platform.sessionManager.hasBypass(localPlayer, weWorld)) {
            return true
        }

        val query = WorldGuard.getInstance().platform.regionContainer.createQuery()
        val weLoc = BukkitAdapter.adapt(block.location)

        return query.testState(weLoc, localPlayer, Flags.BLOCK_BREAK, Flags.BUILD)
    }
}
