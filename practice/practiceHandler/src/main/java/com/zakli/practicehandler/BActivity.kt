package com.zakli.practicehandler

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 *
 * @author zak
 * @date 2021/8/10
 * @email linhenji@163.com / linhenji17@gmail.com
 */
class BActivity: AppCompatActivity() {

    companion object {

        private const val TAG = "activity"

        @JvmStatic
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, BActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.phandler_b)

        Log.d(TAG, "onCreate: singleInstance activity")

        findViewById<TextView>(R.id.bText).setOnClickListener {
            CActivity.startActivity(this)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: singleInstance activity")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: singleInstance activity")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: singleInstance activity")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: singleInstance activity")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: singleInstance activity")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: singleInstance activity")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: singleInstance activity")
    }
}