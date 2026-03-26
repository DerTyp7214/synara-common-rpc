package dev.dertyp.core

import kotlin.test.Test
import kotlin.test.assertEquals

class Md5Test {

    @Test
    fun testMd5() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", "".md5())
        assertEquals("900150983cd24fb0d6963f7d28e17f72", "abc".md5())
        assertEquals("6764017619ecc6d4230efbeab6e38143", "Synara".md5())
    }
}
