package io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap

import co.aikar.commands.PaperCommandManager
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.GetAllCustomBlockDefinitionsUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CommandCompletionBootstrap @Inject constructor(
    private val commandManager: PaperCommandManager,
    private val getAllCustomBlockDefinitionsUseCase: GetAllCustomBlockDefinitionsUseCase

) : StartupTask, ShutdownTask {
    override fun startup() {
        commandManager.commandCompletions.registerCompletion("ccb_ids") { ctx ->
            val ids: List<String> = getAllCustomBlockDefinitionsUseCase().keys.toList()
            if (ctx.input.isNullOrBlank()) {
                return@registerCompletion ids
            } else {
                return@registerCompletion ids.filter { it.startsWith(ctx.input, ignoreCase = true) }
            }
        }
    }

    override fun shutdown() {
        // commandManager.commandCompletions.unregisterCompletion("ccb_ids")
        // It seems there is no need to manually unregister (in fact, an error will be thrown if we do).
    }
}
