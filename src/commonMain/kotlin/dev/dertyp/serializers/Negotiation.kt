package dev.dertyp.serializers

inline fun <T> withSynaraPack(enabled: Boolean = true, block: () -> T): T {
    val old = SynaraNegotiation.isEnabled
    SynaraNegotiation.isEnabled = enabled
    try {
        return block()
    } finally {
        SynaraNegotiation.isEnabled = old
    }
}
