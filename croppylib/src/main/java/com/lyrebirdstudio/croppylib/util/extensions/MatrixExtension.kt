package com.lyrebirdstudio.croppylib.util.extensions

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.view.animation.AccelerateDecelerateInterpolator

private val values = FloatArray(9)

fun Matrix.animateScaleToPoint(
    scaleFactor: Float,
    dx: Float,
    dy: Float,
    onUpdate: () -> Unit = {}
) {

    val targetMatrix = this.clone()
        .apply {
            postConcat(Matrix().apply {
                setScale(scaleFactor, scaleFactor, dx, dy)
            })
        }

    animateToMatrix(targetMatrix, onUpdate)
}

fun Matrix.animateToMatrix(
    targetMatrix: Matrix,
    onUpdate: () -> Unit = {}
) {

    val scaleAnimator = ValueAnimator.ofFloat(this.getScaleX(), targetMatrix.getScaleX())
    val translateXAnimator =
        ValueAnimator.ofFloat(this.getTranslateX(), targetMatrix.getTranslateX())
    val translateYAnimator =
        ValueAnimator.ofFloat(this.getTranslateY(), targetMatrix.getTranslateY())

    translateYAnimator.addUpdateListener {
        reset()
        preScale(
            scaleAnimator.animatedValue as Float,
            scaleAnimator.animatedValue as Float
        )
        postTranslate(
            translateXAnimator.animatedValue as Float,
            translateYAnimator.animatedValue as Float
        )
        onUpdate.invoke()
    }

    AnimatorSet()
        .apply {
            playTogether(
                scaleAnimator,
                translateXAnimator,
                translateYAnimator
            )
        }
        .apply { interpolator = AccelerateDecelerateInterpolator() }
        .apply { duration = 300 }
        .start()


}

fun Matrix.getScaleX(): Float {
    getValues(values)
    return values[Matrix.MSCALE_X]
}

fun Matrix.getScaleY(): Float {
    getValues(values)
    return values[Matrix.MSCALE_Y]
}

fun Matrix.getTranslateX(): Float {
    getValues(values)
    return values[Matrix.MTRANS_X]
}

fun Matrix.getTranslateY(): Float {
    getValues(values)
    return values[Matrix.MTRANS_Y]
}

fun Matrix.clone(): Matrix {
    getValues(values)
    return Matrix().apply {
        setValues(values)
    }
}
