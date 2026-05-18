package dev.dertyp.rpc.annotations

/**
 * Marks an RPC function to be cached.
 *
 * @param duration The duration to cache the result for (e.g., "5m", "1h").
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cached(
    val duration: String
)
