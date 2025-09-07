package io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap

internal interface StartupTask {
    fun startup()
}

internal interface ShutdownTask {
    fun shutdown()
}

enum class Phase { FRAMEWORK, ADAPTERS, INTEGRATIONS, READY }
// RESERVED FOR THE FUTURE
