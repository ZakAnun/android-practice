package com.zakli.practicethread

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import java.util.concurrent.ConcurrentHashMap

/**
 * HandlerThread 继承自 Thread，是一种可以使用 Handler 的 Thread，通过处理消息的 Handler，
 * 可以实现 ui 线程和子线程的消息传送
 *  - 实现原理：在 run() 中通过 Looper.prepare() 创建 Looper 对象和消息队列并通过 Looper.loop() 开启消息循环
 *  从而实现在 HandlerThread 中创建 Handler
 *  - 作用：避免每次子线程中创建 Handler 都需要创建 Looper 对象
 *  - 获取 Looper 对象的方式
 *   - HandlerThread#getLooper
 *   - 在线程中可以通过 Looper.myLoop() 创建 Handler 的时候可以传入这个 Looper，但需要先调用 Thread.start()
 *  - 代码分析
 *   - HandlerThread#run
 *    - 是一个无限循环（所以在明确不再使用 HandlerThread 时，可通过 quit 或 quitSafely 来终止线程的执行
 *    - 1、创建 HandlerThread 的 Looper 对象 和 MessageQueue
 *    - 2、将成员变量 mLooper 赋值为当前线程的 Looper
 *    - 3、唤醒所有等待的线程（唤醒是为了如果没有调用 start 方法就去 getLooper() 中等待
 *    - 4、开启消息循环前的回调
 *    - 5、开始循环
 *   - HandlerThread#getLooper
 *    - 1、判断当前线程是否存活，已死亡就 return null
 *    - 2、如果线程已经创建了，就等待 Looper 创建完成
 *    - 3、wait() 等待 Looper 被创建
 *   - quit() 和 quitSafely() 是直接调用 Looper#quit、Looper#quitSafely
 *    - 这俩方法分别是调用 MessageQueue.quit(false)、MessageQueue.quit(true)
 *    - 调用后 Looper 不再接收新的消息，消息循环会被终结
 *    - 但 quitSafely 会将非延迟消息全部派发完成（removeAllFutureMessagesLocked），
 *    丢弃延迟消息（一旦遇到延迟消息，那么后面的消息都会丢弃）
 *    - 而 quit 则是会将所有的消息都丢弃
 */
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