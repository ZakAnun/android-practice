package com.zakli.practicethread

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HIActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.pthread_hi_activity)

        findViewById<TextView>(R.id.pThreadHt).setOnClickListener {
            HandlerThreadActivity.startActivity(this)
        }
        findViewById<TextView>(R.id.pThreadIs).setOnClickListener {
            IntentServiceActivity.startActivity(this)
        }
    }
}