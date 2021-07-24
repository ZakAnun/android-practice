package com.zakli.practicemotion

import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback

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

        // Enable Activity Transitions. Optionally enable Activity transitions in your
        // theme with <item name=”android:windowActivityTransitions”>true</item>.
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

        // Attach a callback used to capture the shared elements from this Activity to be used
        // by the container transform transition
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        // Keep system bars (status bar, navigation bar) persistent throughout the transition.
        window.sharedElementsUseOverlay = false

        super.onCreate(savedInstanceState)

        setContentView(R.layout.pmotion_first_activity)

        val container = findViewById<ConstraintLayout>(R.id.container)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, firstFragment)
            commit()
        }

        // Container transform（Fragment）
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

        // Container transform（Activity）
        // 查看效果需要放开 FirstFragment、SecondFragment Container transform 注释
        findViewById<TextView>(R.id.changeActivity).setOnClickListener {
            SecondActivity.startActivity(this, it)
        }

        // Shared axis
        // 查看效果需要放开 FirstFragment、SecondFragment Shared axis 注释
        findViewById<TextView>(R.id.sharedAxisFragment).setOnClickListener {
            val textView = it as TextView
            if (onShowFirstFragment) {
                textView.text = resources.getString(R.string.pmotion_shared_axis_go)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, secondFragment, SecondFragment.TAG)
                    .commit()
            } else {
                textView.text = resources.getString(R.string.pmotion_shared_axis_back)
                supportFragmentManager.beginTransaction()
                    .replace (R.id.fragmentContainer, firstFragment, FirstFragment.TAG)
                    .commit()
            }
            onShowFirstFragment = !onShowFirstFragment
        }

        // Fade through
        // 查看效果需要放开 FirstFragment、SecondFragment Fade through 注释
        findViewById<TextView>(R.id.fadeThroughFragment).setOnClickListener {
            if (onShowFirstFragment) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, secondFragment, SecondFragment.TAG)
                    .commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace (R.id.fragmentContainer, firstFragment, FirstFragment.TAG)
                    .commit()
            }
            onShowFirstFragment = !onShowFirstFragment
        }

        // Fade
        val fadeTarget = findViewById<View>(R.id.fadeTarget)
        findViewById<TextView>(R.id.fade).setOnClickListener {
            if (fadeTarget.visibility != View.VISIBLE) {
                val materialFade = MaterialFade().apply {
                    duration = 300L
                }
                TransitionManager.beginDelayedTransition(container, materialFade)
                fadeTarget.visibility = View.VISIBLE
            } else {
                val materialFade = MaterialFade().apply {
                    duration = 300L
                }
                TransitionManager.beginDelayedTransition(container, materialFade)
                fadeTarget.visibility = View.GONE
            }
        }
    }
}