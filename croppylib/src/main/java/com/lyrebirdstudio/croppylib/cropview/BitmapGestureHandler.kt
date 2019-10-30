package com.lyrebirdstudio.croppylib.cropview

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import android.view.ScaleGestureDetector

class BitmapGestureHandler(
    private val context: Context,
    private val bitmapGestureListener: BitmapGestureListener
) {

    interface BitmapGestureListener {
        fun onScale(scaleFactor: Float, focusX: Float, focusY: Float)

        fun onScroll(distanceX: Float, distanceY: Float)

        fun onDoubleTap(motionEvent: MotionEvent)

        fun onEnd()
    }

    private var isScrolling = false

    private val scrollListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            isScrolling = true
            bitmapGestureListener.onScroll(distanceX, distanceY)
            return true
        }
    }

    private val doubleTapListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(event: MotionEvent?): Boolean {
            event?.let { bitmapGestureListener.onDoubleTap(it) }
            return true
        }
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            super.onScaleEnd(detector)
            bitmapGestureListener.onEnd()
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            bitmapGestureListener.onScale(detector.scaleFactor, detector.focusX, detector.focusY)
            return true
        }
    }

    private val scaleDetector = ScaleGestureDetector(context, scaleListener)

    private val scrollDetector = GestureDetector(context, scrollListener)

    private val doubleTapDetector = GestureDetector(context, doubleTapListener)

    fun onTouchEvent(motionEvent: MotionEvent) :Boolean {
        val scale = scaleDetector.onTouchEvent(motionEvent)
        val scroll = scrollDetector.onTouchEvent(motionEvent)
        val doubleTap = doubleTapDetector.onTouchEvent(motionEvent)

        /**
         * Detect if scrolling end. Call onEnd.
         */
        if(motionEvent.action == ACTION_UP){
            if(isScrolling){
                isScrolling = false
                bitmapGestureListener.onEnd()
            }
        }

        return scale || scroll || doubleTap

    }
}