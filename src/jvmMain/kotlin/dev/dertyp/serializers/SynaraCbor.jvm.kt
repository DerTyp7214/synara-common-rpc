package dev.dertyp.serializers

actual object SynaraNegotiation {
    private val enabled = ThreadLocal.withInitial { false }
    actual var isEnabled: Boolean
        get() = enabled.get()
        set(value) = enabled.set(value)
}
