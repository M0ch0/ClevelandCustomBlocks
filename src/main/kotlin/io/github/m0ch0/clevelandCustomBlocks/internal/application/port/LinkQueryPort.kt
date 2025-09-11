package io.github.m0ch0.clevelandCustomBlocks.internal.application.port

import org.bukkit.block.Block
import org.bukkit.entity.ItemDisplay

internal interface LinkQueryPort {
    fun linkedDisplayOf(block: Block): ItemDisplay?
    fun linkedBlockOf(itemDisplay: ItemDisplay): Block?
}
