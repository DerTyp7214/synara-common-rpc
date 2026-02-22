@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package dev.dertyp.core

@JsFun("(s) => s.normalize('NFD').replace(/[\\u0300-\\u036f]/g, '')")
external fun jsStripAccents(s: String): String

actual fun String.stripAccents(): String = jsStripAccents(this)
