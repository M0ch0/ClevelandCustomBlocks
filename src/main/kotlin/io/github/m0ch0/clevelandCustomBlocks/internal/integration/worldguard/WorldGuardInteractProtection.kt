package io.github.m0ch0.clevelandCustomBlocks.internal.integration.worldguard

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flags
import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.InteractProtectionPort
import org.bukkit.block.Block
import org.bukkit.entity.Player

internal class WorldGuardInteractProtection : InteractProtectionPort {
    override fun canInteract(player: Player, block: Block): Boolean {
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        val weWorld = BukkitAdapter.adapt(block.world)

        if (WorldGuard.getInstance().platform.sessionManager.hasBypass(localPlayer, weWorld)) {
            return true
        }

        val query = WorldGuard.getInstance().platform.regionContainer.createQuery()
        val weLocation = BukkitAdapter.adapt(block.location)

        return query.testState(weLocation, localPlayer, Flags.INTERACT)
    }
}

internal class NoopInteractProtection : InteractProtectionPort {
    override fun canInteract(player: Player, block: Block) = true
}
