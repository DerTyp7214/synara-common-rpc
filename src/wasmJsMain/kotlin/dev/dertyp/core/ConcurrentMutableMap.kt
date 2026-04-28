package dev.dertyp.core

actual class ConcurrentMutableMap<K : Any, V : Any> : MutableMap<K, V> {
    private val delegate = mutableMapOf<K, V>()
    actual override val size: Int get() = delegate.size
    actual override fun containsKey(key: K): Boolean = delegate.containsKey(key)
    actual override fun containsValue(value: V): Boolean = delegate.containsValue(value)
    actual override fun get(key: K): V? = delegate[key]
    actual override fun isEmpty(): Boolean = delegate.isEmpty()
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = delegate.entries
    actual override val keys: MutableSet<K> get() = delegate.keys
    actual override val values: MutableCollection<V> get() = delegate.values
    actual override fun clear() = delegate.clear()
    actual override fun put(key: K, value: V): V? = delegate.put(key, value)
    actual override fun putAll(from: Map<out K, V>) = delegate.putAll(from)
    actual override fun remove(key: K): V? = delegate.remove(key)

    actual fun getOrPut(key: K, defaultValue: () -> V): V {
        return delegate.getOrPut(key, defaultValue)
    }
}

actual fun <T : Any> concurrentMutableSetOf(): MutableSet<T> = mutableSetOf()
