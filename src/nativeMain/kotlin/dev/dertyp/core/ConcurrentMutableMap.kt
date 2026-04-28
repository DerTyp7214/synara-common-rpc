package dev.dertyp.core

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

actual class ConcurrentMutableMap<K : Any, V : Any> : MutableMap<K, V> {
    private val delegate = mutableMapOf<K, V>()
    private val mutex = Mutex()

    actual override val size: Int get() = runBlocking { mutex.withLock { delegate.size } }
    actual override fun containsKey(key: K): Boolean = runBlocking { mutex.withLock { delegate.containsKey(key) } }
    actual override fun containsValue(value: V): Boolean = runBlocking { mutex.withLock { delegate.containsValue(value) } }
    actual override fun get(key: K): V? = runBlocking { mutex.withLock { delegate[key] } }
    actual override fun isEmpty(): Boolean = runBlocking { mutex.withLock { delegate.isEmpty() } }
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = runBlocking { mutex.withLock { delegate.entries.toMutableSet() } }
    actual override val keys: MutableSet<K> get() = runBlocking { mutex.withLock { delegate.keys.toMutableSet() } }
    actual override val values: MutableCollection<V> get() = runBlocking { mutex.withLock { delegate.values.toMutableList() } }
    actual override fun clear() = runBlocking { mutex.withLock { delegate.clear() } }
    actual override fun put(key: K, value: V): V? = runBlocking { mutex.withLock { delegate.put(key, value) } }
    actual override fun putAll(from: Map<out K, V>) = runBlocking { mutex.withLock { delegate.putAll(from) } }
    actual override fun remove(key: K): V? = runBlocking { mutex.withLock { delegate.remove(key) } }

    actual fun getOrPut(key: K, defaultValue: () -> V): V = runBlocking {
        mutex.withLock {
            delegate.getOrPut(key, defaultValue)
        }
    }
}

private class ConcurrentMutableSet<T : Any> : MutableSet<T> {
    private val delegate = mutableSetOf<T>()
    private val mutex = Mutex()

    override val size: Int get() = runBlocking { mutex.withLock { delegate.size } }
    override fun add(element: T): Boolean = runBlocking { mutex.withLock { delegate.add(element) } }
    override fun addAll(elements: Collection<T>): Boolean = runBlocking { mutex.withLock { delegate.addAll(elements) } }
    override fun clear() = runBlocking { mutex.withLock { delegate.clear() } }
    override fun iterator(): MutableIterator<T> = runBlocking { mutex.withLock { delegate.toMutableList().iterator() } }
    override fun remove(element: T): Boolean = runBlocking { mutex.withLock { delegate.remove(element) } }
    override fun removeAll(elements: Collection<T>): Boolean = runBlocking { mutex.withLock { delegate.removeAll(elements) } }
    override fun retainAll(elements: Collection<T>): Boolean = runBlocking { mutex.withLock { delegate.retainAll(elements) } }
    override fun contains(element: T): Boolean = runBlocking { mutex.withLock { delegate.contains(element) } }
    override fun containsAll(elements: Collection<T>): Boolean = runBlocking { mutex.withLock { delegate.containsAll(elements) } }
    override fun isEmpty(): Boolean = runBlocking { mutex.withLock { delegate.isEmpty() } }
}

actual fun <T : Any> concurrentMutableSetOf(): MutableSet<T> {
    return ConcurrentMutableSet<T>()
}
