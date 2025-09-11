package io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.BreakProtectionPort
import org.bukkit.block.Block
import org.bukkit.entity.Player
import javax.inject.Inject

internal class CanBreakUseCase @Inject constructor(
    private val breakProtectionPort: BreakProtectionPort
) {

    operator fun invoke(player: Player, block: Block) =
        runCatching { breakProtectionPort.canBreak(player, block) }.getOrDefault(false)
}
