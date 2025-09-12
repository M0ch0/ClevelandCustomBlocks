package io.github.m0ch0.clevelandCustomBlocks.internal.di

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.AdventureI18nBootstrap
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.CommandCompletionBootstrap
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.CommandRegisterBootstrap
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.EventListenerRegisterBootstrap
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.PublicApiBootstrap
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.ShutdownTask
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.StartupTask
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.WorldEditIntegrationBootstrap

@Module
internal object BootstrapModule {

    @Provides
    @IntoSet
    fun provideI18nStartupTask(i18n: AdventureI18nBootstrap): StartupTask = i18n

    @Provides
    @IntoSet
    fun provideI18nShutdown(i18n: AdventureI18nBootstrap): ShutdownTask = i18n

    @Provides @IntoSet
    fun provideApiStartup(registrar: PublicApiBootstrap): StartupTask = registrar

    @Provides @IntoSet
    fun provideApiShutdown(registrar: PublicApiBootstrap): ShutdownTask = registrar

    @Provides @IntoSet
    fun provideCommandCompletionStartup(completion: CommandCompletionBootstrap): StartupTask = completion

    @Provides @IntoSet
    fun provideCommandCompletionShutdown(completion: CommandCompletionBootstrap): ShutdownTask = completion

    @Provides @IntoSet
    fun provideWorldEditStartup(bootstrap: WorldEditIntegrationBootstrap): StartupTask = bootstrap

    @Provides @IntoSet
    fun provideWorldEditShutdown(bootstrap: WorldEditIntegrationBootstrap): ShutdownTask = bootstrap

    @Provides @IntoSet
    fun provideEventListenerRegisterStartup(bootstrap: EventListenerRegisterBootstrap): StartupTask = bootstrap

    @Provides @IntoSet
    fun provideCommandRegisterStartup(bootstrap: CommandRegisterBootstrap): StartupTask = bootstrap
}
