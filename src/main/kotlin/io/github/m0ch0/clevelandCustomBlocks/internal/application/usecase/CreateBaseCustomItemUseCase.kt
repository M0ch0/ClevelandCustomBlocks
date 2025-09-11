package io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.GetCustomBlockDefinitionByIdUseCase
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import javax.inject.Inject
import javax.inject.Named

internal class CreateBaseCustomItemUseCase @Inject constructor(
    private val getCustomBlockDefinitionById: GetCustomBlockDefinitionByIdUseCase,
    @Named("custom_block_id_key") private val customBlockIdKey: NamespacedKey
) {
    sealed interface Result {
        data class Success(val item: ItemStack) : Result
        sealed interface Failure : Result {
            data object DefinitionNotFound : Failure
            data object InvalidMaterial : Failure
        }
    }

    @Suppress("UnstableApiUsage")
    operator fun invoke(id: String): Result {
        val definition = when (val result = getCustomBlockDefinitionById(id)) {
            is GetCustomBlockDefinitionByIdUseCase.Result.Failure.NotFound -> return Result.Failure.DefinitionNotFound
            is GetCustomBlockDefinitionByIdUseCase.Result.Success -> result.customBlock
        }
        val mat = Material.getMaterial(definition.originalBlock) ?: return Result.Failure.InvalidMaterial

        val stack = ItemStack(mat, 1).also { s ->
            s.editMeta { meta ->
                meta.itemName(Component.text(definition.displayName))
                val customModelDataComponent = meta.customModelDataComponent
                customModelDataComponent.strings = listOf(definition.id)
                meta.setCustomModelDataComponent(customModelDataComponent)
                meta.persistentDataContainer.set(
                    customBlockIdKey,
                    PersistentDataType.STRING,
                    definition.id
                )
            }
        }
        return Result.Success(stack)
    }
}
