package io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo

import org.bukkit.Material

object CollisionBlock {
    val material: Material = Material.BARRIER
    /*
    Pure Clean Architecturally, vo should not touch framework objects,
    but since it's a plugin that's like a chunk of the framework, I think it's redundant to separate it.
     */
}
