package io.github.m0ch0.clevelandCustomBlocks.internal.utils

import org.bukkit.configuration.ConfigurationSection

internal fun ConfigurationSection.getNonBlankString(path: String): String? =
    getString(path)?.trim()?.takeUnless { it.isBlank() }
