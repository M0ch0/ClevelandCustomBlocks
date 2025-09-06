package io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.repository

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockDefinition
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockDefinitionsLoad
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.repository.CustomBlocksRepository
import io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.dao.DefinitionYamlDao
import io.github.m0ch0.clevelandCustomBlocks.internal.utils.getNonBlankString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class YamlCustomBlocksRepository @Inject constructor(
    private val definitionYamlDao: DefinitionYamlDao,
) : CustomBlocksRepository {

    @Volatile
    private var cachedAll: Map<String, CustomBlockDefinition> = emptyMap()

    private val mutex = Mutex()

    // toMap is there to prevent accidental aliasing in a mixed Java/Kotlin environment
    override suspend fun getAll(): Map<String, CustomBlockDefinition> = cachedAll.toMap()

    override suspend fun get(id: String): CustomBlockDefinition? = cachedAll[id]

    override suspend fun load(): CustomBlockDefinitionsLoad.Result = mutex.withLock {
        val config: FileConfiguration = withContext(Dispatchers.IO) {
            definitionYamlDao.load()
        }

        val packName: String = config.getNonBlankString("packName") ?: "unknown"

        val blocksRoot: ConfigurationSection? = config.getConfigurationSection("blocks")
        if (blocksRoot == null) {
            val changed = countChanges(cachedAll, emptyMap())
            cachedAll = emptyMap()
            return@withLock CustomBlockDefinitionsLoad.Result(
                changed = changed,
                warnings = emptyList()
            )
        }

        val (newAll, warnings) = parseAll(blocksRoot, packName)

        val changed = countChanges(cachedAll, newAll)
        cachedAll = newAll

        CustomBlockDefinitionsLoad.Result(
            changed = changed,
            warnings = warnings
        )
    }

    private fun parseAll(
        root: ConfigurationSection,
        packName: String
    ): Pair<Map<String, CustomBlockDefinition>, List<CustomBlockDefinitionsLoad.Warning>> {
        val result = mutableMapOf<String, CustomBlockDefinition>()
        val warnings = mutableListOf<CustomBlockDefinitionsLoad.Warning>()

        for (key in root.getKeys(false)) {
            val child = root.getConfigurationSection(key) ?: continue
            // Since we are getting the child from getKeys, it cannot be null, but we need to convince the compiler.

            val displayName = child.getNonBlankString("displayName")
            val originalBlock = child.getNonBlankString("originalBlock")

            val invalids = buildList {
                if (displayName == null) {
                    add("displayName")
                }
                if ((originalBlock == null) || org.bukkit.Material.getMaterial(originalBlock) == null) {
                    add("originalBlock")
                }
            }

            if (invalids.isEmpty()) {
                result["$packName:$key"] = CustomBlockDefinition(
                    id = "$packName:$key",
                    displayName = requireNotNull(displayName),
                    originalBlock = requireNotNull(originalBlock)
                ) // The invalids.isEmpty() validates it's null-safe, even though the compiler doesn't recognize it.
            } else {
                warnings += CustomBlockDefinitionsLoad.Warning(key = key, invalidFields = invalids)
            }
        }

        return result to warnings
    }

    private fun countChanges(
        old: Map<String, CustomBlockDefinition>,
        new: Map<String, CustomBlockDefinition>
    ): Int {
        var changed = 0
        val oldKeys = old.keys
        val newKeys = new.keys

        changed += (newKeys - oldKeys).size
        changed += (oldKeys - newKeys).size

        for (key in oldKeys.intersect(newKeys)) {
            if (old[key] != new[key]) changed++
        }
        return changed
    }
}
