package com.zakli.practicethread

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PracticeThreadActivity: AppCompatActivity() {

    private val msgHandlerList = ArrayList<Handler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pthread_main)
        var count = 0
        val textView = findViewById<TextView>(R.id.textView)
        findViewById<Button>(R.id.startHt).setOnClickListener {
            val name = "HandlerThread-$count"
            val msgHandler = Handler {
                val what = it.what
                val currentProgress = it.obj as Int
                println("name = $name, what = $what, currentProgress = $currentProgress")
                textView.text = String.format(getString(R.string.pthread_show_msg), name, currentProgress)
                true
            }
            msgHandlerList.add(msgHandler)
            HandlerThreadController.start(name, msgHandler) {
                // 这里不用设置 Looper 是因为要执行这个代码的 handler 已经设置过 looper 了
                Toast.makeText(this, "thread finish toast ${Looper.myLooper()?.thread?.name ?: String()}", Toast.LENGTH_SHORT).show()
            }
            ++count
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        HandlerThreadController.quitAll()
        for (handler in msgHandlerList) {
            handler.removeCallbacksAndMessages(null)
        }
    }
}