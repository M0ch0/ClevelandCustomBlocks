package io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap

import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import org.bukkit.plugin.ServicePriority
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PublicApiBootstrap @Inject constructor(
    private val plugin: ClevelandCustomBlocks,
    private val service: ClevelandCustomBlocksService
) : StartupTask, ShutdownTask {

    override fun startup() {
        plugin.server.servicesManager.register(
            ClevelandCustomBlocksService::class.java,
            service,
            plugin,
            ServicePriority.Normal
        )
    }

    override fun shutdown() {
        plugin.server.servicesManager.unregister(
            ClevelandCustomBlocksService::class.java,
            service
        )
    }
}
