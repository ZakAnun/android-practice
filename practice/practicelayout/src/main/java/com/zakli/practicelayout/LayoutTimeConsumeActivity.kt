package com.zakli.practicelayout

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * 布局耗时的获取
 *  - 基本方式（在 setContentView 前后打点，计算时间差，缺点是代码侵入性强，多 Activity 统计不方便）
 *  - AOP 方式（面向切面编程，不会入侵源代码，通过 AspectJ 实现）
 *   - AOP 方式通常还会用作埋点、性能监控等场景
 */
class LayoutTimeConsumeActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "LayoutTcActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 方式一
        val start = SystemClock.elapsedRealtime()
        setContentView(R.layout.playout_tc_activity)
        val end = SystemClock.elapsedRealtime()

        Log.e(TAG, "onCreate: layout time consume = ${end - start}")
    }
}