package io.github.m0ch0.clevelandCustomBlocks.internal.domain.vo

@JvmInline
@Suppress("MagicNumber")
value class PackedRelativePos private constructor(private val packed: Int) {

    companion object {

        @Suppress("ReturnCount")
        fun of(x: Int, y: Int, z: Int): PackedRelativePos? {
            if (x !in 0..15 || z !in 0..15) return null
            val yN = y + 64
            if (yN !in 0..383) return null

            val p = ((x and 0xF) shl 20) or
                    ((z and 0xF) shl 16) or
                    ((yN and 0x1FF) shl 7)
            return PackedRelativePos(p)
        }
    }

    @Suppress("MagicNumber")
    val x: Int get() = (packed ushr 20) and 0xF
    val z: Int get() = (packed ushr 16) and 0xF
    val y: Int get() = ((packed ushr 7) and 0x1FF) - 64
}
