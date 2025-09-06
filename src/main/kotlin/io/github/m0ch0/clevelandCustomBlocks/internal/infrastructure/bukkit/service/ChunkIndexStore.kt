package io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.service

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo.PackedRelativePos
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Suppress("MagicNumber")
internal class ChunkIndexStore @Inject constructor(
    @Named("chunk_block_index_key") private val chunkBlockIndexKey: NamespacedKey
) {

    /*
    Ideally, this should depend on the domain.repository interface, so Bukkit API objects
    shouldn't appear in the parameters. However, we're compromising here because doing otherwise
    would be verbose and re-fetching is costly (admittedly, this deviates from the principle of least privilege).
    Since this code aims to improve performance by being Bukkit-native without relying on an external DB,
    it's acceptable to pay development cost in exchange for performance.
     */

    @Suppress("ReturnCount")
    fun list(chunk: Chunk): MutableSet<PackedRelativePos> {
        val bytes: ByteArray = chunk.persistentDataContainer.get(chunkBlockIndexKey, BYTE_ARRAY_DATA_TYPE)
            ?: return mutableSetOf()

        if (bytes.isEmpty()) return mutableSetOf()

        val positions = LinkedHashSet<PackedRelativePos>(bytes.size / 3)
        var index = 0
        while (index + 2 < bytes.size) {
            val decoded = decodePosition(bytes, index)
            if (decoded != null) {
                positions += decoded
            }
            index += 3
        }
        return positions
    }

    fun add(chunk: Chunk, position: PackedRelativePos): Boolean {
        val positions = list(chunk)
        if (positions.add(position)) {
            save(chunk, positions)
            return true
        }
        return false
    }

    fun addIfMissing(chunk: Chunk, position: PackedRelativePos): Boolean {
        val positions = list(chunk)
        if (!positions.contains(position)) {
            positions.add(position)
            save(chunk, positions)
            return true
        }
        return false
    }

    fun remove(chunk: Chunk, position: PackedRelativePos): Boolean {
        val positions = list(chunk)
        if (positions.remove(position)) {
            save(chunk, positions)
            return true
        }
        return false
    }

    fun add(chunk: Chunk, worldX: Int, worldY: Int, worldZ: Int): Boolean {
        val relative = worldToRelativePosition(worldX, worldY, worldZ, chunk) ?: return false
        return add(chunk, relative)
    }

    fun addIfMissing(chunk: Chunk, worldX: Int, worldY: Int, worldZ: Int): Boolean {
        val relative = worldToRelativePosition(worldX, worldY, worldZ, chunk) ?: return false
        return addIfMissing(chunk, relative)
    }

    fun remove(chunk: Chunk, worldX: Int, worldY: Int, worldZ: Int): Boolean {
        val relative = worldToRelativePosition(worldX, worldY, worldZ, chunk) ?: return false
        return remove(chunk, relative)
    }

    private fun save(chunk: Chunk, positions: Set<PackedRelativePos>) {
        if (positions.isEmpty()) {
            chunk.persistentDataContainer.remove(chunkBlockIndexKey)
            return
        }

        val outputBytes = ByteArray(positions.size * 3)
        var writeIndex = 0
        for (position in positions) {
            encodePositionInto(position, outputBytes, writeIndex)
            writeIndex += 3
        }
        chunk.persistentDataContainer.set(chunkBlockIndexKey, BYTE_ARRAY_DATA_TYPE, outputBytes)
    }

    companion object {
        private val BYTE_ARRAY_DATA_TYPE = PersistentDataType.BYTE_ARRAY

        fun worldToRelativePosition(worldX: Int, worldY: Int, worldZ: Int, chunk: Chunk): PackedRelativePos? {
            val relativeX = worldX - (chunk.x shl 4) // worldX - (shl 4 = 2^4 = *16 = origin of the chunk.) = exact loc.
            val relativeZ = worldZ - (chunk.z shl 4)
            return PackedRelativePos.of(relativeX, worldY, relativeZ)
        }

        fun relativeToWorldLocation(chunk: Chunk, pos: PackedRelativePos): Location {
            val chunkOriginX = chunk.x shl 4 // shl 4 = 2^4 = *16 = origin of the chunk.
            val chunkOriginZ = chunk.z shl 4

            val worldX = chunkOriginX + pos.x
            val worldY = pos.y
            val worldZ = chunkOriginZ + pos.z

            return Location(chunk.world, worldX.toDouble(), worldY.toDouble(), worldZ.toDouble())
        }

        private fun encodePositionInto(position: PackedRelativePos, destination: ByteArray, offset: Int) {
            val x = position.x
            val z = position.z
            val yN = position.y + 64 // 0..383

            val packed = ((x and 0xF) shl 20) or
                    ((z and 0xF) shl 16) or
                    ((yN and 0x1FF) shl 7)

            destination[offset] = ((packed ushr 16) and 0xFF).toByte()
            destination[offset + 1] = ((packed ushr 8) and 0xFF).toByte()
            destination[offset + 2] = (packed and 0xFF).toByte()
        }

        private fun decodePosition(bytes: ByteArray, offset: Int): PackedRelativePos? {
            val byte0 = bytes[offset].toInt() and 0xFF
            val byte1 = bytes[offset + 1].toInt() and 0xFF
            val byte2 = bytes[offset + 2].toInt() and 0xFF

            val packed = (byte0 shl 16) or (byte1 shl 8) or byte2

            val x = (packed ushr 20) and 0xF
            val z = (packed ushr 16) and 0xF
            val yNormalized = (packed ushr 7) and 0x1FF
            val y = yNormalized - 64

            return PackedRelativePos.of(x, y, z)
        }
    }
}
