package com.lasley.kts_viewer.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ContextThemeWrapper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.lasley.kts_viewer.helpers.tryOrNull

/**
 * Retrieve the activity from the given [Context]
 *
 */
val Context.getActivity: Activity
    get() {
        // `Application` does not contain direct to current activity
        // `ContextThemeWrapper` extends `ContextWrapper`
        // `FragmentActivity` extends `Activity`
        // `ViewComponentManager.FragmentContextWrapper` extends `ContextWrapper`
        return when (this) {
            is Activity -> this
            is ContextWrapper -> (baseContext as? Activity) ?: baseContext.getActivity
            else -> throw TypeCastException("Unable to parse $this as an Activity")
        }
    }


/**
 * Calls [doAction] and waits until the context is
 * [stopped][DefaultLifecycleObserver.onStop] or [destroyed][DefaultLifecycleObserver.onDestroy]
 * before calling [onDismiss].
 *
 * Note:
 * - If the context's activity is finishing, [doAction] won't be called
 */
fun Context?.actionWithinLifecycle(
    doAction: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    if (this == null) return
    if (getActivity.isFinishing) return

    val lifecycleOwner = tryOrNull { LifecycleOwner() } ?: return

    var observer: DefaultLifecycleObserver? = null

    fun dismissDialog() {
        observer?.also {
            lifecycleOwner.lifecycle.removeObserver(it)
            tryOrNull { onDismiss() }
        }
    }

    observer = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) = dismissDialog()
        override fun onDestroy(owner: LifecycleOwner) = dismissDialog()
    }

    lifecycleOwner.lifecycle.addObserver(observer)
    try {
        doAction()
    } catch (_: Exception) {
        dismissDialog() // cleanup if we fail to launch
    }
}
