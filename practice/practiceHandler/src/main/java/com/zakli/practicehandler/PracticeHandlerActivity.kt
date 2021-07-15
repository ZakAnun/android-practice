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
 *
 * Handler 消息机制
 *  - Looper: Android 的消息机制中扮演消息循环的角色，具体是它会不断地从 MessageQueue 中查看是否有消息，
 *  如果有消息会立即处理，否则一直阻塞（一个线程最多有一个 Looper 对象，一个 Looper 对应管理此线程的 MessageQueue，二者一一对应）
 *   - prepare(): 可以为当前线程创建一个 Looper 对象。通过 ThreadLocal 获取当前线程的 Looper 对象，如果获取 Looper 对象不为空
 *   说明已经创建过 Looper 对象，抛出异常。如果没有创建 Looper，先创建一个 Looper 对象，
 *   通过 ThreadLocal 保存到当前线程的 ThreadLocalMap 中，这样保证每个线程只有一个 Looper
 *   - myLooper(): 可以获取当前线程的 Looper 对象
 *   - Looper 构造器是私有的，所以不允许外部直接通过 new 方式调用，同时在构造方法中初始化了 MessageQueue 对象和关联到当前线程
 *   - prepareMainLooper(): 主线程的 Looper 对象创建，调用 prepare(false)，设置 quitAllowed 为 false 不允许退出，
 *   创建成功后，从 ThreadLocal 中获取 Looper 对象，并赋值给 Looper 的静态变量 sMainLooper，这样就可以在任何地方都能
 *   获取主线程的 Looper（getMainLooper）
 *   - Activity#main() 中可以看到创建了主线程的 Looper 对象，并调用了 Looper.loop() 开启消息循环
 *   - loop(): 只有调用了 loop() 后，消息循环系统才会真正地起作用
 *    - 获取当前线程本地存储区存储的 looper 对象
 *    - 如果没有通过 prepare() 创建 Looper，直接抛出异常
 *    - 获取 Looper 对应 MessageQueue
 *    - 进入死循环
 *     - 获取 MessageQueue 中的消息处理，可能会阻塞（MessageQueue 为空或者头部 Message 没到触发时间）
 *     - 当获取消息为空时才会退出循环（当 Looper 调用 quit()，会调用 MessageQueue#quit 或者 quitSafely 来通知消息队列退出，
 *     当消息队列被标记为退出时，它的 next() 就会返回 null
 *     - msg.target 是发送这条消息的 Handler 对象，这样 Handler 发送的消息最终又交给它的 dispatchMessage() 来处理
 *      - Handler#dispatchMessage() 是在创建 Handler 时所使用的 Looper 中执行，这样就成功地将代码逻辑切换到指定的线程中
 *     - msg.recycleUnchecked() 回收消息（将消息放入消息池，通过 Message#obtain() 会消耗一个消息）
 *    - quit()、quitSafely()
 *     - 都调用 MessageQueue#quit(safe)
 *     - quit() safe 传 false，会调 removeAllFutureMessagesLocked()，如果链表头部的 Message 触发时间大于当前时间，
 *     直接删除 MessageQueue 的所有 Message，如果 MessageQueue 非头节点 Message 触发时间大于当前时间，删除该节点后面所有的 Message
 *     - quitSafely() safe 传 true，会 调 removeAllMessagesLocked()，遍历回收所有节点
 *    - 子线程中如果手动为其创建了 Looper，那么在处理完所有的消息后应该调用 quit() 来终止消息循环，否则会一直处于阻塞状态
 *  - MessageQueue: 消息队列，内部存储一组消息，以队列的形式对外提供插入消息和读取消息（读取操作本身伴随删除操作）
 *  采用单链表的数据结构来存储消息（因为单链表在插入和删除上又较好的性能）
 *   - 主要包含插入消息和读取消息（读取伴随着删除）两个操作，其中 enqueueMessage 的作用是往消息列表中插入一条消息，而 next 的作用
 *   就是从消息队列中取出一条消息并删除
 *   - next()
 *    - mPtr 消息循环退出标识位，当 mPtr 等于 0 时，说明 loop 消息循环已经退出，返回 null
 *    - 首次循环 pendingIdleHandlerCount 为 -1
 *    - nativePollOnce(): 阻塞操作，当等待 nextPollTimeoutMillis 时长或消息队列又新消息加入时唤醒操作
 *    - 获取当前消息 mMessage
 *    - 遇到同步屏障消息，会遍历消息链表找到最近的一条异步消息
 *    - 消息不为 null，当异步消息触发时间大于当前时间，则设置下一次轮询问的超时时长，否则获取消息并删除返回这条消息
 *    - 消息为 null，没有消息并设置 nextPollTimeoutMillis = -1，会一直等待下去
 *    - 当退出时，直接返回 null，结束循环
 *    - 如果MessageQueue为空，或者队列头部的消息触发时间还没有到，同时 pendingIdleHandlerCount 小于 0 时（刚进这个方法时会设置为-1）
 *     - pendingIdleHandlerCount 会设置为 mIdleHandlers 的数量
 *    - 如果 mIdleHandlers <= 0，继续下一次循环获取 Message 获取阻塞
 *    - 如果 mPendingIdleHandlers 为空，而且设置了 IdleHandler，创建数组并将 mIdleHandlers 转为数组
 *    - 只有一次循环，会运行 IdleHandler，执行完成后，重置 mPendingIdleHandlers 为 0
 *     - 每次循环去掉 mPendingIdleHandlers 元素的引用（并没有删除 IdleHandlers 里的元素）
 *     - 执行 IdleHandler#queueIdle()
 *     - 当 queueIdle() 返回 false 时，删除 mIdleHandlers 元素
 *    - 重置 pendingIdleHandlerCount 个数为 0，以保证不会再次重复运行
 *    - 设置不阻塞，查询新的 Message
 *   - IdleHandler（MessageQueue 内定义的一个接口，一般用于性能优化）
 *    - 当消息队列内没有需要立即执行的 message 时，会主动触发 IdleHandler#queueIdle
 *    返回值为 false（即只会执行一次）
 *    返回值为 true（即每次当消息队列内没有需要立即执行的消息时，都会触发该方法）
 *    - ActivityThread#GcIdler，在某些场景等待消息队列暂时空闲时会尝试执行 GC 操作
 *    - ActivityThread#Idler，在 handlerResumeActivity() 内会注册 Idler()，等待 handlerResumeActivity() 视图绘制完成后，
 *    消息队列暂时空闲时在调用 AMS#activityIdle() 检查页面的生命周期状态，触发 activity#stop 生命周期
 *    （也是为什么 BActivity 跳转 CActivity 时，BActivity#onStop() 会在 CActivity#onResume() 后执行
 *   - enqueueMessage()
 *    - 按照 Message 触发时间的先后顺序排序的，头部消息是最早触发的消息，当有消息需要加入消息列表时，会从队列头开始遍历，
 *    直到找到消息对应插入的位置，以保证所有消息的时间顺序
 *    - 每个普通 Message 必须要有一个 target，否则会抛出 IllegalArgumentException
 *    - 如果执行了退出操作，那么会将 Message 回收（加入消息池）
 *    - 如果链表头节点为空或者链表头节点的执行时间大于当前插入的 Message，那么就将 Message 插入链表头部（when == 0 表示立即执行）
 *     - 如果 MessageQueue 被阻塞（会将需要唤醒操作的标识位置为需要被唤醒）
 *    - 如果 MessageQueue 被阻塞的头节点为同步屏障，并且当前消息是异步消息，那么也会进行唤醒
 *     - 遍历 MessageQueue，如果 MessageQueue 中有比插入 Message 执行时间更后的 Message，就把 Message 插入到该消息之前
 *     否则插入到链表的尾部
 *      - 如果当前插入消息之前还有异步消息那么不唤醒
 *     - 插入链表
 *   - removeMessage()
 *    - 从 MessageQueue 中移除指定消息，移除方法采用了两个 while 循环，
 *    第一个循环从链表头部开始（头节点如果是待删除节点），移除连续符合条件的消息，并将头节点指针指向第一个不满足条件的节点
 *    第二个循环是从头部移除完成，再从新的头部轮询所有满足条件的节点（遍历删除头节点后每个满足条件的节点）
 *  - Message: 消息，分为硬件产生的消息（如点击、触摸）和软件生成的消息
 *   - 消息池（当消息池不为空时，可以直接从消息池中获取 Message 对象，而不是直接创建，提高效率）
 *   通过静态变量 sPool（数据类型为 Message），通过 next 成员变量维护一个消息池，静态变量 MAX_POOL_SIZE 代表消息池的可用大小（默认 50）
 *   - obtain()
 *    - 从消息池中获取消息
 *    - 如果消息池不为空
 *     - 从 sPool 的表头拿出 Message
 *     - 将消息池的表头指向下一个 Message
 *     - 将取出消息的链表断开
 *     - 清除 flag（flag 标记判断此消息是否正被使用
 *     - sPoolSize-- 将消息池的可用大小减 1
 *    - 消息池为空，则直接创建 Message
 *   - recycle()
 *    - 将当前 Message 的 next 指向消息池的头节点
 *    - 当消息池没有满时，将 Message 加入消息池
 *    - 消息池可用大小加 1
 *  - Handler（消息的处理者）
 *   - 向 MessageQueue 发送消息和处理 MessageQueue 分发出来的消息
 *   - Handler()
 *    - Callback 可用于不希望派生 Handler 子类时的场景，也是用来处理消息的
 *    - 如果子线程没有创建 Looper 就使用 Handler 会抛出异常
 *    （在创建时，会默认关联当前线程的 Looper，如果 Looper 对象为空，说明当前线程没调用 Looper.prepare()）
 *    - 关联 Looper 后，会关联 Looper 的 MessageQueue
 *   - dispatchMessage 处理消息（在 MessageQueue 的 loop() 中被调用）
 *    - 检查 Message#callback 是否为空，不为空，直接调用 Message#callback 的 run()（这个 callback 是个 Runnable）
 *    - 否则判断 Handler#mCallback（这个 mCallback 是 Callback，只有一个 handleMessage()）
 *    如果 mCallback 不为空， mCallback#handleMessage return true，就不会执行 Handler 的子类重写的 handleMessage()，
 *    如果 mCallback 为空，或者 mCallback#handleMessage return false，就会执行 Handler 子类重写的 handleMessage()
 *   - 发送消息
 *   （post(Runnable)，postAtTime(Runnable, Long)，postDelayed(Runnable, Long)，
 *   sendEmptyMessage(Int)，sendMessage(Message)，sendMessageAt(Message, Long)
 *   sendMessageDelayed(Message, Long)）
 *   一种是 Runnable 对象，一种是 Message 对象，但最后都被封装成 Message 对象
 *   （post(Runnable) 的 Message 的 what 为 0，删除该消息的方法是 Handler.removeMessages(0)）
 *   post、send 方法都会走到 enqueueMessage 方法里，最终调用 MessageQueue#enqueueMessage 将消息插入消息队列中
 *   - Handler#enqueueMessage
 *    - 设置消息的 target 为发送该消息的 Handler 本身
 *    - 调用 MessageQueue#enqueueMessage()
 *  - ThreadLocal: 一个线程内部的数据存储类（保证线程内部数据在各线程间相互独立）
 *  - QA
 *   - Q: IdleHandler 的作用
 *   - A: 是 Handler 提供的一种在消息队列空闲时，执行任务的时机，当 MessageQueue 当前没有立即需要处理的消息时，
 *   会执行 IdleHandler#queueIdle()
 *   - Q: MessageQueue 提供的 add/remove IdleHandler 方法，是否必须成对使用
 *   - A: 不必须，IdleHandler#queueIdle() 的返回值，可以移除加入 MessageQueue 的 IdleHandler
 *   - Q: 当 mIdleHandlers 一直不为空时，为什么不会进入死循环
 *   - A: 只有在 pendingIdleHandlerCount == -1 时，才会尝试执行 IdleHandler，
 *   pendingIdleHandlerCount 在 next() 中初始时为 -1，执行一遍后会被置为 0，所以不会重复运行
 *   - Q: 是否可以将一些不重要的启动任务搬移到 IdleHandler 中处理
 *   - A: 不建议，因为 IdleHandler 的处理时机不可控，如果 MessageQueue 一直有待处理的消息，
 *   那么 IdleHandler 的执行时机就会很靠后
 *   - Q: IdleHandler#queueIdle() 运行在哪个线程
 *   - A: queueIdle() 运行的线程，只和当前 MessageQueue 的 Looper 所在的线程有关，
 *   子线程一样可以构造 Looper，并添加 IdleHandler
 *
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