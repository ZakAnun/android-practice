package com.zakli.practicelayout.aspect

import android.os.SystemClock
import android.util.Log
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

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