package io.github.m0ch0.clevelandCustomBlocks.internal.di
import co.aikar.commands.PaperCommandManager
import dagger.Module
import dagger.Provides
import io.github.m0ch0.clevelandCustomBlocks.api.service.ClevelandCustomBlocksService
import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.repository.CustomBlocksRepository
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service.ClevelandCustomBlocksServiceImpl
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.dao.DefinitionYamlDao
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.repository.YamlCustomBlocksRepository
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.NamespacedKey
import javax.inject.Named
import javax.inject.Singleton

@Module
internal object PluginModule {

    @Provides
    @Singleton
    fun provideComponentLogger(plugin: ClevelandCustomBlocks): ComponentLogger =
        plugin.componentLogger

    @Provides
    @Singleton
    fun provideCommandManager(plugin: ClevelandCustomBlocks): PaperCommandManager =
        PaperCommandManager(plugin)

    @Provides
    @Singleton
    @Named("custom_block_id_key")
    fun provideCustomBlockIdKey(plugin: ClevelandCustomBlocks): NamespacedKey =
        NamespacedKey(plugin, "custom-block-id")

    @Provides
    @Singleton
    @Named("link_world_uuid_key")
    fun provideLinkWorldUuidKey(plugin: ClevelandCustomBlocks): NamespacedKey =
        NamespacedKey(plugin, "link-world-uuid-key")

    @Provides
    @Singleton
    @Named("link_block_xyz_key")
    fun provideLinkBlockXYZKey(plugin: ClevelandCustomBlocks): NamespacedKey =
        NamespacedKey(plugin, "link-block-xyz-key")

    @Provides
    @Singleton
    @Named("chunk_block_index_key")
    fun provideChunkBlockIndexKey(plugin: ClevelandCustomBlocks): NamespacedKey =
        NamespacedKey(plugin, "chunk-block-index-key")

    @Provides
    @Singleton
    fun provideCustomBlocksRepository(dao: DefinitionYamlDao): CustomBlocksRepository =
        YamlCustomBlocksRepository(dao)

    @Provides
    @Singleton
    fun provideClevelandCustomBlocksService(impl: ClevelandCustomBlocksServiceImpl): ClevelandCustomBlocksService = impl
}
