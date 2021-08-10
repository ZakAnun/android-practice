package com.zakli.practicehandler

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity 启动模式的 demo
 * singleInstance 会新建一个任务栈，一旦有其他任务栈进入前台，那么会等到该任务栈中的 Activity 弹完后
 * 才能到另外的任务栈弹出（对应生命周期，前一个 Activity onPause 先回调，然后新的 Activity 声明周期回调
 * 再到前一个 Activity 的 onStop 回调，前提是前一个 Activity 不是透明的，透明 Activity 不会回调 onStop）
 *
 * @author zak
 * @date 2021/8/10
 * @email linhenji@163.com / linhenji17@gmail.com
 */
class AActivity: AppCompatActivity() {

    companion object {
        
        private const val TAG = "activity"
        
        @JvmStatic
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, AActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.phandler_a)

        Log.d(TAG, "onCreate: standard activity")

        findViewById<TextView>(R.id.aText).setOnClickListener {
            BActivity.startActivity(this)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: standard activity")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: standard activity")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: standard activity")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: standard activity")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: standard activity")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: standard activity")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: standard activity")
    }
}