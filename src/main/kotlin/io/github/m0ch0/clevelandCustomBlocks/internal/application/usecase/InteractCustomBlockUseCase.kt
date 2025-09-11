package io.github.m0ch0.clevelandCustomBlocks.internal.application.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.ActionRunnerPort
import io.github.m0ch0.clevelandCustomBlocks.internal.application.port.LinkQueryPort
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase.GetCustomBlockDefinitionByIdUseCase
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.command.CommandException
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import javax.inject.Inject
import javax.inject.Named

internal class InteractCustomBlockUseCase @Inject constructor(
    private val linkQuery: LinkQueryPort,
    private val getCustomBlockDefinitionById: GetCustomBlockDefinitionByIdUseCase,
    private val actions: ActionRunnerPort,
    @Named("custom_block_id_key") private val customBlockIdKey: NamespacedKey
) {
    sealed interface Result {
        data object Skipped : Result
        data object NoAction : Result
        data object Ran : Result
        sealed interface Failure : Result {
            data object DefinitionMissing : Failure
            data class CommandError(val message: String) : Failure
        }
    }

    operator fun invoke(player: Player, clickedBlock: Block): Result {
        val display = linkQuery.linkedDisplayOf(clickedBlock) ?: return Result.Skipped
        val itemMeta = display.itemStack.itemMeta ?: return Result.Skipped
        val id = itemMeta.persistentDataContainer.get(customBlockIdKey, PersistentDataType.STRING)
            ?: return Result.Skipped

        return when (val definition = getCustomBlockDefinitionById(id)) {
            is GetCustomBlockDefinitionByIdUseCase.Result.Failure.NotFound ->
                Result.Failure.DefinitionMissing
            is GetCustomBlockDefinitionByIdUseCase.Result.Success -> {
                val plan = definition.customBlock.actions
                if (plan.isEmpty()) return Result.NoAction
                return try {
                    actions.runAll(player, clickedBlock, plan)
                    Result.Ran
                } catch (e: CommandException) {
                    Result.Failure.CommandError(e.message ?: e.toString())
                }
            }
        }
    }
}
