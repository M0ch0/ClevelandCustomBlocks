package io.github.m0ch0.clevelandCustomBlocks.internal.application.port

import org.bukkit.block.Block
import org.bukkit.entity.Player

internal interface InteractProtectionPort { fun canInteract(player: Player, block: Block): Boolean }
internal interface BreakProtectionPort { fun canBreak(player: Player, block: Block): Boolean }
