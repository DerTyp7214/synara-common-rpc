@file:JvmName("CommonNumber")
@file:Suppress("unused")

package dev.dertyp.core

import dev.dertyp.platformDateFromEpochMilliseconds
import kotlin.jvm.JvmName
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

fun Number.ifZero(default: Number): Number = if (this.toDouble() == 0.0) default else this

fun Float.roundToNDecimals(n: Int = 0): Float {
    return (this * 10.0.pow(n)).roundToInt() / 100.0f
}

fun Double.roundToNDecimals(n: Int = 0): Double {
    return (this * 10.0.pow(n)).roundToInt() / 100.0
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