@file:JvmName("CommonNumber")
@file:Suppress("unused")

package dev.dertyp.core

import dev.dertyp.platformDateFromEpochMilliseconds
import kotlin.jvm.JvmName
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

fun Number.ifZero(default: Number): Number = if (this.toDouble() == 0.0) default else this
fun Int.ifZeroNullable(default: () -> Int?): Int? = if (this == 0) default() else this

fun Float.roundToNDecimals(n: Int = 0): Float {
    val factor = 10.0.pow(n).toFloat()
    return round(this * factor) / factor
}

fun Double.roundToNDecimals(n: Int = 0): Double {
    val factor = 10.0.pow(n)
    return round(this * factor) / factor
}

fun Int.digitCount(): Int = when (this) {
    0 -> 1
    else -> log10(this.toDouble().absoluteValue).toInt() + 1
}

fun Int.zeroPad(length: Int): String {
    return this.toString().padStart(length, '0')
}

val Number.date get() = platformDateFromEpochMilliseconds(toLong())
@get:JvmName("dateNullable")
val Number?.date get() = this?.let { platformDateFromEpochMilliseconds(toLong()) }