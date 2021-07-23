package com.zakli.practicemotion

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 *
 * @author zak
 * @date 2021/7/23
 * @email linhenji@163.com / linhenji17@gmail.com
 */
class FirstActivity: AppCompatActivity() {

    private val firstFragment by lazy {
        FirstFragment()
    }

    private val secondFragment by lazy {
        SecondFragment()
    }

    private var onShowFirstFragment = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.pmotion_first_activity)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, firstFragment)
            commit()
        }

        // Container transform
        findViewById<TextView>(R.id.changeFragment).setOnClickListener {
            val textView = it as TextView
            if (onShowFirstFragment) {
                textView.text = resources.getString(R.string.pmotion_go_first_fragment)
                supportFragmentManager.beginTransaction()
                    .addSharedElement(checkNotNull(firstFragment.view?.findViewById(R.id.firstView)), "shared_element_container")
                    .replace(R.id.fragmentContainer, secondFragment, SecondFragment.TAG)
                    .addToBackStack(SecondFragment.TAG)
                    .commit()
            } else {
                textView.text = resources.getString(R.string.pmotion_go_second_fragment)
                supportFragmentManager.beginTransaction()
                    .addSharedElement(checkNotNull(secondFragment.view?.findViewById(R.id.secondView)), "shared_element_container")
                    .replace (R.id.fragmentContainer, firstFragment, FirstFragment.TAG)
                    .addToBackStack(FirstFragment.TAG)
                    .commit()
            }
            onShowFirstFragment = !onShowFirstFragment
        }
    }
}