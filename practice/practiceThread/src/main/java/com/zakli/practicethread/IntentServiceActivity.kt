package com.zakli.practicethread

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Messenger
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * IntentService 继承自 Service 且是一个抽象类，所以需要创建它的子类才能使用 IntentService
 * 可用于执行后台耗时任务，当任务执行完后，它会自动停止，由于它是个 Service 所以优先级会比线程高，
 * 所以 IntentService 可用于执行优先级高的后台任务（因为它不容易被系统杀死）
 *
 * IntentService 封装了 HandlerThread 和 Handler
 *
 * 代码分析
 *  - 首次启动 IntentService，onCreate 会被调用，里面会创建一个 HandlerThread
 *  然后使用 Looper 创建一个 ServiceHandler 对象，通过 ServiceHandler 发送的消息最终都在 HandlerThread 中年执行
 *  这个 ServiceHandler 继承自 Handler，重写了 handleMessage 方法（因为 Looper 来自 HandlerThread，
 *  所以 handlerMessage 中的逻辑是可以执行耗时操作的）
 *  - 每次启动 IntentService，它的 onStartCommand 就会被调用一次，onStartCommand 会调 onStart
 *  IntentService 仅仅通过 mServiceHandler 发送一个消息（会在 HandlerThread 中被处理）
 *  - onStart() 的入参 intent，就是 startService 中的 intent，可以通过这个 intent 传入信息
 *  - ServiceHandler#handleMessage 中会调用 onHandleIntent，这个方法执行完成后，IntentService 会通过
 *  stopSelf(startId) 来停止服务（不采用 stopSelf() 是因为它会立即停止服务，而此时可能还有其他消息没处理完）
 *  stopSelf(startId）则会等待所有消息都处理完毕才会终止服务
 *  - onHandleIntent 方法是个抽象方法，需要在子类中实现，作用是可以通过 intent 的参数区分具体的任务并且执行
 *  如果有多个后台任务，那么 onHandleIntent 执行完最后一个任务时，stopSelf(startId) 才会直接停止服务。另外，
 *  由于每执行一个后台任务就必须启动一次 IntentService，而 IntentService 内部通过消息的方式向 HandlerThread
 *  请求执行任务，Handler 中的 Looper 是顺序处理消息的，所以 IntentService 也是按启动顺序处理消息的
 */
class IntentServiceActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "IntentServiceActivity"

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, IntentServiceActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.pthread_is_activity)

        val result = findViewById<TextView>(R.id.pIsResult)

        val updateUIHandler = Handler {
            result.text = it.obj.toString()
            true
        }

        var hasClick = false
        findViewById<TextView>(R.id.pIsSendMsg).setOnClickListener {
            // 这里的消息会等下面四个消息处理完之后，再开始处理
            if (hasClick) {
                (it as TextView).text = getString(R.string.pthread_one)
                startService(Intent(this, DemoIntentService::class.java).apply {
                    putExtra("action", "action_two")
                    putExtra("messenger", Messenger(updateUIHandler))
                })
            } else {
                (it as TextView).text = getString(R.string.pthread_two)
                startService(Intent(this, DemoIntentService::class.java).apply {
                    putExtra("action", "action_one")
                    putExtra("messenger", Messenger(updateUIHandler))
                })
            }
            hasClick = !hasClick
        }

        startService(Intent(this, DemoIntentService::class.java).apply {
            putExtra("action", "action_one")
            putExtra("messenger", Messenger(updateUIHandler))
        })
        startService(Intent(this, DemoIntentService::class.java).apply {
            putExtra("action", "action_two")
            putExtra("messenger", Messenger(updateUIHandler))
        })
        startService(Intent(this, DemoIntentService::class.java).apply {
            putExtra("action", "action_one")
            putExtra("messenger", Messenger(updateUIHandler))
        })
        startService(Intent(this, DemoIntentService::class.java).apply {
            putExtra("action", "")
            putExtra("messenger", Messenger(updateUIHandler))
        })
    }
}