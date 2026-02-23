package dev.dertyp.data

import kotlinx.serialization.Serializable

@Serializable
data class RpcEnvelope(val data: ByteArray? = null, val error: String? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RpcEnvelope) return false

        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data?.contentHashCode() ?: 0
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}