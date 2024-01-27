package com.lasley.kts_viewer.extensions

import android.app.Dialog

/**
 * Shows this [Dialog] within context of the current lifecycle
 *
 * @see actionWithinLifecycle
 */
fun Dialog.showWithLifecycle(onDismiss: () -> Unit = {}) {
    context.actionWithinLifecycle({ show() }) {
        onDismiss()
        dismiss()
    }
}