package com.zakli.practicelayout

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LayoutActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.playout_activity)

        findViewById<TextView>(R.id.pLayoutTc).setOnClickListener {
            LayoutTimeConsumeActivity.startActivity(this)
        }

        findViewById<TextView>(R.id.pLayoutOpti).setOnClickListener {
            LayoutDrawOptiActivity.startActivity(this)
        }
    }
}