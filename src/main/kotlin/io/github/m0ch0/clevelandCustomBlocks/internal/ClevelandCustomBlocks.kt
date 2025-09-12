package io.github.m0ch0.clevelandCustomBlocks.internal

import io.github.m0ch0.clevelandCustomBlocks.internal.di.DaggerPluginComponent
import io.github.m0ch0.clevelandCustomBlocks.internal.di.PluginComponent
import org.bukkit.plugin.java.JavaPlugin

internal class ClevelandCustomBlocks : JavaPlugin() {

    lateinit var pluginComponent: PluginComponent

    override fun onEnable() {
        pluginComponent = DaggerPluginComponent.builder()
            .plugin(this)
            .build()
        pluginComponent.inject(this)
        pluginComponent.startupTasks().forEach { it.startup() }
    }

    override fun onDisable() {
        pluginComponent.shutdownTasks().forEach { it.shutdown() }
        logger.info("bye")
    }
}
