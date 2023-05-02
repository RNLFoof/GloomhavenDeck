package com.example.gloomhavendeck

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color.red
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StatusBarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatusBarFragment() : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addListener()
        return inflater.inflate(R.layout.fragment_status_bar, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun addListener() {
        Controller.player?.playerSignal?.addListener { _, new ->
            updateContents()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun updateContents() {
        val tlStatusContainer = requireView().findViewById<FrameLayout>(R.id.tlStatusContainer)
        val colorFrom = 0xffff0000.toInt()
        val colorTo = 0xff0000ff.toInt()
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(),
            0xffff0000.toInt(),
            0xffffff00.toInt(),
            0xff00ff00.toInt(),
            0xff00ffff.toInt(),
            0xff0000ff.toInt(),
            0xffff00ff.toInt(),
            0xffff0000.toInt(),
        )
        colorAnimation.duration = 1000 // milliseconds
        colorAnimation.repeatCount = Animation.INFINITE;

        colorAnimation.addUpdateListener { animator ->
            tlStatusContainer.setBackgroundColor(animator.animatedValue as Int)
            //colorAnimation.values.s
        }
        colorAnimation.start()

        if (Controller.player == null) {
            return
        }
        if (view == null) {
            return
        }
        val llStatusBar = requireView().findViewById<LinearLayout>(R.id.llStatusBar)
        llStatusBar.removeAllViews()
        val params = LinearLayout.LayoutParams(
            llStatusBar.layoutParams.height,
            llStatusBar.layoutParams.height,
            1.0f
        )

        for (status in Controller.player!!.statuses) {
            for (x in 1..Controller.player!!.checkStatus(status)) {
                val imageView = status.imageView(this.requireContext())
                imageView.layoutParams = params
                llStatusBar.addView(imageView)
            }
        }
    }
}