package ru.despectra.proxiya.controller.store

import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap

class MutexStore<K : Any> {
    private val mutexMap = ConcurrentHashMap<K, Mutex>()

    operator fun get(key: K): Mutex = mutexMap.computeIfAbsent(key) { Mutex() }

    fun remove(key: K) = mutexMap.remove(key)

    fun clear() = mutexMap.clear()
}