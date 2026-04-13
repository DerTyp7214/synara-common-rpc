package dev.dertyp.rpc.annotations

/**
 * Marks an RPC interface or function for documentation.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RpcDoc(
    val description: String = "",
    val adminOnly: Boolean = false,
    val errors: Array<String> = []
)

/**
 * Describes a parameter of an RPC function.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RpcParamDoc(
    val description: String
)

/**
 * Marks a data class for inclusion in the documentation.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModelDoc(
    val description: String = ""
)

/**
 * Describes a property of a data class.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class FieldDoc(
    val description: String
)

/**
 * Marks a function as publicly accessible in REST, even if the service is authenticated.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RestPublic

/**
 * Marks a function for REST to respond with a file.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RestFileResponse

/**
 * Forces a function to use GET method in REST.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RestGet

/**
 * Forces a function to use POST method in REST.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RestPost

/**
 * Forces a function to use PUT method in REST.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RestPut

/**
 * Forces a function to use DELETE method in REST.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RestDelete
