package com.zakli.practicelayout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.LayoutInflaterCompat

/**
 * 布局耗时的获取
 *  - 基本方式（在 setContentView 前后打点，计算时间差，缺点是代码侵入性强，多 Activity 统计不方便）
 *  - AOP 方式（面向切面编程，不会入侵源代码，通过 AspectJ 实现，见 SetContentViewAspect）
 *   - AOP 方式通常还会用作埋点、性能监控等场景
 *  - 通过 Factory 提供的一种 hook 方法，可以拦截 LayoutInflater 创建 View 的过程
 *   - 应用场景：全局替换系统控件为自定义 View；替换 app 中字体；全局换肤；获取控件加载耗时等
 *   - 这个方法是可以拿到布局中每个子 view 的加载时间
 */
class LayoutTimeConsumeActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "LayoutTcActivity"

        fun startActivity(context: Context) {
            context.startActivity(
                Intent(context, LayoutTimeConsumeActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        setFactory2()

        super.onCreate(savedInstanceState)

        // 方式一
        val start = SystemClock.elapsedRealtime()
        setContentView(R.layout.playout_tc_activity)
        val end = SystemClock.elapsedRealtime()

        Log.e(TAG, "onCreate: layout time consume = ${end - start}")
    }

    /**
     * setFactory2 需要在 onCreate 之前调用
     * 因为在 AppCompatActivity#onCreate 中有一行代码 delegate.installViewFactory();
     * 这个代码是用于给 LayoutInflater 设置 Factory2 的
     * LayoutInflater#setFactory2 是不能重复设置的，所以需要在 onCreate 之前添加
     */
    private fun setFactory2() {
        LayoutInflaterCompat.setFactory2(layoutInflater, object : LayoutInflater.Factory2 {
            override fun onCreateView(
                parent: View?,
                name: String,
                context: Context,
                attrs: AttributeSet
            ): View? {
                val startTime = SystemClock.elapsedRealtime()
                val view = delegate.createView(parent, name, context, attrs)
                if (view != null) {
                    val endTime = SystemClock.elapsedRealtime()
                    val cost = endTime - startTime
                    Log.e(TAG, "onCreateView: view = $view, cost = $cost")
                }
                return view
            }

            override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
                return null
            }

        })
    }
}