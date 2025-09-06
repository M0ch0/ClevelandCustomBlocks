package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.event.server

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.LoadCustomBlockDefinitionsUseCase
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import javax.inject.Inject

internal class ServerController @Inject constructor(
    private val plugin: ClevelandCustomBlocks,
    private val logger: ComponentLogger,
    private val loadCustomBlockDefinitionsUseCase: LoadCustomBlockDefinitionsUseCase
) {
    fun onServerLoad() {
        plugin.launch {
            val result = loadCustomBlockDefinitionsUseCase()
            when (result) {
                is LoadCustomBlockDefinitionsUseCase.Result.Success -> {
                    logger.info("${result.changed} definition(s) loaded.")

                    result.warnings.takeIf { it.isNotEmpty() }?.let { warnings ->
                        logger.warn("However, some definitions had issues (${warnings.size} entries):")
                        warnings.forEach { (key, fields) ->
                            logger.warn(" - $key: ${fields.joinToString(", ")}")
                        }
                    }
                }
                is LoadCustomBlockDefinitionsUseCase.Result.Failure.Unknown -> {
                    logger.error("Failed to load custom block definitions.", result.cause)
                }
            }
        }
    }
}
