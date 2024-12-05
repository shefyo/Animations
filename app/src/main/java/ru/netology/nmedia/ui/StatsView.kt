package ru.netology.nmedia.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    private var progress = mutableListOf<Float>()
    private var rotationAngle = 0F

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            progress = MutableList(value.size) { 0F }
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        canvas.save()
        canvas.rotate(rotationAngle, center.x, center.y)

        var startFrom = -90F
        for ((index, datum) in data.withIndex()) {
            val angle = 360F * datum
            paint.color = colors.getOrNull(index) ?: randomColor()
            canvas.drawArc(oval, startFrom, angle * progress[index], false, paint)
            startFrom += angle
        }

        canvas.restore()

        canvas.drawText(
            "%.2f%%".format(data.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }

    private fun update() {
        val totalDuration = 2000L

        for (i in data.indices) {
            val animator = ValueAnimator.ofFloat(0F, 1F).apply {
                duration = totalDuration
                interpolator = LinearInterpolator()
                startDelay = (i * 100).toLong()
                addUpdateListener { animation ->
                    progress[i] = animation.animatedValue as Float
                    invalidate()
                }
            }
            animator.start()
        }

        val rotationAnimator = ValueAnimator.ofFloat(0F, 360F).apply {
            duration = totalDuration
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                rotationAngle = animation.animatedValue as Float
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rotationAngle = 0F
                    invalidate()
                }
            })
        }
        rotationAnimator.start()
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}
