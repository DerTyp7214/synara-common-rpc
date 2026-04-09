package dev.dertyp.rpc.annotations

/**
 * Marks an RPC interface or function for documentation.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class RpcDoc(
    val description: String = "",
    val adminOnly: Boolean = false,
    val errors: Array<String> = []
)

/**
 * Describes a parameter of an RPC function.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class RpcParamDoc(
    val description: String
)

/**
 * Marks a data class for inclusion in the documentation.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ModelDoc(
    val description: String = ""
)

/**
 * Describes a property of a data class.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class FieldDoc(
    val description: String
)
