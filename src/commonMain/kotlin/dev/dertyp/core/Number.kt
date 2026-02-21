@file:Suppress("unused")

package dev.dertyp.core

fun Number.ifZero(default: Number): Number = if (this.toDouble() == 0.0) default else this
