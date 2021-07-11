package com.zakli.practicelayout.aspect

import android.os.SystemClock
import android.util.Log
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

/**
 * 这是 Aspect 的简单使用，还有更多的操作标识就查看文档吧
 * 这个 demo，可以在 LayoutTimeConsumeActivity#setContentView 执行的时候，将加载布局时间的代码加进去
 * 运行这个 demo，可以看到两个日志（因为在 onCreate 里也打了）
 */
@Aspect
class SetContentViewAspect {

    companion object {
        private const val TAG = "SetContentViewAspect"
    }

    @Around("call(* com.zakli.practicelayout.LayoutTimeConsumeActivity.setContentView(..))")
    fun hookSetContentView(joinPoint: ProceedingJoinPoint) {
        val signature = joinPoint.signature
        // 开始执行
        val name = signature.toShortString()
        val startTime = SystemClock.elapsedRealtime()
        kotlin.runCatching {
            // 调用原来的方法
            joinPoint.proceed()
        }.onFailure {
            it.printStackTrace()
        }
        // 执行结束，记录时间
        val endTime = SystemClock.elapsedRealtime() - startTime
        Log.e(TAG, "hookSetContentView: aspect way to get time consume, method name = $name, costTime = $endTime")
    }
}