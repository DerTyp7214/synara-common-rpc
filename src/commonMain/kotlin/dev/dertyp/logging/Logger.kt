package dev.dertyp.logging

import dev.dertyp.currentTimeMillis
import dev.dertyp.getStacktrace
import dev.dertyp.ioDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

enum class LogLevel {
    INFO, WARNING, ERROR, CRASH
}

open class LogTag(val value: String) {
    init {
        @Suppress("LeakingThis")
        register(this)
    }

    companion object {
        private val _entries = mutableListOf<LogTag>()
        val entries: List<LogTag> get() = _entries.toList()

        fun register(tag: LogTag) {
            if (_entries.none { it.value == tag.value }) {
                _entries.add(tag)
            }
        }

        fun register(tags: List<LogTag>) {
            tags.forEach { register(it) }
        }

        val SCROBBLER = LogTag("scrobbler")
        val LISTENBRAINZ = LogTag("listenbrainz")
        val LASTFM = LogTag("last.fm")
        val MUSICBRAINZ = LogTag("musicbrainz")
        val RPC = LogTag("rpc")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LogTag) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String = value
}

interface LogPersistence {
    suspend fun persist(
        tag: LogTag,
        level: LogLevel,
        message: String,
        data: String?,
        stacktrace: String?,
        timestamp: Long
    )
}

interface Logger {
    fun info(tag: LogTag, message: String, data: Any? = null)
    fun warning(tag: LogTag, message: String, data: Any? = null)
    fun error(tag: LogTag, message: String, throwable: Throwable? = null, data: Any? = null)
    fun crash(tag: LogTag, message: String, throwable: Throwable? = null, data: Any? = null)

    suspend fun infoSuspended(tag: LogTag, message: String, data: Any? = null)
    suspend fun warningSuspended(tag: LogTag, message: String, data: Any? = null)
    suspend fun errorSuspended(tag: LogTag, message: String, throwable: Throwable? = null, data: Any? = null)
    suspend fun crashSuspended(tag: LogTag, message: String, throwable: Throwable? = null, data: Any? = null)
}

open class BaseLogger(
    protected val persistence: LogPersistence?,
    protected val json: Json
) : Logger {
    protected val scope = CoroutineScope(ioDispatcher + SupervisorJob())

    override fun info(tag: LogTag, message: String, data: Any?) {
        log(tag, LogLevel.INFO, message, data)
    }

    override fun warning(tag: LogTag, message: String, data: Any?) {
        log(tag, LogLevel.WARNING, message, data)
    }

    override fun error(tag: LogTag, message: String, throwable: Throwable?, data: Any?) {
        log(tag, LogLevel.ERROR, message, data, throwable?.stackTraceToString())
    }

    override fun crash(tag: LogTag, message: String, throwable: Throwable?, data: Any?) {
        log(tag, LogLevel.CRASH, message, data, throwable?.stackTraceToString())
    }

    override suspend fun infoSuspended(tag: LogTag, message: String, data: Any?) {
        logSuspended(tag, LogLevel.INFO, message, data, getStacktrace())
    }

    override suspend fun warningSuspended(tag: LogTag, message: String, data: Any?) {
        logSuspended(tag, LogLevel.WARNING, message, data, getStacktrace())
    }

    override suspend fun errorSuspended(tag: LogTag, message: String, throwable: Throwable?, data: Any?) {
        logSuspended(tag, LogLevel.ERROR, message, data, throwable?.stackTraceToString() ?: getStacktrace())
    }

    override suspend fun crashSuspended(tag: LogTag, message: String, throwable: Throwable?, data: Any?) {
        logSuspended(tag, LogLevel.CRASH, message, data, throwable?.stackTraceToString() ?: getStacktrace())
    }

    protected open fun serializeData(data: Any?): String? {
        return when (data) {
            null -> null
            is String -> data
            is Number, is Boolean -> data.toString()
            else -> data.toString()
        }
    }

    protected suspend fun logSuspended(
        tag: LogTag,
        level: LogLevel,
        message: String,
        data: Any?,
        stacktrace: String?
    ) {
        val stringData = try {
            serializeData(data)
        } catch (_: Exception) {
            null
        }

        persistence?.persist(
            tag = tag,
            level = level,
            message = message,
            data = stringData,
            stacktrace = stacktrace,
            timestamp = currentTimeMillis()
        )
    }

    private fun log(tag: LogTag, level: LogLevel, message: String, data: Any?, stacktrace: String? = getStacktrace()) {
        scope.launch {
            logSuspended(tag, level, message, data, stacktrace)
        }
    }
}
