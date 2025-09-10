package io.github.m0ch0.clevelandCustomBlocks.internal.domain.usecase

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockAction
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockDefinition
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.CustomBlockDefinitionsLoad
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.Orientation
import io.github.m0ch0.clevelandCustomBlocks.internal.domain.repository.CustomBlocksRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class FakeCustomBlocksRepository(
    private var backing: MutableMap<String, CustomBlockDefinition> = mutableMapOf(),
    private var onLoad: suspend () -> CustomBlockDefinitionsLoad.Result = {
        CustomBlockDefinitionsLoad.Result(changed = 0, warnings = emptyList())
    }
) : CustomBlocksRepository {

    override fun getAll(): Map<String, CustomBlockDefinition> = backing.toMap()
    override fun get(id: String): CustomBlockDefinition? = backing[id]
    override suspend fun load(): CustomBlockDefinitionsLoad.Result = onLoad()

    fun setAll(map: Map<String, CustomBlockDefinition>) {
        backing = map.toMutableMap()
    }

    fun setLoadBehavior(block: suspend () -> CustomBlockDefinitionsLoad.Result) {
        onLoad = block
    }
}

class UseCasesTest {

    private val sampleA = CustomBlockDefinition(
        id = "pack:a",
        displayName = "A",
        originalBlock = "STONE",
        actions = listOf(CustomBlockAction(CustomBlockAction.ExecuteAs.PLAYER, "/say hi")),
        orientation = Orientation.NONE
    )

    private val sampleB = CustomBlockDefinition(
        id = "pack:b",
        displayName = "B",
        originalBlock = "STONE_STAIRS",
        actions = emptyList(),
        orientation = Orientation.STAIRS_LIKE
    )

    @Test
    fun `GetAllCustomBlockDefinitionsUseCase returns repository map copy`() {
        val repo = FakeCustomBlocksRepository().apply {
            setAll(mapOf(sampleA.id to sampleA, sampleB.id to sampleB))
        }
        val usecase = GetAllCustomBlockDefinitionsUseCase(repo)

        val result = usecase()

        assertEquals(2, result.size)
        assertEquals(sampleA, result[sampleA.id])
        assertEquals(sampleB, result[sampleB.id])

        // Ensure it's a copy (mutation of returned map should not affect repo)
        val mutated = result.toMutableMap()
        mutated.clear()
        assertEquals(2, repo.getAll().size)
    }

    @Test
    fun `GetCustomBlockDefinitionByIdUseCase returns Success when present`() {
        val repo = FakeCustomBlocksRepository().apply { setAll(mapOf(sampleA.id to sampleA)) }
        val usecase = GetCustomBlockDefinitionByIdUseCase(repo)

        val res = usecase(sampleA.id)

        val success = assertIs<GetCustomBlockDefinitionByIdUseCase.Result.Success>(res)
        assertEquals(sampleA, success.customBlock)
    }

    @Test
    fun `GetCustomBlockDefinitionByIdUseCase returns NotFound when missing`() {
        val repo = FakeCustomBlocksRepository().apply { setAll(emptyMap()) }
        val usecase = GetCustomBlockDefinitionByIdUseCase(repo)

        val res = usecase("pack:missing")

        assertIs<GetCustomBlockDefinitionByIdUseCase.Result.Failure.NotFound>(res)
    }

    @Test
    fun `LoadCustomBlockDefinitionsUseCase returns Success and propagates counts`() {
        val warnings = listOf(
            CustomBlockDefinitionsLoad.Warning(key = "bad1", invalidFields = listOf("displayName")),
            CustomBlockDefinitionsLoad.Warning(key = "bad2", invalidFields = listOf("originalBlock", "action"))
        )
        val repo = FakeCustomBlocksRepository().apply {
            setLoadBehavior {
                CustomBlockDefinitionsLoad.Result(changed = 3, warnings = warnings)
            }
        }

        val usecase = LoadCustomBlockDefinitionsUseCase(repo)

        runBlocking {
            val res = usecase()
            val success = assertIs<LoadCustomBlockDefinitionsUseCase.Result.Success>(res)
            assertEquals(3, success.changed)
            assertEquals(2, success.warnings.size)
            assertTrue(success.warnings.any { it.key == "bad1" })
            assertTrue(success.warnings.any { it.key == "bad2" })
        }
    }

    @Test
    fun `LoadCustomBlockDefinitionsUseCase wraps thrown errors into Failure_Unknown`() {
        val boom = IllegalStateException("boom")
        val repo = FakeCustomBlocksRepository().apply {
            setLoadBehavior { throw boom }
        }

        val usecase = LoadCustomBlockDefinitionsUseCase(repo)

        runBlocking {
            val res = usecase()
            val failure = assertIs<LoadCustomBlockDefinitionsUseCase.Result.Failure.Unknown>(res)
            assertEquals(boom, failure.cause)
        }
    }
}
