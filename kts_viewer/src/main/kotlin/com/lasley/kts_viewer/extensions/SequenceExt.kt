package com.lasley.kts_viewer.extensions

import java.util.AbstractMap

/**
 * Filters [this] sequence of [Map.Entry],
 * which the [value][Map.Entry.value] part is a type of [Z].
 *
 * Note:
 * - the key value type is erased in the process.
 * - Use filterIsInstance to preserve key/ value pairs
 */
fun <Z> Sequence<Map.Entry<*, *>>.filterValueIsInstance()
        : Sequence<Map.Entry<*, Z>> {
    @Suppress("UNCHECKED_CAST")
    return filter { (it.value as? Z) != null }
        .map { AbstractMap.SimpleEntry(it.key, it.value as Z) }
}
