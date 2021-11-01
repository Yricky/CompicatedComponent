package com.bytedance.compicatedcomponent.homework

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 *  author : neo
 *  time   : 2021/10/25
 *  desc   :
 */
class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val FULL_ANGLE = 360

        private const val CUSTOM_ALPHA = 140
        private const val FULL_ALPHA = 255

        private const val POINTER_TYPE_SECOND = 2
        private const val POINTER_TYPE_MINUTES = 1
        private const val POINTER_TYPE_HOURS = 0

        private const val DEFAULT_PRIMARY_COLOR: Int = Color.WHITE
        private const val DEFAULT_SECONDARY_COLOR: Int = Color.LTGRAY

        private const val DEFAULT_DEGREE_STROKE_WIDTH = 0.010f

        private const val UNIT_DEGREE = (6 * Math.PI / 180).toFloat() // 一个小格的度数

        private const val STATUS_SYS_CLK = 0
        private const val STATUS_BIAS_CLK = 2
        private const val STATUS_SETTING = 1
    }
    private var status = STATUS_SYS_CLK

    private var panelRadius = 200.0f // 表盘半径

    private var hourPointerLength = 0f // 指针长度

    private var minutePointerLength = 0f
    private var secondPointerLength = 0f

    private var resultWidth = 0
    private  var centerX: Int = 0
    private  var centerY: Int = 0
    private  var radius: Int = 0

    private var degreesColor = 0

    private val needlePaint: Paint

    private val r:Runnable by lazy {
        Runnable {
            postDelayed (r, 1000)
            when(status){
                STATUS_SYS_CLK -> postInvalidate()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(r)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(r)
    }

    init {
        degreesColor = DEFAULT_PRIMARY_COLOR
        needlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        needlePaint.style = Paint.Style.FILL_AND_STROKE
        needlePaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size: Int
        val width = measuredWidth
        val height = measuredHeight
        val widthWithoutPadding = width - paddingLeft - paddingRight
        val heightWithoutPadding = height - paddingTop - paddingBottom
        size = if (widthWithoutPadding > heightWithoutPadding) {
            heightWithoutPadding
        } else {
            widthWithoutPadding
        }
        setMeasuredDimension(size + paddingLeft + paddingRight, size + paddingTop + paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        resultWidth = if (height > width) width else height
        val halfWidth = resultWidth / 2
        centerX = halfWidth
        centerY = halfWidth
        radius = halfWidth
        panelRadius = radius.toFloat()
        hourPointerLength = panelRadius * 0.4f
        minutePointerLength = panelRadius * 0.6f
        secondPointerLength = panelRadius * 0.8f
        drawDegrees(canvas)
        drawHoursValues(canvas)
        drawNeedles(canvas)
    }

    private fun drawDegrees(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = resultWidth * DEFAULT_DEGREE_STROKE_WIDTH
            color = degreesColor
        }
        val rPadded: Int = centerX - (resultWidth * 0.01f).toInt()
        val rEnd: Int = centerX - (resultWidth * 0.05f).toInt()
        var i = 0
        while (i < FULL_ANGLE) {
            if (i % 15 != 0) {
                paint.alpha = CUSTOM_ALPHA
            } else {
                paint.alpha = FULL_ALPHA
            }
            val startX = (centerX + rPadded * cos(Math.toRadians(i.toDouble())))
            val startY = (centerX - rPadded * sin(Math.toRadians(i.toDouble())))
            val stopX = (centerX + rEnd * cos(Math.toRadians(i.toDouble())))
            val stopY = (centerX - rEnd * sin(Math.toRadians(i.toDouble())))
            canvas.drawLine(
                startX.toFloat(),
                startY.toFloat(),
                stopX.toFloat(),
                stopY.toFloat(),
                paint
            )
            i += 6
        }
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private fun drawHoursValues(canvas: Canvas) {
        // Default Color:
        // - hoursValuesColor
    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private fun drawNeedles(canvas: Canvas) {
        val now = Date(System.currentTimeMillis())
        val hours: Int = now.hours
        val minutes: Int = now.minutes
        val seconds: Int = now.seconds
        // 画秒针
        if(status == STATUS_SYS_CLK)
            drawPointer(canvas, POINTER_TYPE_SECOND, seconds)
        // 画分针
        drawPointer(canvas, POINTER_TYPE_MINUTES,minutes)
        // 画时针
        val part = minutes / 12
        drawPointer(canvas, POINTER_TYPE_HOURS, 5 * hours + part)
    }


    private fun drawPointer(canvas: Canvas, pointerType: Int, value: Int) {
        val degree: Float
        var pointerHeadXY = FloatArray(2)
        needlePaint.strokeWidth = resultWidth * DEFAULT_DEGREE_STROKE_WIDTH
        when (pointerType) {
            POINTER_TYPE_HOURS -> {
                degree = value * UNIT_DEGREE
                needlePaint.color = Color.WHITE
                pointerHeadXY = getPointerHeadXY(hourPointerLength, degree)
            }
            POINTER_TYPE_MINUTES -> {
                degree = value * UNIT_DEGREE
                needlePaint.color = Color.YELLOW
                pointerHeadXY = getPointerHeadXY(minutePointerLength,degree)
            }
            POINTER_TYPE_SECOND -> {
                degree = value * UNIT_DEGREE
                needlePaint.color = Color.GREEN
                pointerHeadXY = getPointerHeadXY(secondPointerLength, degree)
            }
        }
        canvas.drawLine(
            centerX.toFloat(), centerY.toFloat(),
            pointerHeadXY[0], pointerHeadXY[1], needlePaint
        )
    }

    private fun getPointerHeadXY(pointerLength: Float, degree: Float): FloatArray {
        val xy = FloatArray(2)
        xy[0] = centerX + pointerLength * sin(degree)
        xy[1] = centerY - pointerLength * cos(degree)
        return xy
    }
}