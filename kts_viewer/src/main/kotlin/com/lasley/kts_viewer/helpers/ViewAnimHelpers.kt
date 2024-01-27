package com.lasley.kts_viewer.helpers

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import kotlin.math.abs

/**
 * Returns [true][Boolean] if any of these [views] have active animations
 */
fun hasActiveAnimations(vararg views: View): Boolean {
    return views.asSequence()
        .filter { it.animation != null }
        .filterNot { it.animation.hasEnded() }
        .any()
}

/**
 * Expands the view in a linear expansion
 *
 * @param speedMult Speed multiplier of the animation
 *   - Default: 1.0
 *   - x < 1.0: faster
 *   - x > 1.0: slower
 */
fun View.expand(speedMult: Float = 1f, onFinished: () -> Unit = {}) {
    val matchParentMeasureSpec: Int = View.MeasureSpec.makeMeasureSpec(
        (parent as View).width,
        View.MeasureSpec.EXACTLY
    )

    val wrapContentMeasureSpec: Int =
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

    measure(matchParentMeasureSpec, wrapContentMeasureSpec)
    val targetHeight: Int = measuredHeight

    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    layoutParams.height = 1

    visibility = View.VISIBLE

    val anim: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            layoutParams.height =
                if (interpolatedTime == 1f) {
                    onFinished()
                    ViewGroup.LayoutParams.WRAP_CONTENT
                } else
                    (targetHeight * interpolatedTime).toInt()
            requestLayout()
        }

        override fun willChangeBounds(): Boolean = true
    }

    // Expansion speed of 1dp/ms
    val useSpeed = if (speedMult == 0f) 1f else speedMult
    anim.duration = ((targetHeight / context.resources.displayMetrics.density) * useSpeed).toLong()
    startAnimation(anim)
}

/**
 * Collapses the view in a linear expansion
 *
 * @param speedMult Speed multiplier of the animation
 *   - Default: 1.0
 *   - x < 1.0: faster
 *   - x > 1.0: slower
 */
fun View.collapse(speedMult: Float = 1f, onFinished: () -> Unit = {}) {
    if (visibility == View.GONE) {
        onFinished()
        return
    }
    val initialHeight: Int = measuredHeight
    val anim: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime == 1f) {
                onFinished()
                visibility = View.GONE
            } else {
                layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean = true
    }

    // Collapse speed of 1dp/ms
    val useSpeed = if (speedMult == 0f) 1f else speedMult
    anim.duration =
        ((initialHeight / context.resources.displayMetrics.density) * useSpeed).toLong()
    startAnimation(anim)
}


enum class RotateDirection {
    /** Clockwise (North -> East) */
    CW,

    /** Counter-clockwise (North -> West) */
    CCW
}

/**
 * Rotates this [view][this] from the current [rotation][View.getRotation] to the new [rotation][toD].
 *
 * @param toD Where the final rotation direction will point
 * @param direction Which direction the rotation will occur
 * @param duration How long this animation should last. The duration cannot be negative.
 */
fun View.rotate(
    toD: Float,
    direction: RotateDirection = RotateDirection.CW,
    duration: Long = 500L
) = rotate(rotation, toD, direction, duration)

/**
 * Rotates this [view][this] from [angle][fromD] to another [angle][toD].
 *
 * @param fromD Degrees from which to start the rotation
 * @param toD Degrees, from [fromD], which the view will rotate to
 * @param direction Which direction the rotation will occur
 * @param duration How long this animation should last. The duration cannot be negative.
 */
fun View.rotate(
    fromD: Float,
    toD: Float,
    direction: RotateDirection = RotateDirection.CW,
    duration: Long = 500L
) {
    rotation = fromD
    val rotateAmt = abs(fromD - toD)
    val anim: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            rotation = if (direction == RotateDirection.CW)
                fromD + (rotateAmt * interpolatedTime).toInt()
            else
                fromD - (rotateAmt * interpolatedTime).toInt()
            if (rotation > 360) rotation -= ((rotation / 360).toInt() * 360)
            if (rotation < 0) rotation += ((rotation / 360).toInt() * 360)

            requestLayout()
        }

        override fun willChangeBounds(): Boolean = true
    }

    anim.duration = duration
    startAnimation(anim)
}
