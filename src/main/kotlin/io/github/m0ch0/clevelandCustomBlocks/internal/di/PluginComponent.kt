package io.github.m0ch0.clevelandCustomBlocks.internal.di

import dagger.BindsInstance
import dagger.Component
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.ShutdownTask
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.StartupTask
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        PluginModule::class,
        BootstrapModule::class
    ]
)
internal interface PluginComponent {

    fun inject(plugin: ClevelandCustomBlocks)

    fun startupTasks(): Set<StartupTask>

    fun shutdownTasks(): Set<ShutdownTask>

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun plugin(plugin: ClevelandCustomBlocks): Builder

        fun build(): PluginComponent
    }
}
