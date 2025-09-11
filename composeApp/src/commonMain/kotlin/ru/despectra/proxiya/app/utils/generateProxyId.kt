package ru.despectra.proxiya.app.utils

import java.security.MessageDigest
import kotlin.random.Random

fun generateProxyId(name: String, params: Map<String, String>): String {
    val timestamp = System.currentTimeMillis()
    val input = "$name-${params.entries.sortedBy { it.key }.joinToString("-")}-$timestamp"
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(input.toByteArray())
        .joinToString("") { "%02x".format(it) }
        .take(12)
    val random = Random.nextInt(100, 999)
    return "${timestamp.toString().takeLast(6)}-${hash}-${random}"
}