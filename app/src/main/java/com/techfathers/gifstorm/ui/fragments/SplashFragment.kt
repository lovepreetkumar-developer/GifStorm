package com.techfathers.gifstorm.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.navigation.fragment.findNavController
import com.techfathers.gifstorm.R
import com.techfathers.gifstorm.databinding.FragmentSplashBinding
import com.techfathers.gifstorm.util.Constants
import com.techfathers.gifstorm.ui.base.BaseFragment

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class SplashFragment : BaseFragment<FragmentSplashBinding>() {

    private var mHandler: Handler? = null

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_splash
    }

    override fun onFragmentCreateView(savedInstanceState: Bundle?) {
        super.onFragmentCreateView(savedInstanceState)
        initView()
    }

    override fun onDestroy() {
        mHandler?.removeCallbacks(mRunnable)
        super.onDestroy()
    }

    private fun initView() {
        mHandler = Handler(Looper.getMainLooper())
        mHandler?.postDelayed(mRunnable, Constants.SPLASH_TIME_OUT)
    }

    private val mRunnable = Runnable {
        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToHomeFragment())
    }
}