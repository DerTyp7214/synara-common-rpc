package dev.dertyp.rpc.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

const val REMOVED_IN_API_9 = "Removed with API version 9."

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class LegacyWireName(val name: String)
