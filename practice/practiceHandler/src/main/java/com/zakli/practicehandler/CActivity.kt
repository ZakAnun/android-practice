package com.zakli.practicehandler

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 *
 * @author zak
 * @date 2021/8/10
 * @email linhenji@163.com / linhenji17@gmail.com
 */
class CActivity: AppCompatActivity() {

    companion object {

        private const val TAG = "activity"

        @JvmStatic
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, CActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.phandler_c)

        Log.d(TAG, "onCreate: singleTask activity")

        findViewById<TextView>(R.id.cText).setOnClickListener {
            Toast.makeText(this, "nothing go", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: singleTask activity")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: singleTask activity")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: singleTask activity")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: singleTask activity")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: singleTask activity")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: singleTask activity")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: singleTask activity")
    }
}