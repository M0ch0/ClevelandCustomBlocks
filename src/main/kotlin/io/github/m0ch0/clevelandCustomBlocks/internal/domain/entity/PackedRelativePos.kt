package io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity

@JvmInline
value class PackedRelativePos private constructor(private val packed: Int) {

    companion object {
        // Ranges (chunk-local & world height)
        const val X_MIN = 0
        const val X_MAX = 15
        const val Z_MIN = 0
        const val Z_MAX = 15

        // / Origin correction for normalizing y（-64..319 を 0..383 に）
        const val Y_OFFSET = 64
        const val YN_MIN = 0
        const val YN_MAX = 383

        // Bit layout (x:4bits@20..23, z:4bits@16..19, yN:9bits@7..15)
        const val X_SHIFT = 20
        const val Z_SHIFT = 16
        const val Y_SHIFT = 7

        const val X_MASK = 0xF // 4 bits
        const val Z_MASK = 0xF // 4 bits
        const val Y_MASK = 0x1FF // 9 bits

        fun of(x: Int, y: Int, z: Int): PackedRelativePos? {
            if (x !in X_MIN..X_MAX || z !in Z_MIN..Z_MAX) return null

            val yN = y + Y_OFFSET
            if (yN !in YN_MIN..YN_MAX) return null

            val p = ((x and X_MASK) shl X_SHIFT) or
                    ((z and Z_MASK) shl Z_SHIFT) or
                    ((yN and Y_MASK) shl Y_SHIFT)
            return PackedRelativePos(p)
        }
    }

    val x: Int get() = (packed ushr X_SHIFT) and X_MASK
    val z: Int get() = (packed ushr Z_SHIFT) and Z_MASK
    val y: Int get() = ((packed ushr Y_SHIFT) and Y_MASK) - Y_OFFSET
}
