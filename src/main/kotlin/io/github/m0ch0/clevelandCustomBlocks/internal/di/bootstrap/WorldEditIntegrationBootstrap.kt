package io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap

import com.sk89q.worldedit.WorldEdit
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.worldEdit.WorldEditListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WorldEditIntegrationBootstrap @Inject constructor(
    private val plugin: ClevelandCustomBlocks,
    private val worldEditListener: WorldEditListener
) : StartupTask, ShutdownTask {

    private var registered = false

    override fun startup() {
        if (!hasWorldEdit()) return
        WorldEdit.getInstance().eventBus.register(worldEditListener)
        registered = true
        plugin.logger.info("ClevelandCustomBlocks: WorldEdit integration enabled.")
    }

    override fun shutdown() {
        if (!registered) return
        WorldEdit.getInstance().eventBus.unregister(worldEditListener)
        registered = false
    }

    private fun hasWorldEdit(): Boolean =
        plugin.server.pluginManager.getPlugin("WorldEdit") != null
}
