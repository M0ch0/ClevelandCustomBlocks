package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.server

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import javax.inject.Inject

internal class ServerListener @Inject constructor(
    private val serverController: ServerController
) : Listener {

    @EventHandler()
    @Suppress("detekt:UnusedParameter")
    fun onServerLoadEvent(event: ServerLoadEvent) {
        serverController.onServerLoad()
    }
}
