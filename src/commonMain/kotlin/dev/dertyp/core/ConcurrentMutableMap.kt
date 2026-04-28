package dev.dertyp.core

expect class ConcurrentMutableMap<K : Any, V : Any>() : MutableMap<K, V> {
    override val size: Int
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun get(key: K): V?
    override fun isEmpty(): Boolean
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override fun clear()
    override fun put(key: K, value: V): V?
    override fun putAll(from: Map<out K, V>)
    override fun remove(key: K): V?
    fun getOrPut(key: K, defaultValue: () -> V): V
}

expect fun <T : Any> concurrentMutableSetOf(): MutableSet<T>

fun <K : Any, V : Any> concurrentMutableMapOf(): ConcurrentMutableMap<K, V> = ConcurrentMutableMap()
