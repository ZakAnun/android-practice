package com.zakli.androidpractice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val selfMac = findViewById<TextView>(R.id.selfMac)
        val workMac = findViewById<TextView>(R.id.workMac)
        selfMac.text = "self mac add second"
        workMac.text = "work mac add second"
    }
}