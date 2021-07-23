package com.zakli.practicemotion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialContainerTransform

/**
 *
 * @author zak
 * @date 2021/7/23
 * @email linhenji@163.com / linhenji17@gmail.com
 */
class SecondFragment: Fragment() {

    companion object {
        const val TAG = "SecondFragment"
    }

    private var shareView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pmotion_second_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shareView = view.findViewById(R.id.secondView)
    }

//    fun go(target: Fragment, tag: String) {
//        childFragmentManager.beginTransaction()
//            .addSharedElement(checkNotNull(shareView), "shared_element_container")
//            .replace(R.id.fragmentContainer, target, tag)
//            .addToBackStack(tag)
//            .commit()
//    }
}