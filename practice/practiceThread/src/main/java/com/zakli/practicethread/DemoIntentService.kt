package com.zakli.practicethread

import android.app.IntentService
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.util.Log

class DemoIntentService: IntentService(TAG) {

    companion object {
        private const val TAG = "DemoIntentService"
    }

    override fun onHandleIntent(intent: Intent?) {
        intent?.run {
            val uiMessenger = extras?.get("messenger") as? Messenger
            when (getStringExtra("action")) {
                "action_one" -> {
                    Thread.sleep(1000)
                    uiMessenger?.run {
                        val msg = Message.obtain()
                        msg.what = 1
                        msg.obj = "action_one execute complete"
                        send(msg)
                    }
                    Log.d(TAG, "onHandleIntent: action_one do something ok after 1s")
                }
                "action_two" -> {
                    Thread.sleep(2000)
                    Log.d(TAG, "onHandleIntent: action_two do something ok after 2s")
                    uiMessenger?.run {
                        val msg = Message.obtain()
                        msg.what = 2
                        msg.obj = "action_two execute complete"
                        send(msg)
                    }
                }
                else -> {
                    Thread.sleep(3000)
                    Log.d(TAG, "onHandleIntent: unknown action, v try in 3s")
                    uiMessenger?.run {
                        val msg = Message.obtain()
                        msg.what = 3
                        msg.obj = "unknown action execute complete"
                        send(msg)
                    }
                }
            }
        }
    }
}