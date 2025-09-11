package io.github.m0ch0.clevelandCustomBlocks.internal.presentation.i18n

import net.kyori.adventure.text.Component

internal object Msg {

    object Common {

        fun runtimeError() = Component.translatable(MsgKey.Common.RUNTIME_ERROR)

        fun playerNotFound(playerName: String) = Component.translatable(
            MsgKey.Common.PLAYER_NOT_FOUND,
            Component.text(playerName)
        )
    }

    object Command {
        fun playerOnlyCommand() = Component.translatable(MsgKey.Command.ONLY_PLAYER)
    }

    object Reload {

        fun starting() = Component.translatable(MsgKey.Reload.STARTING)
        fun result(changed: Int) =
            Component.translatable(MsgKey.Reload.RESULT, Component.text(changed))

        fun resultWithWarn(changed: Int, warn: Int) =
            result(changed)
                .append(Component.translatable(MsgKey.Reload.RESULT_WARN, Component.text(warn)))
    }

    object Give {

        fun invalidAmount() =
            Component.translatable(MsgKey.Give.INVALID_AMOUNT)

        fun definitionNotFound(itemId: String) =
            Component.translatable(MsgKey.Give.BLOCK_NOT_FOUND, Component.text(itemId))

        fun invalidDefinition(itemId: String) =
            Component.translatable(MsgKey.Give.INVALID_DEFINITION, Component.text(itemId))

        fun invalidMaterial(itemId: String) =
            Component.translatable(MsgKey.Give.INVALID_MATERIAL, Component.text(itemId))

        fun gave(target: String, itemId: String, amount: Int) =
            Component.translatable(
                MsgKey.Give.GAVE,
                Component.text(target),
                Component.text(itemId),
                Component.text(amount)
            )
    }

    object Chunk {

        fun emptyRegistry() = Component.translatable(MsgKey.Chunk.EMPTY_REGISTRY)

        fun validSummary(count: Int) =
            Component.translatable(MsgKey.Chunk.VALID_SUMMARY, Component.text(count))

        fun invalidSummary(count: Int) =
            Component.translatable(MsgKey.Chunk.INVALID_SUMMARY, Component.text(count))

        fun cleanup(count: Int) =
            Component.translatable(MsgKey.Chunk.CLEANUP, Component.text(count))
    }

    object Action {

        fun invalidCommand() =
            Component.translatable(MsgKey.Action.INVALID_COMMAND)

        fun invalidCommandForOp() =
            Component.translatable(MsgKey.Action.INVALID_COMMAND_FOR_OP)

        fun invalidDefinition() =
            Component.translatable(MsgKey.Action.INVALID_DEFINITION)
    }
}
