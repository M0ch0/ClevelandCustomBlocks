package io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap

import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.block.BlockListener
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.entity.EntityListener
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.player.PlayerListener
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.server.ServerListener
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.world.WorldListener
import javax.inject.Inject

internal class EventListenerRegisterBootstrap @Inject constructor(
    private val plugin: ClevelandCustomBlocks,
    private val blockListener: BlockListener,
    private val entityListener: EntityListener,
    private val playerListener: PlayerListener,
    private val serverListener: ServerListener,
    private val worldListener: WorldListener
) : StartupTask {

    override fun startup() {
        plugin.server.pluginManager.registerEvents(blockListener, plugin)
        plugin.server.pluginManager.registerEvents(playerListener, plugin)
        plugin.server.pluginManager.registerEvents(entityListener, plugin)
        plugin.server.pluginManager.registerEvents(serverListener, plugin)
        plugin.server.pluginManager.registerEvents(worldListener, plugin)
    }
}
