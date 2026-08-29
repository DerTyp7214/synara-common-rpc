package dev.dertyp.ui

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Dynamic scalar or list value used in form payloads and action parameters. Exactly one field is set.")
data class UiValue(
    @FieldDoc("String value.")
    val text: String? = null,
    @FieldDoc("Numeric value.")
    val number: Double? = null,
    @FieldDoc("Boolean value.")
    val flag: Boolean? = null,
    @FieldDoc("List value.")
    val items: List<UiValue>? = null,
) {
    companion object {
        fun of(value: String) = UiValue(text = value)
        fun of(value: Double) = UiValue(number = value)
        fun of(value: Int) = UiValue(number = value.toDouble())
        fun of(value: Boolean) = UiValue(flag = value)
        fun list(values: List<UiValue>) = UiValue(items = values)
    }
}
