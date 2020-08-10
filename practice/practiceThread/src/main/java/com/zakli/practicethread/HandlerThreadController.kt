package com.zakli.practicethread

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import java.util.concurrent.ConcurrentHashMap

object HandlerThreadController {

    const val msgWhat = 1
    var isQuit = false

    private val threadMap: ConcurrentHashMap<String, HandlerThread> by lazy {
        ConcurrentHashMap<String, HandlerThread>()
    }

    private val handlerMap: ConcurrentHashMap<String, Handler> by lazy {
        ConcurrentHashMap<String, Handler>()
    }

    fun start(name: String,
              msgHandler: Handler,
              extensionCallback: (() -> Unit)? = null) {
        val handlerThread = HandlerThread(name)
        threadMap[name] = handlerThread
        handlerThread.start()
        // 收取 handlerThread 的信息
        val handler = Handler(handlerThread.looper)
        // 这里还是主线程的 Looper
        extensionCallback?.invoke()
        handler.post {
            var isRunning = true
            var count = 0
            while (isRunning && !isQuit) {
                count++
                if (count >= 100) {
                    isRunning = false
                    // 这是子线程的 Looper
                    extensionCallback?.invoke()
                }
                val msg = Message.obtain()
                msg.what = msgWhat
                msg.obj = count
                msgHandler.sendMessage(msg)
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        handlerMap[name] = handler
    }

    fun quitByName(name: String, safety: Boolean = false) {
        isQuit = true
        if (safety) {
            threadMap[name]?.quitSafely()
            handlerMap[name]?.removeCallbacksAndMessages(null)
        } else {
            threadMap[name]?.quit()
            handlerMap[name]?.removeCallbacksAndMessages(null)
        }
    }

    fun quitAll(safety: Boolean = false) {
        isQuit = true
        for (handlerThread in threadMap.values) {
            if (safety) {
                handlerThread.quitSafely()
            } else {
                handlerThread.quit()
            }
        }
        for (handler in handlerMap.values) {
            handler.removeCallbacksAndMessages(null)
        }
    }
}