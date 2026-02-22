@file:JvmName("CommonGeneric")
@file:Suppress("unused")

package dev.dertyp.core

import kotlin.jvm.JvmName

fun <T> T?.ifNull(default: () -> T): T = this ?: default()
