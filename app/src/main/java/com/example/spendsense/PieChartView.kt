package com.example.spendsense

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Slice(
        val value: Float,
        val color: Int
    )

    private val slices: MutableList<Slice> = mutableListOf()

    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        setShadowLayer(18f, 0f, 10f, Color.argb(70, 0, 0, 0))
    }

    private val arcRect = RectF()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setData(newSlices: List<Slice>) {
        slices.clear()
        slices.addAll(newSlices)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val size = min(w, h)
        val left = (w - size) / 2f
        val top = (h - size) / 2f
        val right = left + size
        val bottom = top + size

        arcRect.set(left, top, right, bottom)

        val total = slices.sumOf { it.value.toDouble() }.toFloat()
        if (total <= 0f) {
            shadowPaint.color = Color.LTGRAY
            canvas.drawOval(arcRect, shadowPaint)
            slicePaint.color = Color.LTGRAY
            canvas.drawOval(arcRect, slicePaint)
            return
        }

        shadowPaint.color = Color.WHITE
        canvas.drawOval(arcRect, shadowPaint)

        var startAngle = -90f
        for (slice in slices) {
            val sweep = (slice.value / total) * 360f
            slicePaint.color = slice.color
            canvas.drawArc(arcRect, startAngle, sweep, true, slicePaint)
            startAngle += sweep
        }
    }
}
