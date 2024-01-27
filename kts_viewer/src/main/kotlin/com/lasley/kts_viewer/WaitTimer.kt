package com.lasley.kts_viewer

import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

/**
 * Wrapper for the [Timer] class with some helper functions
 *
 * @param durationInMillis How long to wait until [onFinish] is called
 * @param startDelay Delay before the timer starts
 * @param repeating When true, the timer will continue every [durationInMillis]
 */
class WaitTimer(
    val durationInMillis: Long,
    val startDelay: Long = 0,
    val repeating: Boolean = false
) {
    private var timer: TimerTask? = null
    private var onFinishAction: () -> Unit = {}
    val isActive: Boolean
        get() = timer != null

    fun cancel() {
        timer?.cancel()
        timer = null
    }

    fun start() {
        timer = Timer().schedule(startDelay, durationInMillis) {
            if (!repeating) {
                timer?.cancel()
                timer = null
            }
            onFinishAction()
        }
        timer?.run()
    }

    fun onFinish(action: () -> Unit): WaitTimer {
        onFinishAction = action
        return this
    }
}
