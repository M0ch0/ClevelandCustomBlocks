package io.github.m0ch0.clevelandCustomBlocks.internal.di

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.ShutdownTask
import io.github.m0ch0.clevelandCustomBlocks.internal.di.bootstrap.StartupTask
import io.github.m0ch0.clevelandCustomBlocks.internal.presentation.i18n.AdventureI18nBootstrap

@Module
internal object BootstrapModule {

    @Provides
    @IntoSet
    fun provideI18nStartupTask(i18n: AdventureI18nBootstrap): StartupTask = i18n

    @Provides
    @IntoSet
    fun provideI18nShutdown(i18n: AdventureI18nBootstrap): ShutdownTask = i18n
}
