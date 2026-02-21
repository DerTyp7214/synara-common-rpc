@file:Suppress("unused")

package dev.dertyp.core

fun <T> T?.ifNull(default: () -> T): T = this ?: default()
