package com.lasley.kts_viewer.extensions

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner

/**
 * Returns a [LifecycleOwner] from the given [Context], or throws [ClassCastException]
 *
 * @exception ClassCastException Given context is not, or contains, a [LifecycleOwner]
 * @exception IllegalStateException
 * - Fragments; if the Fragment's [View][androidx.fragment.app.Fragment.getView] is null.
 */
@Throws(ClassCastException::class, IllegalStateException::class)
fun Context.LifecycleOwner(): LifecycleOwner {
    return when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.LifecycleOwner()

        else -> throw ClassCastException("Context does not support LifecycleOwner")
    }
}
