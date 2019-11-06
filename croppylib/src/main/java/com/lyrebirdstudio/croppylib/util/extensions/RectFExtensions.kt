package com.lyrebirdstudio.croppylib.util.extensions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.RectF
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import com.lyrebirdstudio.croppylib.util.model.AnimatableRectF
import com.lyrebirdstudio.croppylib.util.model.Corner
import com.lyrebirdstudio.croppylib.util.model.Edge
import kotlin.math.hypot

fun AnimatableRectF.animateTo(target: AnimatableRectF, onUpdate: (RectF) -> Unit = {}) {

    val animateLeft = ObjectAnimator.ofFloat(this, "left", left, target.left)
    val animateRight = ObjectAnimator.ofFloat(this, "right", right, target.right)
    val animateTop = ObjectAnimator.ofFloat(this, "top", top, target.top)
    val animateBottom = ObjectAnimator.ofFloat(this, "bottom", bottom, target.bottom)
    animateBottom.addUpdateListener {
        onUpdate.invoke(this)
    }

    AnimatorSet()
        .apply { playTogether(animateLeft, animateRight, animateTop, animateBottom) }
        .apply { interpolator = AccelerateDecelerateInterpolator() }
        .apply { duration = 300 }
        .start()
}

fun RectF.getEdgeTouch(touchEvent: MotionEvent, touchThreshold: Float = 50f): Edge {
    val isLeft = touchEvent.x < left + touchThreshold &&
            touchEvent.x > left - touchThreshold &&
            touchEvent.y > top &&
            touchEvent.y < bottom

    val isRight = touchEvent.x < right + touchThreshold &&
            touchEvent.x > right - touchThreshold &&
            touchEvent.y > top &&
            touchEvent.y < bottom

    val isTop = touchEvent.x < right &&
            touchEvent.x > left &&
            touchEvent.y < top + touchThreshold &&
            touchEvent.y > top - touchThreshold

    val isBottom = touchEvent.x < right &&
            touchEvent.x > left &&
            touchEvent.y < bottom + touchThreshold &&
            touchEvent.y > bottom - touchThreshold

    return when {
        isLeft -> Edge.LEFT
        isRight -> Edge.RIGHT
        isTop -> Edge.TOP
        isBottom -> Edge.BOTTOM
        else -> Edge.NONE
    }
}

fun RectF.getCornerTouch(touchEvent: MotionEvent, touchThreshold: Float = 50f): Corner {
    val isTopLeft =
        touchEvent.y < top + touchThreshold &&
                touchEvent.y > top - touchThreshold &&
                touchEvent.x < left + touchThreshold &&
                touchEvent.x > left - touchThreshold

    val isTopRight = touchEvent.y < top + touchThreshold &&
            touchEvent.y > top - touchThreshold &&
            touchEvent.x < right + touchThreshold &&
            touchEvent.x > right - touchThreshold

    val isBottomLeft = touchEvent.y < bottom + touchThreshold &&
            touchEvent.y > bottom - touchThreshold &&
            touchEvent.x < left + touchThreshold &&
            touchEvent.x > left - touchThreshold

    val isBottomRight = touchEvent.y < bottom + touchThreshold &&
            touchEvent.y > bottom - touchThreshold &&
            touchEvent.x < right + touchThreshold &&
            touchEvent.x > right - touchThreshold

    return when {
        isTopLeft -> Corner.TOP_LEFT
        isTopRight -> Corner.TOP_RIGHT
        isBottomLeft -> Corner.BOTTOM_LEFT
        isBottomRight -> Corner.BOTTOM_RIGHT
        else -> Corner.NONE
    }
}

fun RectF.getHypotenus(): Float {
    return hypot(height().toDouble(), width().toDouble()).toFloat()
}