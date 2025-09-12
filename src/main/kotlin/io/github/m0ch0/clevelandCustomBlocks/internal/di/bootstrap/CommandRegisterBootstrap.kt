package io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap

import co.aikar.commands.PaperCommandManager
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.command.clevelandCustomBlocks.ClevelandCustomBlocksCommand
import javax.inject.Inject

internal class CommandRegisterBootstrap @Inject constructor(
    private val commandManager: PaperCommandManager,
    private val clevelandCustomBlocksCommand: ClevelandCustomBlocksCommand,
) : StartupTask {

    override fun startup() {
        commandManager.registerCommand(clevelandCustomBlocksCommand)
    }
}
