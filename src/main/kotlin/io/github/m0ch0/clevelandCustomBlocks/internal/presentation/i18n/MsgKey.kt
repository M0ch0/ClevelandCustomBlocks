package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.i18n

internal object MsgKey {
    object Common {
        const val HEADER = "clevelandcustomblocks.common.header"
        const val PREFIX = "clevelandcustomblocks.common.prefix"

        const val RUNTIME_ERROR = "clevelandcustomblocks.common.runtime_error"

        const val PLAYER_NOT_FOUND = "clevelandcustomblocks.common.player_not_found" // {0}=name
    }

    object Command {
        const val ONLY_PLAYER = "clevelandcustomblocks.command.only_player"
    }

    object Reload {
        const val STARTING = "clevelandcustomblocks.reload.starting"
        const val RESULT = "clevelandcustomblocks.reload.result" // {0}=changedCount
        const val RESULT_WARN = "clevelandcustomblocks.reload.result_warn" // {0}=warnCount
    }

    object Give {

        const val BLOCK_NOT_FOUND = "clevelandcustomblocks.give.block_not_found" // {0}=itemId
        const val INVALID_DEFINITION = "clevelandcustomblocks.give.invalid_definition" // {0}=itemId
        const val GAVE = "clevelandcustomblocks.give.gave"
    }

    object Chunk {
        const val EMPTY_REGISTRY = "clevelandcustomblocks.chunk.empty_registry"
        const val VALID_SUMMARY = "clevelandcustomblocks.chunk.valid_summary" // {0}=count
        const val INVALID_SUMMARY = "clevelandcustomblocks.chunk.invalid_summary" // {0}=count
    }
}
