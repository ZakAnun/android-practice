package com.zakli.practiceview.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

class PViewHorizontalLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet ? = null, defStyleAttr: Int = 0
): ViewGroup(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val childCount = childCount
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

        if (childCount == 0) {
            setMeasuredDimension(0, 0)
        } else if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            val childView = getChildAt(0)
            setMeasuredDimension(childView.measuredWidth * childCount, childView.measuredHeight)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            val childView = getChildAt(0)
            setMeasuredDimension(childView.measuredWidth * childCount, heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            val childView = getChildAt(0)
            setMeasuredDimension(widthSpecSize, childView.measuredHeight)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var childLeft = 0
        for (index in 0 until childCount) {
            val childView = getChildAt(index)
            if (childView.visibility != View.GONE) {
                val childWidth = childView.measuredWidth
                childView.layout(childLeft, 0, childLeft + childWidth, childView.measuredHeight)
                childLeft += childWidth
            }
        }
    }
}