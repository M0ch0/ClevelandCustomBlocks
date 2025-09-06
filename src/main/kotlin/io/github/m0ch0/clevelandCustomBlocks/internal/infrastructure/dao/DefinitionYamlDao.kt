package io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.dao

import io.github.m0ch0.clevelandCustomBlocks.internal.ClevelandCustomBlocks
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefinitionYamlDao @Inject constructor(
    private val plugin: ClevelandCustomBlocks
) {

    fun load(): FileConfiguration {
        val file = File(plugin.dataFolder, FILE_NAME)
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false)
        }
        return YamlConfiguration.loadConfiguration(file)
    }

    companion object {
        private const val FILE_NAME: String = "define.yml"
    }
}
