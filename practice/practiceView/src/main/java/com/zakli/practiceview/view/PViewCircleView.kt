package com.zakli.practiceview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zakli.practiceview.R

class PViewCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mColor: Int = Color.RED
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private fun init() {
        mPaint.color = mColor
    }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PViewCircleView)
        mColor = a.getColor(R.styleable.PViewCircleView_PViewCircleColor, Color.RED)
        a.recycle()
        init()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 获取宽度测量模式
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        // 获取控件提供的 view 宽的最大值
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)

        // 获取高度测量模式
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        // 获取控件提供的 view 高的最大值
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(200, 200)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(200, heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, 200)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val width = width - paddingLeft - paddingRight
        val height = height - paddingTop - paddingBottom
        val radius = width.coerceAtMost(height) / 2
        canvas.drawCircle((paddingLeft + width / 2).toFloat(),
            (paddingTop + height / 2).toFloat(), radius.toFloat(), mPaint)
    }
}