package io.github.m0ch0.clevelandCustomBlocks.internal

import co.aikar.commands.PaperCommandManager
import io.github.m0ch0.clevelandCustomBlocks.internal.di.DaggerPluginComponent
import io.github.m0ch0.clevelandCustomBlocks.internal.di.PluginComponent
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.command.clevelandCustomBlocks.ClevelandCustomBlocksCommand
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.block.BlockListener
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.entity.EntityListener
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.player.PlayerListener
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.server.ServerListener
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.world.WorldListener
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject

internal class ClevelandCustomBlocks : JavaPlugin() {

    lateinit var pluginComponent: PluginComponent

    @Inject
    lateinit var blockListener: BlockListener

    @Inject
    lateinit var playerListener: PlayerListener

    @Inject
    lateinit var entityListener: EntityListener

    @Inject
    lateinit var serverListener: ServerListener

    @Inject
    lateinit var worldListener: WorldListener

    @Inject
    lateinit var commandManager: PaperCommandManager

    @Inject
    lateinit var clevelandCustomBlocksCommand: ClevelandCustomBlocksCommand

    override fun onEnable() {
        pluginComponent = DaggerPluginComponent.builder()
            .plugin(this)
            .build()
        pluginComponent.inject(this)

        // Later, we will also send registerEvents and registerCommands to bootstrap.

        server.pluginManager.registerEvents(blockListener, this)
        server.pluginManager.registerEvents(playerListener, this)
        server.pluginManager.registerEvents(entityListener, this)
        server.pluginManager.registerEvents(serverListener, this)
        server.pluginManager.registerEvents(worldListener, this)

        pluginComponent.startupTasks().forEach { it.startup() }

        registerCommands()
    }

    override fun onDisable() {
        pluginComponent.shutdownTasks().forEach { it.shutdown() }
        logger.info("bye")
    }

    private fun registerCommands() {
        commandManager.registerCommand(clevelandCustomBlocksCommand)
    }
}
