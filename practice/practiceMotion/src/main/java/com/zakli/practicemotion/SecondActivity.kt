package com.zakli.practicemotion

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback

/**
 *
 * @author zak
 * @date 2021/7/23
 * @email linhenji@163.com / linhenji17@gmail.com
 */
class SecondActivity: AppCompatActivity() {

    companion object {
        @JvmStatic
        fun startActivity(context: Context, startView: View) {
            val intent = Intent(context, SecondActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                context as Activity, startView, "share_activity")
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        // Enable Activity Transitions. Optionally enable Activity transitions in your
        // theme with <item name=”android:windowActivityTransitions”>true</item>.
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

        // Set the transition name, which matches Activity A’s start view transition name, on
        // the root view.
        findViewById<View>(android.R.id.content).transitionName = "share_activity"

        // Attach a callback used to receive the shared elements from Activity A to be
        // used by the container transform transition.
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        // Set this Activity’s enter and return transition to a MaterialContainerTransform
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 250L
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.pmotion_second_activity)

        findViewById<TextView>(R.id.backToActivity).setOnClickListener {
            onBackPressed()
        }
    }
}