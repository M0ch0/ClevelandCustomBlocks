package io.github.m0ch0.clevelandCustomBlocks.internal.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PackedRelativePosTest {

    @Test
    fun `of returns null when x z or y out of range`() {
        // x
        assertNull(PackedRelativePos.of(-1, 0, 0))
        assertNull(PackedRelativePos.of(16, 0, 0))

        // z
        assertNull(PackedRelativePos.of(0, 0, -1))
        assertNull(PackedRelativePos.of(0, 0, 16))

        // y
        assertNull(PackedRelativePos.of(0, -65, 0))
        assertNull(PackedRelativePos.of(0, 320, 0))
    }

    @Test
    fun `of accepts boundary values`() {
        val p1 = PackedRelativePos.of(0, -64, 0)
        assertNotNull(p1)
        assertEquals(0, p1.x)
        assertEquals(-64, p1.y)
        assertEquals(0, p1.z)

        val p2 = PackedRelativePos.of(15, 319, 15)
        assertNotNull(p2)
        assertEquals(15, p2.x)
        assertEquals(319, p2.y)
        assertEquals(15, p2.z)
    }

    @Test
    fun `round trip x y z across whole valid space`() {
        for (x in PackedRelativePos.X_MIN..PackedRelativePos.X_MAX) {
            for (z in PackedRelativePos.Z_MIN..PackedRelativePos.Z_MAX) {
                for (y in -PackedRelativePos.Y_OFFSET..(PackedRelativePos.YN_MAX - PackedRelativePos.Y_OFFSET)) {
                    val p = PackedRelativePos.of(x, y, z)
                    assertNotNull(p, "Expected non-null for ($x,$y,$z)")
                    assertEquals(x, p.x)
                    assertEquals(y, p.y)
                    assertEquals(z, p.z)
                }
            }
        }
    }
}
