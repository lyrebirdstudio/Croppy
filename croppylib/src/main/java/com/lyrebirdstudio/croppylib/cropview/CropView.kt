package com.lyrebirdstudio.croppylib.cropview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model.AspectRatio
import com.lyrebirdstudio.croppylib.cropview.AspectMode.*
import com.lyrebirdstudio.croppylib.util.model.Corner.*
import com.lyrebirdstudio.croppylib.util.model.Corner.NONE
import com.lyrebirdstudio.croppylib.util.model.DraggingState.DraggingCorner
import com.lyrebirdstudio.croppylib.util.model.DraggingState.DraggingEdge
import com.lyrebirdstudio.croppylib.util.model.Edge.*
import kotlin.math.max
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model.AspectRatio.*
import com.lyrebirdstudio.croppylib.ui.CroppedBitmapData
import com.lyrebirdstudio.croppylib.R
import com.lyrebirdstudio.croppylib.main.CroppyTheme
import com.lyrebirdstudio.croppylib.util.extensions.*
import com.lyrebirdstudio.croppylib.util.model.*
import java.lang.IllegalStateException
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

class CropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onInitialized: (() -> Unit)? = null

    var observeCropRectOnOriginalBitmapChanged: ((RectF) -> Unit)? = null

    private val cropRectOnOriginalBitmapMatrix = Matrix()

    /**
     * Touch threshold for corners and edges
     */
    private val touchThreshold = resources.getDimensionPixelSize(R.dimen.touch_threshold).toFloat()

    /**
     * Main rect which is drawn to canvas.
     */
    private val cropRect: AnimatableRectF =
        AnimatableRectF()

    /**
     * Temporary rect to animate crop rect to.
     * This value will be set to zero after using.
     */
    private val targetRect: AnimatableRectF =
        AnimatableRectF()

    /**
     * Minimum scale limitation is dependens on screen
     * and bitmap size. bitmapMinRect is calculated
     * initially. This value holds the miminum rectangle
     * which bitmapMatrix can be.
     */
    private val bitmapMinRect = RectF()

    /**
     * Minimum rectangle for cropRect can be.
     * This value will be only calculated on ACTION_DOWN.
     * Then will be check the crop rect value ACTION_MOVE and
     * override cropRect if it exceed its limit.
     */
    private val minRect = RectF()

    /**
     * Maximum rectangle for cropRect can be.
     * This value will be only calculated on ACTION_DOWN.
     * Then will be check the crop rect value ACTION_MOVE and
     * override cropRect if it exceed its limit.
     */
    private val maxRect = RectF()

    /**
     * Bitmap rect value. Holds original bitmap width
     * and height rectangle.
     */
    private val bitmapRect = RectF()

    /**
     * CropView rectangle. Holds view borders.
     */
    private val viewRect = RectF()

    /**
     * This value is hold view width minus margin between screen sides.
     * So it will be measuredWidth - dimen(R.dimen.default_crop_margin)
     */
    private var viewWidth = 0f

    /**
     * This value is hold view height minus margin between screen sides.
     * So it will be measuredWidth - dimen(R.dimen.default_crop_margin)
     */
    private var viewHeight = 0f

    /**
     * Original bitmap value
     */
    private var bitmap: Bitmap? = null

    /**
     * Bitmap matrix to draw bitmap on canvas
     */
    private val bitmapMatrix: Matrix = Matrix()

    /**
     * Empty paint to draw something on canvas.
     */
    private val emptyPaint = Paint().apply {
        isAntiAlias = true
    }

    /**
     * Default margin for cropRect.
     */
    private val marginInPixelSize =
        resources.getDimensionPixelSize(R.dimen.margin_max_crop_rect).toFloat()

    /**
     * Aspect ratio matters for calculation.
     * It can be ASPECT_FREE or ASPECT_X_X. Default
     * value is ASPECT_FREE
     */
    private var selectedAspectRatio = ASPECT_FREE

    /**
     * Aspect mode (FREE or ASPECT)
     */
    private var aspectAspectMode: AspectMode = FREE

    /**
     * User can drag crop rect from Corner, Edge or Bitmap
     */
    private var draggingState: DraggingState = DraggingState.Idle

    /**
     * Hold value for scaling bitmap with two finger.
     * We initialize this point to avoid memory
     * allocation every time user scale bitmap with fingers.
     */
    private val zoomFocusPoint = FloatArray(2)

    /**
     * This value holds inverted matrix when user scale
     * bitmap image with two finger. This value initialized to
     * avoid memory allocation every time user pinch zoom.
     */
    private val zoomInverseMatrix = Matrix()

    /**
     * Crop rect grid line width
     */
    private val gridLineWidthInPixel = resources.getDimension(R.dimen.grid_line_width)

    /**
     * Crop rect draw paint
     */
    private val cropPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = gridLineWidthInPixel
        style = Paint.Style.STROKE
    }

    /**
     * Corner toggle line width
     */
    private val cornerToggleWidthInPixel = resources.getDimension(R.dimen.corner_toggle_width)

    /**
     * Corner toggle line length
     */
    private val cornerToggleLengthInPixel = resources.getDimension(R.dimen.corner_toggle_length)

    private val minRectLength = resources.getDimension(R.dimen.min_rect)

    /**
     * Corner toggle paint
     */
    private val cornerTogglePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.blue)
        strokeWidth = cornerToggleWidthInPixel
        style = Paint.Style.STROKE
    }

    /**
     * Mask color
     */
    private val maskBackgroundColor = ContextCompat.getColor(context, R.color.colorCropAlpha)

    /**
     * Mask canvas
     */
    private var maskCanvas: Canvas? = null

    /**
     * Mask bitmap
     */
    private var maskBitmap: Bitmap? = null

    private val bitmapGestureListener = object : BitmapGestureHandler.BitmapGestureListener {
        override fun onDoubleTap(motionEvent: MotionEvent) {

            if (isBitmapScaleExceedMaxLimit(DOUBLE_TAP_SCALE_FACTOR)) {

                val resetMatrix = Matrix()
                val scale = max(
                    cropRect.width() / bitmapRect.width(),
                    cropRect.height() / bitmapRect.height()
                )
                resetMatrix.setScale(scale, scale)

                val translateX = (viewWidth - bitmapRect.width() * scale) / 2f + marginInPixelSize
                val translateY = (viewHeight - bitmapRect.height() * scale) / 2f + marginInPixelSize
                resetMatrix.postTranslate(translateX, translateY)

                bitmapMatrix.animateToMatrix(resetMatrix) {
                    notifyCropRectChanged()
                    invalidate()
                }

                return
            }

            bitmapMatrix.animateScaleToPoint(
                DOUBLE_TAP_SCALE_FACTOR,
                motionEvent.x,
                motionEvent.y
            ) {
                notifyCropRectChanged()
                invalidate()
            }
        }

        override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float) {

            /**
             * Return if new calculated bitmap matrix will exceed scale
             * point then early return.
             * Otherwise continue and do calculation and apply to bitmap matrix.
             */
            if (isBitmapScaleExceedMaxLimit(scaleFactor)) {
                return
            }

            zoomInverseMatrix.reset()
            bitmapMatrix.invert(zoomInverseMatrix)

            /**
             * Inverse focus points
             */
            zoomFocusPoint[0] = focusX
            zoomFocusPoint[1] = focusY
            zoomInverseMatrix.mapPoints(zoomFocusPoint)

            /**
             * Scale bitmap matrix
             */
            bitmapMatrix.preScale(
                scaleFactor,
                scaleFactor,
                zoomFocusPoint[0],
                zoomFocusPoint[1]
            )
            notifyCropRectChanged()

            invalidate()
        }

        override fun onScroll(distanceX: Float, distanceY: Float) {
            bitmapMatrix.postTranslate(-distanceX, -distanceY)
            invalidate()
        }

        override fun onEnd() {
            settleDraggedBitmap()
        }
    }

    private val bitmapGestureHandler = BitmapGestureHandler(context, bitmapGestureListener)

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorCropBackground))
    }

    /**
     * Initialize necessary rects, bitmaps, canvas here.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initialize()
    }

    /**
     * Handles touches
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }

        when (event.action) {
            ACTION_DOWN -> {
                val corner = cropRect.getCornerTouch(event, touchThreshold)
                val edge = cropRect.getEdgeTouch(event, touchThreshold)

                draggingState = when {
                    isCorner(corner) -> DraggingCorner(corner)
                    isEdge(edge) -> DraggingEdge(edge)
                    else -> DraggingState.DraggingBitmap
                }

                calculateMinRect()
                calculateMaxRect()
            }
            ACTION_MOVE -> {
                when (val state = draggingState) {
                    is DraggingCorner -> {
                        onCornerPositionChanged(state.corner, event)
                        updateExceedMaxBorders()
                        updateExceedMinBorders()
                        notifyCropRectChanged()
                    }
                    is DraggingEdge -> {
                        onEdgePositionChanged(state.edge, event)
                        updateExceedMaxBorders()
                        updateExceedMinBorders()
                        notifyCropRectChanged()
                    }
                }
            }
            ACTION_UP -> {
                minRect.setEmpty()
                maxRect.setEmpty()
                when (draggingState) {
                    is DraggingEdge, is DraggingCorner -> {
                        calculateCenterTarget()
                        animateBitmapToCenterTarget()
                        animateCropRectToCenterTarget()
                    }
                }
            }
        }

        if (draggingState == DraggingState.DraggingBitmap) {
            bitmapGestureHandler.onTouchEvent(event)
        }

        invalidate()
        return true
    }

    /**
     * Draw bitmap, cropRect, overlay
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        bitmap?.let { bitmap ->
            canvas?.drawBitmap(bitmap, bitmapMatrix, emptyPaint)
        }

        canvas?.save()
        canvas?.clipRect(cropRect, Region.Op.DIFFERENCE)
        canvas?.drawColor(maskBackgroundColor)
        canvas?.restore()

        drawGrid(canvas)

        drawCornerToggles(canvas)
    }

    /**
     * Set bitmap from outside of this view.
     * Calculates bitmap rect and bitmap min rect.
     */
    fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap

        bitmapRect.set(
            0f,
            0f,
            this.bitmap?.width?.toFloat() ?: 0f,
            this.bitmap?.height?.toFloat() ?: 0f
        )

        val bitmapMinRectSize = max(bitmapRect.width(), bitmapRect.height()) / MAX_SCALE
        bitmapMinRect.set(0f, 0f, bitmapMinRectSize, bitmapMinRectSize)

        initialize()

        requestLayout()
        invalidate()
    }

    fun setTheme(croppyTheme: CroppyTheme) {
        cornerTogglePaint.color = ContextCompat.getColor(context, croppyTheme.accentColor)
        invalidate()
    }

    /**
     * Get cropped bitmap.
     */
    fun getCroppedData(): CroppedBitmapData {
        val croppedBitmapRect = getCropSizeOriginal()

        if (bitmapRect.intersect(croppedBitmapRect).not()) {
            return CroppedBitmapData(croppedBitmap = bitmap)
        }

        val cropLeft = if (croppedBitmapRect.left.roundToInt() < bitmapRect.left) {
            bitmapRect.left.toInt()
        } else {
            croppedBitmapRect.left.roundToInt()
        }

        val cropTop = if (croppedBitmapRect.top.roundToInt() < bitmapRect.top) {
            bitmapRect.top.toInt()
        } else {
            croppedBitmapRect.top.roundToInt()
        }

        val cropRight = if (croppedBitmapRect.right.roundToInt() > bitmapRect.right) {
            bitmapRect.right.toInt()
        } else {
            croppedBitmapRect.right.roundToInt()
        }

        val cropBottom = if (croppedBitmapRect.bottom.roundToInt() > bitmapRect.bottom) {
            bitmapRect.bottom.toInt()
        } else {
            croppedBitmapRect.bottom.roundToInt()
        }

        bitmap?.let {
            val croppedBitmap = Bitmap.createBitmap(
                it, cropLeft, cropTop, cropRight - cropLeft, cropBottom - cropTop
            )
            return CroppedBitmapData(croppedBitmap = croppedBitmap)
        }

        throw IllegalStateException("Bitmap is null.")
    }

    /**
     * Changes aspect ratio and aspect mode values and
     * call aspectRatioChanged() method to do calculations
     * and animations from current editState.
     */
    fun setAspectRatio(aspectRatio: AspectRatio) {
        this.selectedAspectRatio = aspectRatio

        aspectAspectMode = when (aspectRatio) {
            ASPECT_FREE -> FREE
            else -> ASPECT
        }

        aspectRatioChanged()
        invalidate()
    }


    /**
     * Current crop size depending on original bitmap.
     * Returns rectangle as pixel values.
     */
    fun getCropSizeOriginal(): RectF {
        val cropSizeOriginal = RectF()
        cropRectOnOriginalBitmapMatrix.reset()
        bitmapMatrix.invert(cropRectOnOriginalBitmapMatrix)
        cropRectOnOriginalBitmapMatrix.mapRect(cropSizeOriginal, cropRect)
        return cropSizeOriginal
    }

    /**
     * Initialize
     */
    private fun initialize() {

        viewWidth = measuredWidth.toFloat() - (marginInPixelSize * 2)

        viewHeight = measuredHeight.toFloat() - (marginInPixelSize * 2)

        viewRect.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())

        createMaskBitmap()

        initializeBitmapMatrix()

        initializeCropRect()

        onInitialized?.invoke()

        invalidate()
    }

    /**
     * Create mask bitmap
     */
    private fun createMaskBitmap() {
        maskBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        maskCanvas = Canvas(maskBitmap!!)
    }

    /**
     * Draw crop rect as a grid.
     */
    private fun drawGrid(canvas: Canvas?) {
        canvas?.drawRect(cropRect, cropPaint)
        canvas?.drawLine(
            cropRect.left + cropRect.width() / 3f,
            cropRect.top,
            cropRect.left + cropRect.width() / 3f,
            cropRect.bottom,
            cropPaint
        )

        canvas?.drawLine(
            cropRect.left + cropRect.width() * 2f / 3f,
            cropRect.top,
            cropRect.left + cropRect.width() * 2f / 3f,
            cropRect.bottom,
            cropPaint
        )

        canvas?.drawLine(
            cropRect.left,
            cropRect.top + cropRect.height() / 3f,
            cropRect.right,
            cropRect.top + cropRect.height() / 3f,
            cropPaint
        )

        canvas?.drawLine(
            cropRect.left,
            cropRect.top + cropRect.height() * 2f / 3f,
            cropRect.right,
            cropRect.top + cropRect.height() * 2f / 3f,
            cropPaint
        )
    }

    /**
     * Draw corner lines and toggles
     */
    private fun drawCornerToggles(canvas: Canvas?) {
        /**
         * Top left toggle
         */
        canvas?.drawLine(
            cropRect.left - gridLineWidthInPixel,
            cropRect.top + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
            cropRect.left + cornerToggleLengthInPixel,
            cropRect.top + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
            cornerTogglePaint
        )

        canvas?.drawLine(
            cropRect.left + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
            cropRect.top - gridLineWidthInPixel,
            cropRect.left + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
            cropRect.top + cornerToggleLengthInPixel,
            cornerTogglePaint
        )

        /**
         * Top Right toggle
         */

        canvas?.drawLine(
            cropRect.right - cornerToggleLengthInPixel,
            cropRect.top + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
            cropRect.right + gridLineWidthInPixel,
            cropRect.top + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
            cornerTogglePaint
        )

        canvas?.drawLine(
            cropRect.right - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
            cropRect.top - gridLineWidthInPixel,
            cropRect.right - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
            cropRect.top + cornerToggleLengthInPixel,
            cornerTogglePaint
        )

        /**
         * Bottom Left toggle
         */

        canvas?.drawLine(
            cropRect.left - gridLineWidthInPixel,
            cropRect.bottom - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
            cropRect.left + cornerToggleLengthInPixel,
            cropRect.bottom - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
            cornerTogglePaint
        )

        canvas?.drawLine(
            cropRect.left + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
            cropRect.bottom + gridLineWidthInPixel,
            cropRect.left + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
            cropRect.bottom - cornerToggleLengthInPixel,
            cornerTogglePaint
        )

        /**
         * Bottom Right toggle
         */
        canvas?.drawLine(
            cropRect.right - cornerToggleLengthInPixel,
            cropRect.bottom - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
            cropRect.right + gridLineWidthInPixel,
            cropRect.bottom - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
            cornerTogglePaint
        )

        canvas?.drawLine(
            cropRect.right - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
            cropRect.bottom + gridLineWidthInPixel,
            cropRect.right - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
            cropRect.bottom - cornerToggleLengthInPixel,
            cornerTogglePaint
        )
    }

    /**
     * If selected aspect ratio is ASPECT_FREE
     * then bitmap matrix and cropRect should be same.
     * Otherwise calculate scale value for bitmap matrix,
     * and animate bitmap matrix to center crop rect.
     *
     * And also in this method. cropRect will be animate to calculated
     * target rect.
     */
    private fun aspectRatioChanged() {
        val widthRatio: Float
        val heightRatio: Float

        if (selectedAspectRatio == ASPECT_FREE) {
            widthRatio = bitmapRect.width() / min(bitmapRect.width(), bitmapRect.height())
            heightRatio = bitmapRect.height() / min(bitmapRect.width(), bitmapRect.height())
        } else {
            widthRatio = selectedAspectRatio.widthRatio
            heightRatio = selectedAspectRatio.heightRatio
        }

        val aspectRatio = widthRatio / heightRatio
        val overlayViewRatio = viewWidth / viewHeight

        val cropWidth: Float
        val cropHeight: Float

        when {
            aspectRatio > overlayViewRatio -> {
                cropWidth = viewWidth
                cropHeight = heightRatio * viewWidth / widthRatio
            }
            else -> {
                cropHeight = viewHeight
                cropWidth = widthRatio * viewHeight / heightRatio
            }
        }

        val distanceToCenterX = viewRect.centerX() - cropWidth / 2f
        val distanceToCenterY = viewRect.centerY() - cropHeight / 2f

        targetRect.set(
            0f + distanceToCenterX,
            0f + distanceToCenterY,
            cropWidth + distanceToCenterX,
            cropHeight + distanceToCenterY
        )

        val resetMatrix = Matrix()
        val scale = max(
            targetRect.width() / bitmapRect.width(),
            targetRect.height() / bitmapRect.height()
        )
        resetMatrix.setScale(scale, scale)

        val translateX = (viewWidth - bitmapRect.width() * scale) / 2f + marginInPixelSize
        val translateY = (viewHeight - bitmapRect.height() * scale) / 2f + marginInPixelSize
        resetMatrix.postTranslate(translateX, translateY)

        bitmapMatrix.animateToMatrix(resetMatrix) {
            invalidate()
        }

        cropRect.animateTo(targetRect) {
            invalidate()
            notifyCropRectChanged()
        }

        targetRect.setEmpty()
    }

    /**
     * Initializes bitmap matrix
     */
    private fun initializeBitmapMatrix() {
        val scale = min(viewWidth / bitmapRect.width(), viewHeight / bitmapRect.height())
        bitmapMatrix.setScale(scale, scale)

        val translateX = (viewWidth - bitmapRect.width() * scale) / 2f + marginInPixelSize
        val translateY = (viewHeight - bitmapRect.height() * scale) / 2f + marginInPixelSize
        bitmapMatrix.postTranslate(translateX, translateY)
    }

    /**
     * Initializes crop rect with bitmap.
     */
    private fun initializeCropRect() {
        val rect = RectF(0f, 0f, bitmapRect.width(), bitmapRect.height())
        bitmapMatrix.mapRect(cropRect, rect)
    }

    /**
     * Check if corner is touched
     * @return true if corner, false otherwise
     */
    private fun isCorner(corner: Corner) = corner != NONE

    /**
     * Check if edge is touched
     * @return true if edge, false otherwise
     */
    private fun isEdge(edge: Edge) = edge != Edge.NONE

    /**
     * Move cropRect on user drag cropRect from corners.
     * Corner will be move to opposite side of the selected cropRect's
     * corner. If aspect ratio selected (Not free), then aspect ration shouldn't
     * be change on cropRect is changed.
     */
    private fun onCornerPositionChanged(corner: Corner, motionEvent: MotionEvent) {
        when (aspectAspectMode) {
            FREE -> {
                when (corner) {
                    TOP_RIGHT -> {
                        cropRect.setTop(motionEvent.y)
                        cropRect.setRight(motionEvent.x)
                    }
                    TOP_LEFT -> {
                        cropRect.setTop(motionEvent.y)
                        cropRect.setLeft(motionEvent.x)
                    }
                    BOTTOM_RIGHT -> {
                        cropRect.setBottom(motionEvent.y)
                        cropRect.setRight(motionEvent.x)
                    }
                    BOTTOM_LEFT -> {
                        cropRect.setBottom(motionEvent.y)
                        cropRect.setLeft(motionEvent.x)
                    }
                    else -> return
                }
            }
            ASPECT -> {
                when (corner) {
                    TOP_RIGHT -> {

                        if (motionEvent.y > minRect.top && motionEvent.x < minRect.right) {
                            return
                        }

                        val motionHypo = hypot(
                            (motionEvent.y - cropRect.bottom).toDouble(),
                            (motionEvent.x - cropRect.left).toDouble()
                        ).toFloat()

                        val differenceWidth = (cropRect.getHypotenus() - motionHypo) / 2
                        val differenceHeight =
                            selectedAspectRatio.heightRatio * differenceWidth / selectedAspectRatio.widthRatio

                        cropRect.setTop(cropRect.top + differenceHeight)
                        cropRect.setRight(cropRect.right - differenceWidth)
                    }
                    TOP_LEFT -> {

                        if (motionEvent.y > minRect.top && motionEvent.x > minRect.left) {
                            return
                        }

                        val motionHypo = hypot(
                            (cropRect.bottom - motionEvent.y).toDouble(),
                            (cropRect.right - motionEvent.x).toDouble()
                        ).toFloat()

                        val differenceWidth = (cropRect.getHypotenus() - motionHypo) / 2
                        val differenceHeight =
                            selectedAspectRatio.heightRatio * differenceWidth / selectedAspectRatio.widthRatio

                        cropRect.setTop(cropRect.top + differenceHeight)
                        cropRect.setLeft(cropRect.left + differenceWidth)
                    }
                    BOTTOM_RIGHT -> {
                        if (motionEvent.y < minRect.bottom && motionEvent.x < minRect.right) {
                            return
                        }

                        val motionHypo = hypot(
                            (cropRect.top - motionEvent.y).toDouble(),
                            (cropRect.left - motionEvent.x).toDouble()
                        ).toFloat()

                        val differenceWidth = (cropRect.getHypotenus() - motionHypo) / 2
                        val differenceHeight =
                            selectedAspectRatio.heightRatio * differenceWidth / selectedAspectRatio.widthRatio

                        cropRect.setBottom(cropRect.bottom - differenceHeight)
                        cropRect.setRight(cropRect.right - differenceWidth)
                    }
                    BOTTOM_LEFT -> {

                        if (motionEvent.y < minRect.bottom && motionEvent.x > minRect.left) {
                            return
                        }

                        val motionHypo = hypot(
                            (cropRect.top - motionEvent.y).toDouble(),
                            (cropRect.right - motionEvent.x).toDouble()
                        ).toFloat()

                        val differenceWidth = (cropRect.getHypotenus() - motionHypo) / 2
                        val differenceHeight =
                            selectedAspectRatio.heightRatio * differenceWidth / selectedAspectRatio.widthRatio

                        cropRect.setBottom(cropRect.bottom - differenceHeight)
                        cropRect.setLeft(cropRect.left + differenceWidth)
                    }
                    else -> return
                }
            }
        }
    }

    /**
     * Move cropRect on user drag cropRect from edges.
     * Corner will be move to opposite side of the selected cropRect's
     * edge. If aspect ratio selected (Not free), then aspect ration shouldn't
     * be change on cropRect is changed.
     */
    private fun onEdgePositionChanged(edge: Edge, motionEvent: MotionEvent) {
        val bitmapBorderRect = RectF()
        bitmapMatrix.mapRect(bitmapBorderRect, bitmapRect)

        when (aspectAspectMode) {
            FREE -> {
                when (edge) {
                    LEFT -> cropRect.setLeft(motionEvent.x)
                    TOP -> cropRect.setTop(motionEvent.y)
                    RIGHT -> cropRect.setRight(motionEvent.x)
                    BOTTOM -> cropRect.setBottom(motionEvent.y)
                    else -> return
                }
            }
            ASPECT -> {
                when (edge) {
                    LEFT -> {
                        val differenceWidth = motionEvent.x - cropRect.left
                        val differenceHeight =
                            selectedAspectRatio.heightRatio * differenceWidth / selectedAspectRatio.widthRatio
                        cropRect.setLeft(cropRect.left + differenceWidth)
                        cropRect.setTop(cropRect.top + differenceHeight / 2f)
                        cropRect.setBottom(cropRect.bottom - differenceHeight / 2f)
                    }
                    TOP -> {
                        val differenceHeight = motionEvent.y - cropRect.top
                        val differenceWidth =
                            selectedAspectRatio.widthRatio * differenceHeight / selectedAspectRatio.heightRatio
                        cropRect.setTop(cropRect.top + differenceHeight)
                        cropRect.setLeft(cropRect.left + differenceWidth / 2f)
                        cropRect.setRight(cropRect.right - differenceWidth / 2f)
                    }
                    RIGHT -> {
                        val differenceWidth = cropRect.right - motionEvent.x
                        val differenceHeight =
                            selectedAspectRatio.heightRatio * differenceWidth / selectedAspectRatio.widthRatio
                        cropRect.setRight(cropRect.right - differenceWidth)
                        cropRect.setTop(cropRect.top + differenceHeight / 2f)
                        cropRect.setBottom(cropRect.bottom - differenceHeight / 2f)
                    }
                    BOTTOM -> {
                        val differenceHeight = cropRect.bottom - motionEvent.y
                        val differenceWidth =
                            selectedAspectRatio.widthRatio * differenceHeight / selectedAspectRatio.heightRatio
                        cropRect.setBottom(cropRect.bottom - differenceHeight)
                        cropRect.setLeft(cropRect.left + differenceWidth / 2f)
                        cropRect.setRight(cropRect.right - differenceWidth / 2f)
                    }
                    else -> return
                }
            }
        }
    }

    /**
     * Calculates minimum possibel rectangle that user can drag
     * cropRect
     */
    private fun calculateMinRect() {
        val mappedBitmapMinRectSize = RectF()
            .apply { bitmapMatrix.mapRect(this, bitmapMinRect) }
            .width()

        val minSize = max(mappedBitmapMinRectSize, minRectLength)

        when (aspectAspectMode) {
            FREE -> {
                when (val state = draggingState) {
                    is DraggingEdge -> {
                        when (state.edge) {
                            LEFT -> minRect.set(
                                cropRect.right - minSize,
                                cropRect.top,
                                cropRect.right,
                                cropRect.bottom
                            )
                            TOP -> minRect.set(
                                cropRect.left,
                                cropRect.bottom - minSize,
                                cropRect.right,
                                cropRect.bottom
                            )
                            RIGHT -> minRect.set(
                                cropRect.left,
                                cropRect.top,
                                cropRect.left + minSize,
                                cropRect.bottom
                            )
                            BOTTOM -> minRect.set(
                                cropRect.left,
                                cropRect.top,
                                cropRect.right,
                                cropRect.top + minSize
                            )
                        }
                    }
                    is DraggingCorner -> {
                        when (state.corner) {
                            TOP_RIGHT -> minRect.set(
                                cropRect.left,
                                cropRect.bottom - minSize,
                                cropRect.left + minSize,
                                cropRect.bottom
                            )
                            TOP_LEFT -> minRect.set(
                                cropRect.right - minSize,
                                cropRect.bottom - minSize,
                                cropRect.right,
                                cropRect.bottom
                            )
                            BOTTOM_RIGHT -> minRect.set(
                                cropRect.left,
                                cropRect.top,
                                cropRect.left + minSize,
                                cropRect.top + minSize
                            )
                            BOTTOM_LEFT -> minRect.set(
                                cropRect.right - minSize,
                                cropRect.top,
                                cropRect.right,
                                cropRect.top + minSize
                            )
                        }
                    }
                }
            }
            ASPECT -> {
                val scaleWidth = minSize / cropRect.width()
                val scaleHeight = minSize / cropRect.height()
                val scale = max(scaleWidth, scaleHeight)

                when (val state = draggingState) {
                    is DraggingEdge -> {
                        val matrix = Matrix()
                        when (state.edge) {
                            LEFT -> matrix.setScale(
                                scale,
                                scale,
                                cropRect.right,
                                cropRect.centerY()
                            )
                            TOP -> matrix.setScale(
                                scale,
                                scale,
                                cropRect.centerX(),
                                cropRect.bottom
                            )
                            RIGHT -> matrix.setScale(
                                scale,
                                scale,
                                cropRect.left,
                                cropRect.centerY()
                            )
                            BOTTOM -> matrix.setScale(
                                scale,
                                scale,
                                cropRect.centerX(),
                                cropRect.top
                            )
                        }
                        matrix.mapRect(minRect, cropRect)
                    }
                    is DraggingCorner -> {
                        val matrix = Matrix()
                        when (state.corner) {
                            TOP_RIGHT -> matrix.setScale(
                                scale,
                                scale,
                                cropRect.left,
                                cropRect.bottom
                            )
                            TOP_LEFT -> matrix.setScale(
                                scale,
                                scale,
                                cropRect.right,
                                cropRect.bottom
                            )
                            BOTTOM_RIGHT -> matrix.setScale(
                                scale,
                                scale,
                                cropRect.left,
                                cropRect.top
                            )
                            BOTTOM_LEFT -> matrix.setScale(
                                scale,
                                scale,
                                cropRect.right,
                                cropRect.top
                            )
                        }
                        matrix.mapRect(minRect, cropRect)
                    }
                }
            }
        }
    }

    /**
     * Calculates maximum possible rectangle that user can
     * drag cropRect
     */
    private fun calculateMaxRect() {
        when (aspectAspectMode) {
            FREE -> {
                val borderRect = RectF().apply {
                    val bitmapBorderRect = RectF()
                    bitmapMatrix.mapRect(bitmapBorderRect, bitmapRect)
                    top = max(bitmapBorderRect.top, viewRect.top)
                    right = min(bitmapBorderRect.right, viewRect.right)
                    bottom = min(bitmapBorderRect.bottom, viewRect.bottom)
                    left = max(bitmapBorderRect.left, viewRect.left)
                }

                when (val state = draggingState) {
                    is DraggingEdge -> {
                        when (state.edge) {
                            LEFT -> maxRect.set(
                                borderRect.left,
                                cropRect.top,
                                cropRect.right,
                                cropRect.bottom
                            )
                            TOP -> maxRect.set(
                                cropRect.left,
                                borderRect.top,
                                cropRect.right,
                                cropRect.bottom
                            )
                            RIGHT -> maxRect.set(
                                cropRect.left,
                                cropRect.top,
                                borderRect.right,
                                cropRect.bottom
                            )
                            BOTTOM -> maxRect.set(
                                cropRect.left,
                                cropRect.top,
                                cropRect.right,
                                borderRect.bottom
                            )
                        }
                    }
                    is DraggingCorner -> {
                        when (state.corner) {
                            TOP_RIGHT -> maxRect.set(
                                cropRect.left,
                                borderRect.top,
                                borderRect.right,
                                cropRect.bottom
                            )
                            TOP_LEFT -> maxRect.set(
                                borderRect.left,
                                borderRect.top,
                                cropRect.right,
                                cropRect.bottom
                            )
                            BOTTOM_RIGHT -> maxRect.set(
                                cropRect.left,
                                cropRect.top,
                                borderRect.right,
                                borderRect.bottom
                            )
                            BOTTOM_LEFT -> maxRect.set(
                                borderRect.left,
                                cropRect.top,
                                cropRect.right,
                                borderRect.bottom
                            )
                        }
                    }
                }

            }
            ASPECT -> {
                val borderRect = RectF().apply {
                    val bitmapBorderRect = RectF()
                    bitmapMatrix.mapRect(bitmapBorderRect, bitmapRect)
                    top = max(bitmapBorderRect.top, viewRect.top)
                    right = min(bitmapBorderRect.right, viewRect.right)
                    bottom = min(bitmapBorderRect.bottom, viewRect.bottom)
                    left = max(bitmapBorderRect.left, viewRect.left)
                }

                when (val state = draggingState) {
                    is DraggingEdge -> {
                        var leftScale =
                            (cropRect.centerX() - borderRect.left) / (cropRect.width() / 2f)
                        var topScale =
                            (cropRect.centerY() - borderRect.top) / (cropRect.height() / 2f)
                        var bottomScale =
                            (borderRect.bottom - cropRect.centerY()) / (cropRect.height() / 2f)
                        var rightScale =
                            (borderRect.right - cropRect.centerX()) / (cropRect.width() / 2f)

                        when (state.edge) {
                            LEFT -> {
                                leftScale = (cropRect.right - borderRect.left) / cropRect.width()
                                val minScale = min(leftScale, min(topScale, bottomScale))
                                val matrix = Matrix()
                                matrix.setScale(
                                    minScale,
                                    minScale,
                                    cropRect.right,
                                    cropRect.centerY()
                                )
                                matrix.mapRect(maxRect, cropRect)
                            }
                            TOP -> {
                                topScale = (cropRect.bottom - borderRect.top) / cropRect.height()
                                val minScale = min(topScale, min(leftScale, rightScale))
                                val matrix = Matrix()
                                matrix.setScale(
                                    minScale,
                                    minScale,
                                    cropRect.centerX(),
                                    cropRect.bottom
                                )
                                matrix.mapRect(maxRect, cropRect)
                            }
                            RIGHT -> {
                                rightScale = (borderRect.right - cropRect.left) / cropRect.width()
                                val minScale = min(rightScale, min(topScale, bottomScale))
                                val matrix = Matrix()
                                matrix.setScale(
                                    minScale,
                                    minScale,
                                    cropRect.left,
                                    cropRect.centerY()
                                )
                                matrix.mapRect(maxRect, cropRect)
                            }
                            BOTTOM -> {
                                bottomScale = (borderRect.bottom - cropRect.top) / cropRect.height()
                                val minScale =
                                    min(bottomScale, min(leftScale, rightScale))
                                val matrix = Matrix()
                                matrix.setScale(
                                    minScale,
                                    minScale,
                                    cropRect.centerX(),
                                    cropRect.top
                                )
                                matrix.mapRect(maxRect, cropRect)
                            }
                        }
                    }
                    is DraggingCorner -> {
                        val leftScale = (cropRect.right - borderRect.left) / cropRect.width()
                        val topScale = (cropRect.bottom - borderRect.top) / cropRect.height()
                        val bottomScale = (borderRect.bottom - cropRect.top) / cropRect.height()
                        val rightScale = (borderRect.right - cropRect.left) / cropRect.width()
                        when (state.corner) {
                            TOP_RIGHT -> {
                                val minScale = min(rightScale, topScale)
                                val matrix = Matrix()
                                matrix.setScale(minScale, minScale, cropRect.left, cropRect.bottom)
                                matrix.mapRect(maxRect, cropRect)
                            }
                            TOP_LEFT -> {
                                val minScale = min(leftScale, topScale)
                                val matrix = Matrix()
                                matrix.setScale(minScale, minScale, cropRect.right, cropRect.bottom)
                                matrix.mapRect(maxRect, cropRect)
                            }
                            BOTTOM_RIGHT -> {
                                val minScale = min(rightScale, bottomScale)
                                val matrix = Matrix()
                                matrix.setScale(minScale, minScale, cropRect.left, cropRect.top)
                                matrix.mapRect(maxRect, cropRect)
                            }
                            BOTTOM_LEFT -> {
                                val minScale = min(leftScale, bottomScale)
                                val matrix = Matrix()
                                matrix.setScale(minScale, minScale, cropRect.right, cropRect.top)
                                matrix.mapRect(maxRect, cropRect)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * If user exceed its limit we override cropRect borders
     */
    private fun updateExceedMaxBorders() {
        if (cropRect.left < maxRect.left) {
            cropRect.left = maxRect.left
        }

        if (cropRect.top < maxRect.top) {
            cropRect.top = maxRect.top
        }

        if (cropRect.right > maxRect.right) {
            cropRect.right = maxRect.right
        }

        if (cropRect.bottom > maxRect.bottom) {
            cropRect.bottom = maxRect.bottom
        }
    }

    /**
     * If user exceed its limit we override cropRect borders
     */
    private fun updateExceedMinBorders() {
        if (cropRect.left > minRect.left) {
            cropRect.left = minRect.left
        }

        if (cropRect.top > minRect.top) {
            cropRect.top = minRect.top
        }

        if (cropRect.right < minRect.right) {
            cropRect.right = minRect.right
        }

        if (cropRect.bottom < minRect.bottom) {
            cropRect.bottom = minRect.bottom
        }
    }

    /**
     * If user miminize the croprect, we need to
     * calculate target centered rectangle according to
     * current cropRect aspect ratio and size. With this
     * target rectangle, we can animate crop rect to
     * center target. and also we can animate bitmap matrix
     * to selected cropRect using this target rectangle.
     */
    private fun calculateCenterTarget() {
        val heightScale = viewHeight / cropRect.height()
        val widthScale = viewWidth / cropRect.width()
        val scale = min(heightScale, widthScale)

        val targetRectWidth = cropRect.width() * scale
        val targetRectHeight = cropRect.height() * scale

        val targetRectLeft = (viewWidth - targetRectWidth) / 2f + marginInPixelSize
        val targetRectTop = (viewHeight - targetRectHeight) / 2f + marginInPixelSize
        val targetRectRight = targetRectLeft + targetRectWidth
        val targetRectBottom = targetRectTop + targetRectHeight

        targetRect.set(targetRectLeft, targetRectTop, targetRectRight, targetRectBottom)
    }

    /**
     * When user change cropRect size by dragging it, cropRect
     * should be animated to center without changing aspect ratio,
     * meanwhile bitmap matrix should be take selected crop rect to
     * the center. This methods take selected crop rect to the cennter.
     */
    private fun animateBitmapToCenterTarget() {
        val newBitmapMatrix = bitmapMatrix.clone()

        val scale = targetRect.width() / cropRect.width()
        val translateX = targetRect.centerX() - cropRect.centerX()
        val translateY = targetRect.centerY() - cropRect.centerY()

        val matrix = Matrix()
        matrix.setScale(scale, scale, cropRect.centerX(), cropRect.centerY())
        matrix.postTranslate(translateX, translateY)
        newBitmapMatrix.postConcat(matrix)

        bitmapMatrix.animateToMatrix(newBitmapMatrix) {
            invalidate()
        }
    }

    /**
     * Animates current croprect to the center position
     */
    private fun animateCropRectToCenterTarget() {
        cropRect.animateTo(targetRect) {
            invalidate()
            notifyCropRectChanged()
        }
    }

    /**
     * when user drag bitmap too much, we need to settle bitmap matrix
     * back to the possible closest edge.
     */
    private fun settleDraggedBitmap() {
        val draggedBitmapRect = RectF()
        bitmapMatrix.mapRect(draggedBitmapRect, bitmapRect)

        /**
         * Scale dragged matrix if it needs to
         */
        val widthScale = cropRect.width() / draggedBitmapRect.width()
        val heightScale = cropRect.height() / draggedBitmapRect.height()
        var scale = 1.0f

        if (widthScale > 1.0f || heightScale > 1.0f) {
            scale = max(widthScale, heightScale)
        }

        /**
         * Calculate new scaled matrix for dragged bitmap matrix
         */
        val scaledRect = RectF()
        val scaledMatrix = Matrix()
        scaledMatrix.setScale(scale, scale)
        scaledMatrix.mapRect(scaledRect, draggedBitmapRect)


        /**
         * Calculate translateX
         */
        var translateX = 0f
        if (scaledRect.left > cropRect.left) {
            translateX = cropRect.left - scaledRect.left
        }

        if (scaledRect.right < cropRect.right) {
            translateX = cropRect.right - scaledRect.right
        }

        /**
         * Calculate translateX
         */
        var translateY = 0f
        if (scaledRect.top > cropRect.top) {
            translateY = cropRect.top - scaledRect.top
        }

        if (scaledRect.bottom < cropRect.bottom) {
            translateY = cropRect.bottom - scaledRect.bottom
        }

        /**
         * New temp bitmap matrix
         */
        val newBitmapMatrix = bitmapMatrix.clone()

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(translateX, translateY)
        newBitmapMatrix.postConcat(matrix)

        bitmapMatrix.animateToMatrix(newBitmapMatrix) {
            invalidate()
            notifyCropRectChanged()
        }
    }

    /**
     * Pretend a bitmap matrix value if scale factor will be applied to
     * bitmap matrix. , then returns
     * true, false otherwise.
     * @return true If pretended value is exceed max scale value, false otherwise
     */
    private fun isBitmapScaleExceedMaxLimit(scaleFactor: Float): Boolean {
        val bitmapMatrixCopy = bitmapMatrix.clone()
        bitmapMatrixCopy.preScale(scaleFactor, scaleFactor)

        val invertedBitmapMatrix = Matrix()
        bitmapMatrixCopy.invert(invertedBitmapMatrix)

        val invertedBitmapCropRect = RectF()

        invertedBitmapMatrix.mapRect(invertedBitmapCropRect, cropRect)
        return min(
            invertedBitmapCropRect.width(),
            invertedBitmapCropRect.height()
        ) <= bitmapMinRect.width()
    }

    private fun notifyCropRectChanged() {
        observeCropRectOnOriginalBitmapChanged?.invoke(getCropSizeOriginal())
    }

    companion object {

        /**
         * Maximum scale for given bitmap
         */
        private const val MAX_SCALE = 15f

        /**
         * Use this constant, when user double tap to scale
         */
        private const val DOUBLE_TAP_SCALE_FACTOR = 2f

    }
}