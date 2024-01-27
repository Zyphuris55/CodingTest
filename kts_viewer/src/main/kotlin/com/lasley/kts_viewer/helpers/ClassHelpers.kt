package com.lasley.kts_viewer.helpers

/**
 * Tries to cast [this] as [R].
 *
 * [action] is also called when the casting is successful
 */
inline fun <R> Any?.cast(action: R.() -> Unit = {}): R? =
    @Suppress("UNCHECKED_CAST")
    (this as? R)?.also(action)
