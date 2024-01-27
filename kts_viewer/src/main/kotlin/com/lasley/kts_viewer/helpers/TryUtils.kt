package com.lasley.kts_viewer.helpers

/*
 * Attempts to get data from [action], or returns a null.
 * Throws return null.
 */
inline fun <R : Any> tryOrNull(
    printStackTrace: Boolean = false,
    action: () -> R?
): R? {
    return try {
        action.invoke()
    } catch (e: Throwable) {
        if (printStackTrace)
            e.printStackTrace()
        null
    }
}
