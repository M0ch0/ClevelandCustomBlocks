package io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.InteractProtectionPort
import org.bukkit.block.Block
import org.bukkit.entity.Player
import javax.inject.Inject

internal class CanInteractUseCase @Inject constructor(
    private val interactionProtectionPort: InteractProtectionPort
) {

    operator fun invoke(player: Player, block: Block) =
        runCatching { interactionProtectionPort.canInteract(player, block) }.getOrDefault(false)
}
