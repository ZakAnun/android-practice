package com.zakli.practicemotion

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

/**
 *
 * @author zak
 * @date 2021/7/23
 * @email linhenji@163.com / linhenji17@gmail.com
 */
class SecondActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        setContentView(R.layout.pmotion_second_activity)
    }
}