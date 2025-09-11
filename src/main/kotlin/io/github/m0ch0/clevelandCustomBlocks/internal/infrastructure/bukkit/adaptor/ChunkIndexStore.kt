package io.github.m0ch0.clevelandCustomBlocks.internal.infrastructure.bukkit.adaptor

import io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity.PackedRelativePos
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

    fun list(chunk: Chunk): MutableSet<PackedRelativePos> {
        val bytes: ByteArray = chunk.persistentDataContainer.get(chunkBlockIndexKey, BYTE_ARRAY_DATA_TYPE)
            ?: return mutableSetOf()

        if (bytes.isEmpty()) return mutableSetOf()

        val positions = LinkedHashSet<PackedRelativePos>(bytes.size / BYTES_PER_POSITION)
        var index = 0
        while (index + BYTES_PER_POSITION - 1 < bytes.size) {
            val decoded = decodePosition(bytes, index)
            if (decoded != null) {
                positions += decoded
            }
            index += BYTES_PER_POSITION
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

        val outputBytes = ByteArray(positions.size * BYTES_PER_POSITION)
        var writeIndex = 0
        for (position in positions) {
            encodePositionInto(position, outputBytes, writeIndex)
            writeIndex += BYTES_PER_POSITION
        }
        chunk.persistentDataContainer.set(chunkBlockIndexKey, BYTE_ARRAY_DATA_TYPE, outputBytes)
    }

    companion object {
        private val BYTE_ARRAY_DATA_TYPE = PersistentDataType.BYTE_ARRAY

        private const val BYTES_PER_POSITION = 3
        private const val CHUNK_SHIFT = 4

        fun worldToRelativePosition(worldX: Int, worldY: Int, worldZ: Int, chunk: Chunk): PackedRelativePos? {
            // worldX - (shl CHUNK_SHIFT = 2^4 = *16 = origin of the chunk.) = exact location.
            val relativeX = worldX - (chunk.x shl CHUNK_SHIFT)
            val relativeZ = worldZ - (chunk.z shl CHUNK_SHIFT)
            return PackedRelativePos.of(relativeX, worldY, relativeZ)
        }

        fun relativeToWorldLocation(chunk: Chunk, pos: PackedRelativePos): Location {
            // shl CHUNK_SHIFT = 2^4 = *16 = origin of the chunk.
            val chunkOriginX = chunk.x shl CHUNK_SHIFT
            val chunkOriginZ = chunk.z shl CHUNK_SHIFT

            val worldX = chunkOriginX + pos.x
            val worldY = pos.y
            val worldZ = chunkOriginZ + pos.z

            return Location(chunk.world, worldX.toDouble(), worldY.toDouble(), worldZ.toDouble())
        }

        private fun encodePositionInto(position: PackedRelativePos, destination: ByteArray, offset: Int) {
            val x = position.x
            val z = position.z
            val yN = position.y + PackedRelativePos.Y_OFFSET // 0..383

            val packed = ((x and PackedRelativePos.X_MASK) shl PackedRelativePos.X_SHIFT) or
                    ((z and PackedRelativePos.Z_MASK) shl PackedRelativePos.Z_SHIFT) or
                    ((yN and PackedRelativePos.Y_MASK) shl PackedRelativePos.Y_SHIFT)

            destination[offset] = ((packed ushr 16) and 0xFF).toByte()
            destination[offset + 1] = ((packed ushr 8) and 0xFF).toByte()
            destination[offset + 2] = (packed and 0xFF).toByte()
        }

        private fun decodePosition(bytes: ByteArray, offset: Int): PackedRelativePos? {
            val byte0 = bytes[offset].toInt() and 0xFF
            val byte1 = bytes[offset + 1].toInt() and 0xFF
            val byte2 = bytes[offset + 2].toInt() and 0xFF

            val packed = (byte0 shl 16) or (byte1 shl 8) or byte2

            val x = (packed ushr PackedRelativePos.X_SHIFT) and PackedRelativePos.X_MASK
            val z = (packed ushr PackedRelativePos.Z_SHIFT) and PackedRelativePos.Z_MASK
            val yNormalized = (packed ushr PackedRelativePos.Y_SHIFT) and PackedRelativePos.Y_MASK
            val y = yNormalized - PackedRelativePos.Y_OFFSET

            return PackedRelativePos.of(x, y, z)
        }
    }
}
