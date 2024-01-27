@file:Suppress("FunctionName")

package com.lasley.kts_viewer.helpers

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import androidx.viewbinding.ViewBinding
import com.lasley.kts_viewer.extensions.getActivity


/**
 * Displays a dialog which has a [ViewBinding][T] wrapped inside
 *
 * Dialog Defaults:
 * - No title flag
 * - Widow is:
 *    - width = Match Parent
 *    - height = Wrap Content
 *
 * @param preConfig Configuration before [setContentView][Dialog.setContentView]
 *   - Some configs (ex: [requestWindowFeature][Dialog.requestWindowFeature]) must be called before setContentView
 * @param config Binding and post-setup dialog configs
 */
inline fun <reified T : ViewBinding> BindingDialog(
    context: Context,
    preConfig: Dialog.() -> Unit = {},
    config: T.(Dialog) -> Unit
): Dialog {
    val useContext = if (context is Application)
        context.getActivity else context
    // todo; optionally not return Dialog if Activity/ context is finished?
    //   if ((this as? Activity)?.isFinishing == true) return
    return Dialog(useContext).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        preConfig(this)
        val binding: T = T::class.java.getBinding(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        tryOrNull { config(binding, this) }
    }
}