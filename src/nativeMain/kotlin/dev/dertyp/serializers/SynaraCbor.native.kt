package dev.dertyp.serializers

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
actual object SynaraNegotiation {
    actual var isEnabled: Boolean = false
}
