package dev.dertyp.data

import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Specific actions or features a user is authorized to perform.")
enum class UserCapability {
    IMPORT,
    EDIT,
    DELETE,
    PODCAST_EDIT;

    companion object {
        fun fromString(value: String?): UserCapability? = when (value?.uppercase()) {
            "IMPORT" -> IMPORT
            "EDIT" -> EDIT
            "DELETE" -> DELETE
            "PODCAST_EDIT" -> PODCAST_EDIT
            else -> null
        }
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresCapability(val capability: UserCapability)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresAdmin


