package com.zakli.practicehandler

import android.os.*
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Handler
 *  - 同步屏障，会在消息队列中阻挡同步信息，但开发者不能主动发起这个消息（在源码中会有看到）
 *  同步屏障不会拦截异步信息，设置后，同步信息会被拦截，异步信息会被处理
 *  如果不设置同步屏障的话，不管信息是同步信息还是异步信息都会按照发起的顺序处理
 *  可以将同步屏障理解为确保异步信息被处理的优先级
 *
 *  MessageQueue#postSyncBarrier() // 发同步屏障消息
 *   - 会根据当前时间创建一个屏障消息，屏障消息和普通消息的区别是屏障消息没有 target（屏障消息无须被分发，所以没有 target）
 *   - 根据时间顺序将屏障插入到消息链表中适当的位置（所以只能挡住后来的同步消息）
 *   - 返回 token，用于取消屏障消息
 *   - 如果插入同步屏障的话，消息队列会被唤醒
 *
 *  MessageQueue#next() // 获取消息
 *   - nativePollOnce(ptr, nextPollTimeoutMillis) // 用于阻塞，因为是在一个死循环中，如果消息队列没有消息或超时时间到了，就阻塞
 *   - 发现 msg.target == null 表示遇到屏障，会遍历消息链表找到最近的一条异步消息
 *   - 找到异步消息后判断是否已经到时间，没到就等待，到了就将该消息从链表移除并返回它
 *
 *  MessageQueue#removeSyncBarrier() // 移除同步屏障
 *   - 根据发送时返回的 token 进行移除（存在 Message 的 arg1 中）
 */
class PracticeHandlerActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "PracticeHandlerActivity"

        private const val MESSAGE_TYPE_SYNC = 1000
        private const val MESSAGE_TYPE_ASYNC = 1001
    }

    private lateinit var handler: Handler

//    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.phandler_main)

        Thread {
            Looper.prepare()
            handler = Handler {
                if (it.what == MESSAGE_TYPE_SYNC) {
                    Log.d(TAG, "onCreate: ${getString(R.string.phandler_receive_sync_msg)}")
                } else if (it.what == MESSAGE_TYPE_ASYNC) {
                    Log.d(TAG, "onCreate: ${getString(R.string.phandler_receive_async_msg)}")
                }
                // 返回 true 不对 msg 进行进一步处理
                false
            }
            Looper.loop()
        }.start()

        findViewById<TextView>(R.id.setSyncMsg).setOnClickListener {
            Log.d(TAG, "onCreate: 不能主动发起")
//            Log.d(TAG, "onCreate: ${(it as TextView).text}")
//            val queue: MessageQueue = handler.looper.queue
//            queue.postSyncBarrier()
        }
//
//        findViewById<TextView>(R.id.delSyncMsg).setOnClickListener {
//            Log.d(TAG, "onCreate: ${(it as TextView).text}")
//        }

        findViewById<TextView>(R.id.insertSyncMsg).setOnClickListener {
            Log.d(TAG, "onCreate: ${(it as TextView).text}")
            val msg = Message.obtain()
            msg.what = MESSAGE_TYPE_SYNC
            handler.sendMessageDelayed(msg, 1000)
        }

        findViewById<TextView>(R.id.insertAsyncMsg).setOnClickListener {
            Log.d(TAG, "onCreate: ${(it as TextView).text}")
            val msg = Message.obtain()
            msg.what = MESSAGE_TYPE_ASYNC
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Log.d(TAG, "onCreate: API >= 22 设置异步信息")
                msg.isAsynchronous = true
            } else {
                Log.d(TAG, "onCreate: API 21 及以前只能是同步信息")
            }
            handler.sendMessageDelayed(msg, 1000)
        }
    }
}