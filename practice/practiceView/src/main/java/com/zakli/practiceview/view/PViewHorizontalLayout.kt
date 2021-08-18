package com.zakli.practiceview.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.widget.Scroller
import kotlin.math.abs

class PViewHorizontalLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet ? = null, defStyleAttr: Int = 0
): ViewGroup(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "PViewHorizontalLayout"
    }

    private var childrenSize = 0
    private var mChildWidth = 0
    private var mChildIndex = 0

    // 记录上次滑动的坐标
    private var mLastX = 0f
    private var mLastY = 0f

    // 记录上次拦截的坐标
    private var mLastXIntercept = 0f
    private var mLastYIntercept = 0f

    private var mScroller: Scroller = Scroller(context)
    private var mVelocityTracker: VelocityTracker = VelocityTracker.obtain()

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
                mChildWidth = childWidth
                childView.layout(childLeft, 0, childLeft + childWidth, childView.measuredHeight)
                childLeft += childWidth
            }
        }
    }

    /**
     * 外部拦截法
     *
     * @param ev ev
     */
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val event = ev ?: return super.onInterceptTouchEvent(ev)
        var intercepted = false

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                intercepted = false
                Log.d(TAG, "onInterceptTouchEvent: mScroller.isFinished = ${mScroller.isFinished}")
                if (mScroller.isFinished) {
                    mScroller.abortAnimation()
                    intercepted = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - mLastXIntercept
                val deltaY = y - mLastYIntercept
                intercepted = abs(deltaX) > abs(deltaY)
            }
            MotionEvent.ACTION_UP -> {
                intercepted = false
            }
        }
        Log.d(TAG, "onInterceptTouchEvent: intercepted = $intercepted")

        mLastX = x
        mLastY = y
        mLastXIntercept = x
        mLastYIntercept = y

        return intercepted
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val ev = event ?: return super.onTouchEvent(event)

        mVelocityTracker.addMovement(ev)
        val x = ev.x
        val y = ev.y

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - mLastX
                scrollBy((-deltaX).toInt(), 0)
            }
            MotionEvent.ACTION_UP -> {
                val scrollX = scrollX
                mVelocityTracker.computeCurrentVelocity(1000)
                val xVelocity = mVelocityTracker.xVelocity
                mChildIndex = if (abs(xVelocity) >= 50) {
                    if (xVelocity > 0) {
                        mChildIndex - 1
                    } else {
                        mChildIndex + 1
                    }
                } else {
                    (scrollX + mChildWidth / 2) / mChildWidth
                }
                mChildIndex = 0.coerceAtLeast(mChildIndex.coerceAtMost(childrenSize + 1))
                val dx = mChildIndex * mChildWidth - scrollX
                smoothScrollBy(dx)
                mVelocityTracker.clear()

                performClick()
            }
        }

        mLastX = x
        mLastY = y

        return super.onTouchEvent(event)
    }

    private fun smoothScrollBy(dx: Int) {
        mScroller.startScroll(scrollX, 0, dx, 0, 0)
        invalidate()
    }
}