package dev.dertyp.core

import kotlin.math.abs
import kotlin.math.sin

internal object Md5 {
    private val S = intArrayOf(
        7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
        5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
        4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
        6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21
    )

    private val K = IntArray(64) { i ->
        (abs(sin(i + 1.0)) * 4294967296.0).toLong().toInt()
    }

    fun digest(data: ByteArray): ByteArray {
        val message = pad(data)
        var a = 0x67452301
        var b = 0xefcdab89.toInt()
        var c = 0x98badcfe.toInt()
        var d = 0x10325476

        for (i in 0 until message.size / 64) {
            val x = IntArray(16)
            for (j in 0 until 16) {
                x[j] = (message[i * 64 + j * 4].toInt() and 0xff) or
                        (message[i * 64 + j * 4 + 1].toInt() and 0xff shl 8) or
                        (message[i * 64 + j * 4 + 2].toInt() and 0xff shl 16) or
                        (message[i * 64 + j * 4 + 3].toInt() and 0xff shl 24)
            }

            val aa = a
            val bb = b
            val cc = c
            val dd = d

            for (j in 0 until 64) {
                var f = 0
                var g = 0
                when (j / 16) {
                    0 -> {
                        f = (b and c) or (b.inv() and d)
                        g = j
                    }
                    1 -> {
                        f = (d and b) or (d.inv() and c)
                        g = (5 * j + 1) % 16
                    }
                    2 -> {
                        f = b xor c xor d
                        g = (3 * j + 5) % 16
                    }
                    3 -> {
                        f = c xor (b or d.inv())
                        g = (7 * j) % 16
                    }
                }
                val temp = d
                d = c
                c = b
                b += (a + f + K[j] + x[g]).rotateLeft(S[j])
                a = temp
            }

            a += aa
            b += bb
            c += cc
            d += dd
        }

        val result = ByteArray(16)
        for (i in 0 until 4) {
            val v = when (i) {
                0 -> a
                1 -> b
                2 -> c
                else -> d
            }
            result[i * 4] = v.toByte()
            result[i * 4 + 1] = (v ushr 8).toByte()
            result[i * 4 + 2] = (v ushr 16).toByte()
            result[i * 4 + 3] = (v ushr 24).toByte()
        }
        return result
    }

    private fun pad(data: ByteArray): ByteArray {
        val len = data.size
        val padLen = if (len % 64 < 56) 64 - len % 64 else 128 - len % 64
        val result = ByteArray(len + padLen)
        data.copyInto(result)
        result[len] = 0x80.toByte()
        val bitLen = len.toLong() * 8
        for (i in 0 until 8) {
            result[result.size - 8 + i] = (bitLen ushr (i * 8)).toByte()
        }
        return result
    }

    private fun Int.rotateLeft(n: Int): Int = (this shl n) or (this ushr (32 - n))
}

fun String.md5(): String {
    return Md5.digest(this.encodeToByteArray()).joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
}
